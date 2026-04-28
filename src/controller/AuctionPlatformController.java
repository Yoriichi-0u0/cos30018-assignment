package controller;

import agents.BrokerAgent;
import agents.BuyerAgent;
import agents.DealerAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import logging.AuctionLog;
import models.BuyerProfile;
import models.Car;
import models.DealerListing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuctionPlatformController {

    private static final String DEFAULT_BROKER_NAME = "auctionBroker";

    private Runtime runtime;
    private AgentContainer mainContainer;
    private String brokerName;

    private final Map<String, AgentController> activeAgents = new LinkedHashMap<String, AgentController>();
    private final Map<String, DealerListing> dealerListings = new LinkedHashMap<String, DealerListing>();
    private final Map<String, BuyerProfile> buyerProfiles = new LinkedHashMap<String, BuyerProfile>();

    public synchronized void ensurePlatformStarted() throws StaleProxyException {
        if (mainContainer != null) {
            return;
        }

        runtime = Runtime.instance();
        runtime.setCloseVM(false);

        // Load config for GUI toggle
        boolean useJadeGui = true;
        java.util.Properties props = new java.util.Properties();
        try (java.io.InputStream in = new java.io.FileInputStream("config.properties")) {
            props.load(in);
            useJadeGui = Boolean.parseBoolean(props.getProperty("jade_gui", "true"));
        } catch (Exception e) {
            // Default to true if file missing
        }

        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN, Boolean.TRUE.toString());
        profile.setParameter(Profile.GUI, String.valueOf(useJadeGui));
        profile.setParameter(Profile.LOCAL_HOST, "127.0.0.1");
        profile.setParameter(Profile.LOCAL_PORT, "1099");

        mainContainer = runtime.createMainContainer(profile);
        AuctionLog.info("System", "JADE main container started on 127.0.0.1:1099.");
        launchBroker(DEFAULT_BROKER_NAME);
    }

    public synchronized boolean isRunning() {
        return mainContainer != null;
    }

    public synchronized String launchDealer(String preferredName, Car car, double minProfitMargin, String strategy) throws StaleProxyException {
        ensurePlatformStarted();
        String agentName = uniqueName(preferredName, "dealer");

        List<Car> inventory = new ArrayList<Car>();
        inventory.add(car);
        AgentController controller = mainContainer.createNewAgent(
                agentName,
                DealerAgent.class.getName(),
                new Object[]{inventory, minProfitMargin, strategy}
        );
        controller.start();
        activeAgents.put(agentName, controller);
        dealerListings.put(agentName, new DealerListing(agentName, car, minProfitMargin));
        AuctionLog.info("System", "Dealer " + agentName + " [" + strategy + "] entered the auction hall with " + car + ".");
        return agentName;
    }

    public synchronized String launchBuyer(String preferredName, String make, String model, double initialOffer, double maxPrice, int maxRounds, boolean isManual) throws StaleProxyException {
        ensurePlatformStarted();
        String agentName = uniqueName(preferredName, "buyer");

        AgentController controller = mainContainer.createNewAgent(
                agentName,
                BuyerAgent.class.getName(),
                new Object[]{make, model, initialOffer, maxPrice, maxRounds, isManual}
        );
        controller.start();
        activeAgents.put(agentName, controller);
        buyerProfiles.put(agentName, new BuyerProfile(agentName, make, model, initialOffer, maxPrice, maxRounds));
        AuctionLog.info("System", "Buyer " + agentName + " joined the floor for " + make + " " + model + ".");
        return agentName;
    }

    public void launchDemoScenario() throws Exception {
        ensurePlatformStarted();

        launchDealer("dealerNorthStar", new Car("Toyota", "Camry", 2023, 128000.0), 0.09, "Stubborn");
        launchDealer("dealerSummit", new Car("Honda", "Civic", 2022, 115000.0), 0.12, "Desperate");
        launchDealer("dealerLegacy", new Car("Toyota", "Camry", 2023, 130000.0), 0.08, "Matcher");
        Thread.sleep(350L);
        launchDealer("dealerApex", new Car("Toyota", "Camry", 2022, 122500.0), 0.10, "Stubborn");
        Thread.sleep(350L);
        launchDealer("dealerRedline", new Car("Honda", "Civic", 2021, 116000.0), 0.08, "Desperate");
        Thread.sleep(900L);

        launchBuyer("buyerMia", "Toyota", "Camry", 108000.0, 131000.0, 5, false);
        Thread.sleep(250L);
        launchBuyer("buyerNoah", "Toyota", "Camry", 111500.0, 125000.0, 5, false);
        Thread.sleep(250L);
        launchBuyer("buyerIris", "Honda", "Civic", 103000.0, 117000.0, 4, false);
        Thread.sleep(250L);
        launchBuyer("buyerTheo", "Toyota", "Camry", 109000.0, 121500.0, 4, false);
        Thread.sleep(250L);
        launchBuyer("buyerJune", "Ford", "Ranger", 145000.0, 157000.0, 3, false);
    }

    public synchronized void shutdownPlatform() {
        ArrayList<String> names = new ArrayList<String>(activeAgents.keySet());
        for (String agentName : names) {
            AgentController controller = activeAgents.get(agentName);
            try {
                controller.kill();
            } catch (Exception ignored) {
            }
        }

        activeAgents.clear();
        dealerListings.clear();
        buyerProfiles.clear();
        brokerName = null;

        if (mainContainer != null) {
            try {
                mainContainer.kill();
            } catch (Exception ignored) {
            }
            mainContainer = null;
        }

        if (runtime != null) {
            runtime.shutDown();
            runtime = null;
        }

        AuctionLog.info("System", "Platform shut down and all agents were cleared.");
    }

    public synchronized int getBrokerCount() {
        return brokerName == null ? 0 : 1;
    }

    public synchronized int getDealerCount() {
        return dealerListings.size();
    }

    public synchronized int getBuyerCount() {
        return buyerProfiles.size();
    }

    public synchronized List<DealerListing> getDealerListings() {
        return new ArrayList<DealerListing>(dealerListings.values());
    }

    public synchronized List<BuyerProfile> getBuyerProfiles() {
        return new ArrayList<BuyerProfile>(buyerProfiles.values());
    }

    private void launchBroker(String preferredName) throws StaleProxyException {
        String agentName = uniqueName(preferredName, "broker");
        AgentController controller = mainContainer.createNewAgent(agentName, BrokerAgent.class.getName(), new Object[0]);
        controller.start();
        activeAgents.put(agentName, controller);
        brokerName = agentName;
        AuctionLog.info("System", "Broker " + brokerName + " is now managing the auction floor.");
    }

    private String uniqueName(String preferredName, String fallbackPrefix) {
        String baseName = preferredName == null || preferredName.trim().isEmpty() ? fallbackPrefix : preferredName.trim();
        String candidate = baseName;
        int suffix = 2;
        while (activeAgents.containsKey(candidate)) {
            candidate = baseName + suffix;
            suffix++;
        }
        return candidate;
    }
}
