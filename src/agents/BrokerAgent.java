package agents;

import analytics.AnalyticsStore;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import logging.AuctionLog;
import models.Car;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BrokerAgent extends Agent {

    private HashMap<AID, List<Car>> dealerCatalogs = new HashMap<>();

    // BROKER COMMISSION & FEES
    private double totalCommissions = 0.0;
    private double totalFees = 0.0;
    private static final double FIXED_FEE = 50.0;
    private static final double COMMISSION_RATE = 0.02; // 2%

    @Override
    protected void setup() {
        AuctionLog.info("Broker (KA)", getAID().getLocalName() + " is ready.");

        // Register the broker service
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("car-brokering");
        sd.setName("JADE-automated-car-trading");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
           fe.printStackTrace();
        }

        // Add behaviors
        addBehaviour(new ReceiveCatalogsBehaviour());
        addBehaviour(new ListenForBuyerRequests());
        addBehaviour(new ForwardBuyerInterests());
        addBehaviour(new CollectCommissions());
        addBehaviour(new ListenForDealerLeadAcceptance());
        addBehaviour(new ListenForDealerRemoval());
    }

    private class ReceiveCatalogsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            
            // Note: Prevent conflicting with buyer-interest or successful-deal INFORMS
            if (msg != null && msg.getConversationId() != null && msg.getConversationId().equals("dealer-catalog")) {
                try {
                    List<Car> receivedCatalog = (List<Car>) msg.getContentObject();
                    dealerCatalogs.put(msg.getSender(), receivedCatalog);
                    AuctionLog.info("Broker (KA)", "Received inventory from " + msg.getSender().getLocalName());
                } catch (UnreadableException e) { e.printStackTrace(); }
            } else { block(); }
        }
    }

    private class ListenForBuyerRequests extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId("broker-match")
            );
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                String requestedCar = msg.getContent(); 
                String[] parts = requestedCar.split(",");
                if(parts.length < 2) return;
                
                String targetMake = parts[0];
                String targetModel = parts[1];

                AuctionLog.info("Broker (KA)", "Request for " + targetMake + " " + targetModel + " from " + msg.getSender().getLocalName());
                ArrayList<AID> matchingDealers = new ArrayList<>();

                for (AID dealerAID : dealerCatalogs.keySet()) {
                    List<Car> cars = dealerCatalogs.get(dealerAID);
                    for (Car car : cars) {
                        if (car.getMake().equalsIgnoreCase(targetMake) && car.getModel().equalsIgnoreCase(targetModel)) {
                            if (!matchingDealers.contains(dealerAID)) matchingDealers.add(dealerAID); 
                        }
                    }
                }

                // Send the list of matching dealers back to the Buyer
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                try {
                    reply.setContentObject(matchingDealers);
                } catch (IOException e) { e.printStackTrace(); }
                myAgent.send(reply);

            } else {
                block();
            }
        }
    }

    private class ForwardBuyerInterests extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("buyer-interest")
            );
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    // Msg content string: targetMake,targetModel,initialOffer,dealer1,dealer2...
                    String[] parts = msg.getContent().split(",");
                    String targetMake = parts[0];
                    String targetModel = parts[1];
                    String initialOffer = parts[2];
                    
                    AuctionLog.info("Broker (KA)", "Forwarding interest from " + msg.getSender().getLocalName() + " to matching dealers.");

                    for (int i = 3; i < parts.length; i++) {
                        String buyerName = msg.getSender().getLocalName();
                        String dealerName = parts[i];
                        String sessionId = dealerName + "-" + buyerName;

                        AID dealer = new AID(dealerName, AID.ISLOCALNAME);

                        // Charge a fixed broker fee for connecting this buyer-dealer negotiation.
                        totalFees += FIXED_FEE;

                        // Send structured analytics data to the GUI.
                        // This updates the Treasury Dashboard in real time.
                        AnalyticsStore.recordNegotiationStarted(
                                sessionId,
                                buyerName,
                                dealerName,
                                FIXED_FEE
                        );

                        ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
                        forward.addReceiver(dealer);
                        forward.setConversationId("broker-lead");

                        // Tell the dealer about the buyer and their offer.
                        forward.setContent(buyerName + "," + targetMake + "," + targetModel + "," + initialOffer);

                        myAgent.send(forward);
                    }
                } catch (Exception e) {}
            } else {
                block();
            }
        }
    }

    private class CollectCommissions extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("successful-deal")
            );
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String content = msg.getContent();

                String sessionId = msg.getSender().getLocalName() + "-unknown";
                double price;

// New format: sessionId,finalPrice
// Old fallback format: finalPrice
                if (content.contains(",")) {
                    String[] parts = content.split(",");
                    sessionId = parts[0];
                    price = Double.parseDouble(parts[1]);
                } else {
                    price = Double.parseDouble(content);
                }

                double commission = price * COMMISSION_RATE;
                totalCommissions += commission;

// Send structured treasury update to the GUI.
                AnalyticsStore.recordSuccessfulDeal(sessionId, price, commission);

                AuctionLog.info(
                        "Broker (KA)",
                        ">>> Collected RM" + commission + " commission from "
                                + msg.getSender().getLocalName() + " <<<"
                );

                AuctionLog.info(
                        "Broker (KA)",
                        "Treasury -> Total Fees: RM" + totalFees
                                + " | Total Commissions: RM" + totalCommissions
                );
            } else {
                block();
            }
        }
    }

    private class ListenForDealerLeadAcceptance extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId("dealer-accepts-lead")
            );
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                AuctionLog.info("Broker (KA)", msg.getSender().getLocalName() + " accepted lead for " + msg.getContent());
            } else {
                block();
            }
        }
    }

    private class ListenForDealerRemoval extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("dealer-remove")
            );

            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                dealerCatalogs.remove(msg.getSender());
                AuctionLog.warn("Broker (KA)", "Removed dealer catalog for " + msg.getSender().getLocalName());
            } else {
                block();
            }
        }
    }
}
