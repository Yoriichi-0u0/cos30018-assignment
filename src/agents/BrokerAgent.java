package agents;

import gui.MainDashboardFX;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import ontology.AuctionOntology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrokerAgent extends Agent {
    public static BrokerAgent instance;

    private final Map<String, List<Double>> marketData = new HashMap<>();
    private final Map<AID, String> inventoryCatalog = new HashMap<>();

    public static final Set<String> securedBuyers = new HashSet<>();

    private final double commissionRate = 0.02;
    private final double negotiationFee = 50.0;

    private int negotiationsCount = 0;
    private double totalCommission = 0.0;

    @Override
    protected void setup() {
        instance = this;

        marketData.clear();
        inventoryCatalog.clear();
        securedBuyers.clear();

        Codec codec = new SLCodec();
        Ontology auctionOntology = AuctionOntology.getInstance();

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(auctionOntology);

        registerWithDF();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();

                if (msg == null) {
                    block();
                    return;
                }

                String ontology = msg.getOntology();

                if ("register-inventory".equals(ontology)) {
                    handleRegisterInventory(msg);
                } else if ("find-dealers".equals(ontology)) {
                    handleFindDealers(msg);
                } else if ("shortlist".equals(ontology)) {
                    handleShortlist(msg);
                } else if ("buyer-approved".equals(ontology)) {
                    log("BROKER", "Dealer approved buyer: " + msg.getContent());
                } else if ("deal-closed".equals(ontology)) {
                    handleDealClosed(msg);
                }
            }
        });
    }

    private void handleRegisterInventory(ACLMessage msg) {
        inventoryCatalog.put(msg.getSender(), msg.getContent());

        String[] parts = msg.getContent().split(",");

        String carName = parts.length > 0 ? parts[0] : "Unknown vehicle";
        String price = parts.length > 1 ? parts[1] : "0";

        log("BROKER", "Cataloged " + msg.getSender().getLocalName()
                + " selling " + carName
                + " at RM " + price);
    }

    private void handleFindDealers(ACLMessage msg) {
        BuyerRequest request = parseBuyerRequest(msg.getContent());

        List<String> shortlist = new ArrayList<>();
        List<String> budgetAlternatives = new ArrayList<>();

        log("BROKER", String.format(
                "%s requested %s with budget RM%,.2f.",
                msg.getSender().getLocalName(),
                request.targetCar,
                request.budget
        ));

        for (Map.Entry<AID, String> entry : inventoryCatalog.entrySet()) {
            String[] parts = entry.getValue().split(",");

            if (parts.length < 2) {
                continue;
            }

            String dealerCar = parts[0];
            double dealerAskingPrice = parseDouble(parts[1], 0.0);

            boolean withinNegotiationRange = dealerAskingPrice <= request.budget + 10000;

            if (withinNegotiationRange && carMatches(request.targetCar, dealerCar)) {
                shortlist.add(entry.getKey().getLocalName());

                if (shortlist.size() >= 3) {
                    break;
                }
            } else if (withinNegotiationRange && isReasonableAlternative(request.targetCar, dealerCar)) {
                budgetAlternatives.add(entry.getKey().getLocalName());
            }
        }

        boolean usedAlternativeMatches = false;

        if (shortlist.isEmpty() && !budgetAlternatives.isEmpty()) {
            usedAlternativeMatches = true;

            for (String dealerName : budgetAlternatives) {
                shortlist.add(dealerName);

                if (shortlist.size() >= 3) {
                    break;
                }
            }
        }

        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setOntology("search-results");

        if (shortlist.isEmpty()) {
            reply.setContent("NONE");

            log("BROKER", String.format(
                    "No dealer matched %s within RM%,.2f budget.",
                    request.targetCar,
                    request.budget
            ));
        } else {
            reply.setContent(String.join(",", shortlist));

            if (usedAlternativeMatches) {
                log("BROKER", String.format(
                        "No exact listing for %s. Offered %d budget-fit alternative dealer(s).",
                        request.targetCar,
                        shortlist.size()
                ));
            } else {
                log("BROKER", String.format(
                        "Matched %d dealer(s) for %s within RM%,.2f budget.",
                        shortlist.size(),
                        request.targetCar,
                        request.budget
                ));
            }
        }

        send(reply);
    }

    private void handleShortlist(ACLMessage msg) {
        String[] dealerOffers = msg.getContent().split(";");

        for (String data : dealerOffers) {
            if (!data.contains(":")) {
                continue;
            }

            String[] parts = data.split(":", 2);

            String dealerName = parts[0];
            String offerData = parts[1];

            ACLMessage engageMsg = new ACLMessage(ACLMessage.REQUEST);
            engageMsg.addReceiver(new AID(dealerName, AID.ISLOCALNAME));
            engageMsg.setOntology("evaluate-buyer");
            engageMsg.setContent(msg.getSender().getLocalName() + ":" + offerData);

            send(engageMsg);

            negotiationsCount++;
        }
    }

    private void handleDealClosed(ACLMessage msg) {
        String[] parts = msg.getContent().split(",");

        if (parts.length < 2) {
            log("BROKER", "Invalid deal-closed message: " + msg.getContent());
            return;
        }

        String closedCar = parts[0];
        double finalPrice = parseDouble(parts[1], 0.0);

        marketData.putIfAbsent(closedCar, new ArrayList<>());
        marketData.get(closedCar).add(finalPrice);

        totalCommission += finalPrice * commissionRate;

        log("BROKER", String.format(
                "Deal closed for a %s at RM%,.2f",
                closedCar,
                finalPrice
        ));

        if (MainDashboardFX.getInstance() != null) {
            MainDashboardFX.getInstance().updateMarketChart(marketData);
        }
    }

    private void registerWithDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setType("car-broker");
            sd.setName("JADE-Auto-Auction");

            dfd.addServices(sd);

            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generateReport() {
        StringBuilder report = new StringBuilder();

        report.append("\n📊 === BROKER MARKET ANALYTICS REPORT === 📊\n");

        if (marketData.isEmpty()) {
            report.append("No deals have been closed yet.\n");
        } else {
            for (Map.Entry<String, List<Double>> entry : marketData.entrySet()) {
                String car = entry.getKey();
                List<Double> prices = entry.getValue();

                double total = 0.0;

                for (double price : prices) {
                    total += price;
                }

                double average = prices.isEmpty() ? 0.0 : total / prices.size();

                report.append(String.format(
                        " > %s | Units Sold: %d | Avg Selling Price: RM %,.2f\n",
                        car,
                        prices.size(),
                        average
                ));
            }
        }

        double totalFees = negotiationsCount * negotiationFee;
        double totalBrokerEarnings = totalFees + totalCommission;

        report.append(String.format("\nTotal Negotiations Facilitated: %d", negotiationsCount));
        report.append(String.format("\nTotal Fees Collected: RM %,.2f", totalFees));
        report.append(String.format("\nTotal Commission Earned: RM %,.2f", totalCommission));
        report.append(String.format("\nTotal Broker Earnings: RM %,.2f", totalBrokerEarnings));
        report.append("\n=============================================");

        String output = report.toString();

        log("BROKER", output);

        return output;
    }

    private BuyerRequest parseBuyerRequest(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new BuyerRequest("Any vehicle", 0.0);
        }

        if (content.contains("|")) {
            String[] parts = content.split("\\|", 2);

            String targetCar = parts[0].trim();
            double budget = parseDouble(parts[1], 0.0);

            return new BuyerRequest(targetCar, budget);
        }

        double budgetOnly = parseDouble(content, 0.0);

        return new BuyerRequest("Any vehicle", budgetOnly);
    }

    private boolean carMatches(String requestedCar, String dealerCar) {
        String request = normalize(requestCarSafe(requestedCar));
        String listing = normalize(dealerCar);

        if (request.isEmpty() || "any vehicle".equals(request)) {
            return true;
        }

        return listing.contains(request)
                || request.contains(listing)
                || sharedKeywords(request, listing) >= 3;
    }

    private String requestCarSafe(String requestedCar) {
        return requestedCar == null ? "" : requestedCar;
    }

    private boolean isReasonableAlternative(String requestedCar, String dealerCar) {
        String request = normalize(requestedCar);
        String listing = normalize(dealerCar);

        if (request.isEmpty() || "any vehicle".equals(request)) {
            return true;
        }

        String[] requestTokens = request.split(" ");
        String[] listingTokens = listing.split(" ");

        if (requestTokens.length > 1 && listingTokens.length > 1) {
            return requestTokens[1].equals(listingTokens[1]);
        }

        return true;
    }

    private int sharedKeywords(String request, String listing) {
        int matches = 0;

        for (String token : request.split(" ")) {
            if (token.length() > 1 && listing.contains(token)) {
                matches++;
            }
        }

        return matches;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void log(String source, String message) {
        if (MainDashboardFX.getInstance() != null) {
            MainDashboardFX.getInstance().log(source, message);
        } else {
            System.out.println("[" + source + "] " + message);
        }
    }

    private static final class BuyerRequest {
        private final String targetCar;
        private final double budget;

        private BuyerRequest(String targetCar, double budget) {
            if (targetCar == null || targetCar.trim().isEmpty()) {
                this.targetCar = "Any vehicle";
            } else {
                this.targetCar = targetCar.trim();
            }

            this.budget = budget;
        }
    }
}
