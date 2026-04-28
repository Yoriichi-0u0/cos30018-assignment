package app;

import gui.AuctionDashboard;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AuctionDashboardApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }

                new AuctionDashboard().setVisible(true);
            }
        });
    }
}
