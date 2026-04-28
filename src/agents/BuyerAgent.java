package agents;

import gui.ManualNegotiationUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SSContractNetResponder;
import jade.proto.SSResponderDispatcher;
import logging.AuctionLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BuyerAgent extends Agent {

    private String targetMake = "Toyota";
    private String targetModel = "Camry";
    private double initialOffer = 110000.0;
    private double maxPrice = 130000.0;

    private int maxRounds = 5;
    private boolean isManualMode = false;

    private AID brokerAID;

    // This prevents the buyer from repeatedly forwarding interest after dealers are already found.
    private boolean interestAlreadyForwarded = false;

    // Retry control for broker matching.
    // If a buyer joins before dealers exist, the buyer will retry instead of stopping permanently.
    private int matchRequestAttempts = 0;
    private static final int MAX_MATCH_REQUEST_ATTEMPTS = 10;
    private static final long MATCH_RETRY_INTERVAL_MS = 2000L;

    private final Map<AID, Double> previousDealerPrices = new HashMap<>();

    @Override
    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            targetMake = args[0].toString();
            targetModel = args[1].toString();

            if (args.length > 2 && args[2] != null) {
                initialOffer = Double.parseDouble(args[2].toString());
            }

            if (args.length > 3 && args[3] != null) {
                maxPrice = Double.parseDouble(args[3].toString());
            }

            if (args.length > 4 && args[4] != null) {
                maxRounds = Integer.parseInt(args[4].toString());
            }

            if (args.length > 5 && args[5] != null) {
                String val = args[5].toString();
                isManualMode = val.equalsIgnoreCase("true");
            }
        }

        AuctionLog.info(
                getLocalName(),
                "Seeking " + targetMake + " " + targetModel
                        + " | Budget: RM" + maxPrice
                        + " | Mode: " + (isManualMode ? "Manual" : "Auto")
        );

        addBehaviour(new RequestDealersFromBrokerRetry(this, MATCH_RETRY_INTERVAL_MS));
        addBehaviour(new ReceiveMatchingDealers());

        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);

        addBehaviour(new SSResponderDispatcher(this, mt) {
            @Override
            protected jade.core.behaviours.Behaviour createResponder(ACLMessage cfp) {
                return new BuyerNegotiationResponder(myAgent, cfp);
            }
        });
    }

    private class RequestDealersFromBrokerRetry extends TickerBehaviour {

        public RequestDealersFromBrokerRetry(Agent agent, long period) {
            super(agent, period);
        }

        @Override
        protected void onTick() {
            if (interestAlreadyForwarded) {
                stop();
                return;
            }

            if (matchRequestAttempts >= MAX_MATCH_REQUEST_ATTEMPTS) {
                AuctionLog.warn(
                        getLocalName(),
                        "Stopped searching after " + MAX_MATCH_REQUEST_ATTEMPTS
                                + " broker match attempts. No suitable dealer found."
                );
                stop();
                return;
            }

            matchRequestAttempts++;

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();

            sd.setType("car-brokering");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                if (result.length > 0) {
                    brokerAID = result[0].getName();

                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(brokerAID);
                    msg.setConversationId("broker-match");
                    msg.setContent(targetMake + "," + targetModel);

                    myAgent.send(msg);

                    AuctionLog.info(
                            getLocalName(),
                            "Requested dealers from broker. Attempt "
                                    + matchRequestAttempts + "/"
                                    + MAX_MATCH_REQUEST_ATTEMPTS
                    );
                } else {
                    AuctionLog.warn(
                            getLocalName(),
                            "No broker found. Attempt "
                                    + matchRequestAttempts + "/"
                                    + MAX_MATCH_REQUEST_ATTEMPTS
                    );
                }

            } catch (FIPAException e) {
                AuctionLog.error(getLocalName(), "Failed to search for broker: " + e.getMessage());
            }
        }
    }

    private class ReceiveMatchingDealers extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("broker-match")
            );

            ACLMessage msg = myAgent.receive(mt);

            if (msg != null && brokerAID != null && msg.getSender().equals(brokerAID)) {
                try {
                    ArrayList<AID> availableDealers = (ArrayList<AID>) msg.getContentObject();

                    AuctionLog.info(getLocalName(), "Broker matched " + availableDealers.size() + " dealers.");

                    if (!availableDealers.isEmpty()) {
                        // Once matching dealers are found, prevent duplicate interest messages
                        // from future retry ticks.
                        interestAlreadyForwarded = true;

                        StringBuilder sb = new StringBuilder(
                                targetMake + "," + targetModel + "," + initialOffer
                        );

                        int limit = Math.min(availableDealers.size(), 3);

                        for (int i = 0; i < limit; i++) {
                            AID dealer = availableDealers.get(i);
                            sb.append(",").append(dealer.getLocalName());
                        }

                        ACLMessage interest = new ACLMessage(ACLMessage.INFORM);
                        interest.addReceiver(brokerAID);
                        interest.setConversationId("buyer-interest");
                        interest.setContent(sb.toString());

                        myAgent.send(interest);

                        AuctionLog.info(
                                getLocalName(),
                                "Interest forwarded via broker to " + limit + " dealer(s)."
                        );
                    } else {
                        AuctionLog.warn(
                                getLocalName(),
                                "Broker matched 0 dealers. Waiting for future dealer listings..."
                        );
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

            } else {
                block();
            }
        }
    }

    private class BuyerNegotiationResponder extends SSContractNetResponder {

        public BuyerNegotiationResponder(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) {
            String[] parts = cfp.getContent().split(",");

            String make = parts[0];
            String model = parts[1];
            double dealerPrice = Double.parseDouble(parts[2]);
            int round = Integer.parseInt(parts[3]);

            ACLMessage propose = cfp.createReply();

            AuctionLog.info(getLocalName(), "Received offer RM" + dealerPrice + " from " + cfp.getSender().getLocalName() + " (Round " + round + ")");

            if (round >= maxRounds) {
                propose.setPerformative(ACLMessage.REFUSE);
                AuctionLog.warn(getLocalName(), "Max rounds reached. Walking away from " + cfp.getSender().getLocalName());
                return propose;
            }

            if (isManualMode) {
                return handleManualNegotiation(cfp, propose, make, model, dealerPrice, round);
            } else {
                return handleAutomatedNegotiation(cfp, propose, make, model, dealerPrice, round);
            }
        }

        private ACLMessage handleManualNegotiation(
                ACLMessage cfp,
                ACLMessage propose,
                String make,
                String model,
                double dealerPrice,
                int round
        ) {
            // 1. Open the Modal UI (This safely freezes the agent until a button is clicked)
            String input = ManualNegotiationUI.getCounterOffer(
                    getLocalName(),
                    cfp.getSender().getLocalName(),
                    make + " " + model,
                    dealerPrice,
                    maxPrice,
                    round
            );

            // 2. Process the human's choice
            if (input == null || input.equals("REJECT")) {
                propose.setPerformative(ACLMessage.REFUSE);
                AuctionLog.warn(getLocalName(), "Manual rejection of " + cfp.getSender().getLocalName());
                return propose;
            }

            try {
                double humanOffer = Double.parseDouble(input.trim());

                if (humanOffer > maxPrice) {
                    propose.setPerformative(ACLMessage.REFUSE);
                    AuctionLog.warn(getLocalName(), "Manual offer RM" + humanOffer + " exceeds budget RM" + maxPrice);
                    return propose;
                }

                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(make + "," + model + "," + humanOffer + "," + round);

                AuctionLog.info(getLocalName(), "Manual counter RM" + humanOffer + " to " + cfp.getSender().getLocalName());

            } catch (NumberFormatException e) {
                propose.setPerformative(ACLMessage.REFUSE);
                AuctionLog.error(getLocalName(), "Invalid manual input.");
            }

            return propose;
        }

        private ACLMessage handleAutomatedNegotiation(
                ACLMessage cfp,
                ACLMessage propose,
                String make,
                String model,
                double dealerPrice,
                int round
        ) {
            if (dealerPrice <= maxPrice) {
                double previousDealerPrice = previousDealerPrices.getOrDefault(cfp.getSender(), -1.0);
                double myNextOffer;

                if (previousDealerPrice != -1.0 && round > 0) {
                    double concessionAmount = previousDealerPrice - dealerPrice;
                    int remainingRounds = maxRounds - round;

                    double predictedBottomLine = dealerPrice - (concessionAmount * remainingRounds);

                    AuctionLog.info(getLocalName(), "AI Prediction: " + cfp.getSender().getLocalName() + " bottom line ~RM" + predictedBottomLine);

                    myNextOffer = Math.max(predictedBottomLine, initialOffer);

                } else {
                    double stepUp = (maxPrice - initialOffer) / maxRounds;
                    myNextOffer = initialOffer + (stepUp * round);
                }

                if (myNextOffer >= dealerPrice) {
                    myNextOffer = dealerPrice;
                }

                previousDealerPrices.put(cfp.getSender(), dealerPrice);

                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(make + "," + model + "," + myNextOffer + "," + round);

                AuctionLog.info(getLocalName(), "Automated counter RM" + myNextOffer + " to " + cfp.getSender().getLocalName());

            } else {
                propose.setPerformative(ACLMessage.REFUSE);
                AuctionLog.warn(getLocalName(), "Dealer price RM" + dealerPrice + " exceeds budget RM" + maxPrice);
            }

            return propose;
        }

        @Override
        protected ACLMessage handleAcceptProposal(
                ACLMessage cfp,
                ACLMessage propose,
                ACLMessage accept
        ) {
            AuctionLog.info(getLocalName(), "*** SUCCESS! Deal closed with " + accept.getSender().getLocalName() + " for RM" + accept.getContent() + " ***");

            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);

            return inform;
        }

        @Override
        protected void handleRejectProposal(
                ACLMessage cfp,
                ACLMessage propose,
                ACLMessage reject
        ) {
            AuctionLog.info(getLocalName(), reject.getSender().getLocalName() + " rejected our offer.");
        }
    }
}
