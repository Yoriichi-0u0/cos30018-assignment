package models;

public class DealerListing {

    private final String agentName;
    private final Car car;
    private final double minProfitMargin;

    public DealerListing(String agentName, Car car, double minProfitMargin) {
        this.agentName = agentName;
        this.car = car;
        this.minProfitMargin = minProfitMargin;
    }

    public String getAgentName() {
        return agentName;
    }

    public Car getCar() {
        return car;
    }

    public double getMinProfitMargin() {
        return minProfitMargin;
    }

    public double getFloorPrice() {
        return car.getPrice() * (1 - minProfitMargin);
    }
}
