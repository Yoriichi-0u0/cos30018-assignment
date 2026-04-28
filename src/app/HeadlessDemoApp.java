package app;

import controller.AuctionPlatformController;

public class HeadlessDemoApp {

    public static void main(String[] args) throws Exception {
        AuctionPlatformController controller = new AuctionPlatformController();
        controller.launchDemoScenario();
        Thread.sleep(12000L);
        controller.shutdownPlatform();
    }
}
