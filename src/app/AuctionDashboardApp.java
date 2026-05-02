package app;

import gui.AuctionDashboard;

import javax.swing.SwingUtilities;

public class AuctionDashboardApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AuctionDashboard.installCrossPlatformLookAndFeel();
                new AuctionDashboard().setVisible(true);
            }
        });
    }
}
