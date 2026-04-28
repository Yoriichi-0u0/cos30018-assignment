package agents;

import analytics.AnalyticsStore;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import logging.AuctionLog;
import models.Car;

import java.util.ArrayList;
import java.util.Vector;

public class DealerAgent extends Agent {

    private ArrayList<Car> catalog;
    private double minProfitMargin = 0.10; // Dealer will not sell below 10% profit margin
    private String strategy = "Stubborn"; // Default strategy
    private AID brokerAID;

    @Override
    protected void setup() {
        catalog = new ArrayList<>();
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof java.util.List) {
                catalog.addAll((java.util.List<Car>) args[0]);
            }
            if (args.length > 1) {
                minProfitMargin = (Double) args[1];
            }
            if (args.length > 2 && args[2] != null) {
                strategy = args[2].toString();
            }
        } else {
            // Fallback for headless testing
            catalog.add(new Car("Toyota", "Camry", 2022, 125000.0));
        }

        AuctionLog.info(getLocalName(), "Ready with strategy: " + strategy);

        addBehaviour(new SendCatalogToBroker());
        addBehaviour(new ListenForBrokerLeads());
    }

    private class ListenForBrokerLeads extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId("broker-lead")
            );
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                // Robust broker validation
                if (brokerAID == null) {
                    try {
                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("car-brokering");
                        template.addServices(sd);
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        if (result.length > 0) brokerAID = result[0].getName();
                    } catch (Exception e) {}
                }

                if (brokerAID != null && msg.getSender().equals(brokerAID)) {
                    String[] parts = msg.getContent().split(",");
                    String buyerName = parts[0];
                    String make = parts[1];
                    String model = parts[2];
                    double buyerInitialOffer = Double.parseDouble(parts[3]);

                    Car car = findCar(make, model);
                    if (car != null) {
                        double minAcceptablePrice = car.getPrice() * (1 - minProfitMargin);

                        if (buyerInitialOffer >= (minAcceptablePrice * 0.8)) {
                            AuctionLog.info(getLocalName(), "Lead from " + buyerName + " (RM" + buyerInitialOffer + ") reasonable. Engaging (" + strategy + ").");
                            
                            ACLMessage acceptLead = new ACLMessage(ACLMessage.INFORM);
                            acceptLead.addReceiver(brokerAID);
                            acceptLead.setConversationId("dealer-accepts-lead");
                            acceptLead.setContent(buyerName);
                            myAgent.send(acceptLead);

                            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                            cfp.addReceiver(new AID(buyerName, AID.ISLOCALNAME));
                            cfp.setProtocol(jade.domain.FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                            cfp.setConversationId("nego-" + buyerName + "-" + System.currentTimeMillis());
                            
                            // Dealer starts first: offer at List Price
                            cfp.setContent(make + "," + model + "," + car.getPrice() + ",0"); // Make,Model,Price,Round

                            String sessionId = getLocalName() + "-" + buyerName;

                            // Record round 0 so the graph starts with the buyer's first offer
                            // and the dealer's first asking price.
                            AnalyticsStore.recordRound(
                                    sessionId,
                                    buyerName,
                                    getLocalName(),
                                    0,
                                    buyerInitialOffer,
                                    car.getPrice(),
                                    "STARTED"
                            );

                            myAgent.addBehaviour(new DealerNegotiator(
                                    myAgent,
                                    cfp,
                                    car.getPrice(),
                                    0,
                                    buyerName,
                                    make,
                                    model,
                                    car.getPrice(),
                                    buyerInitialOffer,
                                    sessionId
                            ));
                        } else {
                            AuctionLog.warn(getLocalName(), "Lead from " + buyerName + " (RM" + buyerInitialOffer + ") too low. Ignoring.");
                        }
                    }
                }
            } else {
                block();
            }
        }
    }

    private class DealerNegotiator extends ContractNetInitiator {
        private double listPrice;
        private int round;
        private String buyerName;
        private String make;
        private String model;
        private double lastDealerOffer;
        private double lastBuyerOffer;
        private String sessionId;

        public DealerNegotiator(
                Agent a,
                ACLMessage cfp,
                double listPrice,
                int round,
                String buyerName,
                String make,
                String model,
                double lastDealerOffer,
                double lastBuyerOffer,
                String sessionId
        ) {
            super(a, cfp);
            this.listPrice = listPrice;
            this.round = round;
            this.buyerName = buyerName;
            this.make = make;
            this.model = model;
            this.lastDealerOffer = lastDealerOffer;
            this.lastBuyerOffer = lastBuyerOffer;
            this.sessionId = sessionId;
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            double minAcceptablePrice = listPrice * (1 - minProfitMargin);

            for (Object obj : responses) {
                ACLMessage msg = (ACLMessage) obj;
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    // Msg format from buyer: Make,Model,OfferPrice,Round
                    String[] parts = msg.getContent().split(",");
                    double currentBuyerOffer = Double.parseDouble(parts[2]);
                    
                    ACLMessage reply = msg.createReply();
                    
                    if (currentBuyerOffer >= minAcceptablePrice) {
                        // Accept
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent(String.valueOf(currentBuyerOffer));
                        AuctionLog.info(getLocalName(), "ACCEPTED " + buyerName + " offer RM" + currentBuyerOffer);

                        // Tell Broker to collect commission
                        if (brokerAID != null) {
                            ACLMessage informBroker = new ACLMessage(ACLMessage.INFORM);
                            informBroker.addReceiver(brokerAID);
                            informBroker.setConversationId("successful-deal");
                            // Send both session ID and final price to the broker.
                            // Format: sessionId,finalPrice
                            informBroker.setContent(sessionId + "," + currentBuyerOffer);
                            myAgent.send(informBroker);
                        }

                    } else if (round < 5) {
                        // Iterated Contract Net
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reply.setContent("COUNTER");
                        
                        double nextDealerPrice;
                        if (strategy.equalsIgnoreCase("Stubborn")) {
                            nextDealerPrice = lastDealerOffer - (listPrice * 0.01);
                        } else if (strategy.equalsIgnoreCase("Desperate")) {
                            nextDealerPrice = lastDealerOffer - (listPrice * 0.06);
                        } else if (strategy.equalsIgnoreCase("Matcher")) {
                            double buyerIncrement = currentBuyerOffer - lastBuyerOffer;
                            if (buyerIncrement <= 0) buyerIncrement = listPrice * 0.02;
                            nextDealerPrice = lastDealerOffer - buyerIncrement;
                        } else {
                            // Default / Fallback
                            nextDealerPrice = lastDealerOffer - (listPrice * 0.04);
                        }
                        
                        nextDealerPrice = Math.max(nextDealerPrice, minAcceptablePrice);
                        
                        ACLMessage newCfp = new ACLMessage(ACLMessage.CFP);
                        newCfp.addReceiver(msg.getSender());
                        newCfp.setProtocol(jade.domain.FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                        newCfp.setConversationId("nego-" + buyerName + "-" + System.currentTimeMillis());
                        newCfp.setContent(make + "," + model + "," + nextDealerPrice + "," + (round + 1));

                        // Record the dealer's next counter-offer.
                        // This shows the gap narrowing on the chart.
                        AnalyticsStore.recordRound(
                                sessionId,
                                buyerName,
                                myAgent.getLocalName(),
                                round + 1,
                                currentBuyerOffer,
                                nextDealerPrice,
                                "DEALER_COUNTERED"
                        );

                        myAgent.addBehaviour(new DealerNegotiator(
                                myAgent,
                                newCfp,
                                listPrice,
                                round + 1,
                                buyerName,
                                make,
                                model,
                                nextDealerPrice,
                                currentBuyerOffer,
                                sessionId
                        ));

                        AuctionLog.info(getLocalName(), "Countering RM" + nextDealerPrice + " (Round " + (round + 1) + ")");

                        // Record the buyer's proposal against the dealer's current ask.
                        // This is used by the real-time negotiation line chart.
                        AnalyticsStore.recordRound(
                                sessionId,
                                buyerName,
                                myAgent.getLocalName(),
                                round,
                                currentBuyerOffer,
                                lastDealerOffer,
                                "BUYER_PROPOSED"
                        );
                    } else {
                        // Max rounds
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reply.setContent("WALKAWAY");
                        AuctionLog.warn(getLocalName(), "Max rounds reached. Walking away from " + buyerName);
                        AnalyticsStore.recordFailedDeal(sessionId);
                    }
                    acceptances.add(reply);
                }
                else if (msg.getPerformative() == ACLMessage.REFUSE) {
                    AnalyticsStore.recordFailedDeal(sessionId);
                    AuctionLog.warn(getLocalName(), buyerName + " refused the negotiation.");
                }
            }
        }
    }

    private class SendCatalogToBroker extends OneShotBehaviour {
        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("car-brokering");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    brokerAID = result[0].getName();
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(brokerAID);
                    msg.setConversationId("dealer-catalog"); 
                    msg.setContentObject(catalog);
                    myAgent.send(msg);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private Car findCar(String make, String model) {
        for (Car car : catalog) if (car.getMake().equalsIgnoreCase(make) && car.getModel().equalsIgnoreCase(model)) return car;
        return null;
    }

    @Override
    protected void takeDown() {
        if (brokerAID != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(brokerAID);
            msg.setConversationId("dealer-remove");
            msg.setContent(getLocalName());
            send(msg);
        }

        AuctionLog.warn(getLocalName(), "Dealer agent is leaving the auction floor.");

        try {
            DFService.deregister(this);
        } catch (Exception ignored) {
        }
    }
}
