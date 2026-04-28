package models;

public class BuyerProfile {

    private final String agentName;
    private final String targetMake;
    private final String targetModel;
    private final double initialOffer;
    private final double maxPrice;
    private final int maxNegotiationRounds;

    public BuyerProfile(String agentName, String targetMake, String targetModel, double initialOffer, double maxPrice, int maxNegotiationRounds) {
        this.agentName = agentName;
        this.targetMake = targetMake;
        this.targetModel = targetModel;
        this.initialOffer = initialOffer;
        this.maxPrice = maxPrice;
        this.maxNegotiationRounds = maxNegotiationRounds;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getTargetMake() {
        return targetMake;
    }

    public String getTargetModel() {
        return targetModel;
    }

    public double getInitialOffer() {
        return initialOffer;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public int getMaxNegotiationRounds() {
        return maxNegotiationRounds;
    }

    public String getTargetLabel() {
        return targetMake + " " + targetModel;
    }
}
