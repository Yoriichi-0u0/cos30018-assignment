package gui;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import logging.AuctionLog;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MainDashboard extends JFrame {

    private AgentContainer mainContainer;
    private DefaultListModel<String> logModel;
    private JList<String> logList;
    private AtomicInteger dealerCount = new AtomicInteger(1);
    private AtomicInteger buyerCount = new AtomicInteger(1);

    public MainDashboard() {
        setTitle("Automated Auto Auction Platform");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Register with AuctionLog
        AuctionLog.addListener(event -> {
            log(event.getSource(), event.getMessage());
        });

        // Dark Theme Colors
        Color bgDark = new Color(30, 30, 35);
        getContentPane().setBackground(bgDark);

        // --- Top Panel: Controls ---
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(bgDark);
        
        JButton btnStartJade = new JButton("1. Start JADE Platform");
        JButton btnSpawnBroker = new JButton("2. Spawn Broker");
        JButton btnSpawnDealer = new JButton("3. Spawn Dealer");
        JButton btnSpawnBuyer = new JButton("4. Spawn Buyer (Auto)");
        JButton btnSpawnBuyerManual = new JButton("5. Spawn Buyer (Manual)");

        controlPanel.add(btnStartJade);
        controlPanel.add(btnSpawnBroker);
        controlPanel.add(btnSpawnDealer);
        controlPanel.add(btnSpawnBuyer);
        controlPanel.add(btnSpawnBuyerManual);
        add(controlPanel, BorderLayout.NORTH);

        // --- Center Panel: Live Log & Analytics ---
        logModel = new DefaultListModel<>();
        logList = new JList<>(logModel);
        logList.setBackground(new Color(40, 40, 45));
        logList.setForeground(new Color(150, 250, 150));
        logList.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        JScrollPane logScrollPane = new JScrollPane(logList);
        logScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 70)), "Live Auction Feed", 
                0, 0, null, new Color(150, 150, 160)));

        VisualAnalyticsPanel analyticsPanel = new VisualAnalyticsPanel();
        analytics.AnalyticsStore.uiPanel = analyticsPanel;
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logScrollPane, analyticsPanel);
        splitPane.setDividerLocation(300);
        splitPane.setBackground(bgDark);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        btnStartJade.addActionListener(e -> startJadePlatform());
        
        btnSpawnBroker.addActionListener(e -> {
            spawnAgent("Broker", "agents.BrokerAgent", new Object[]{});
            btnSpawnBroker.setEnabled(false); // Only 1 broker allowed per rubric
        });
        
        btnSpawnDealer.addActionListener(e -> {
            String name = "Dealer_" + dealerCount.getAndIncrement();
            // Passing: Make, Model, Year, Price, Margin, Strategy
            Object[] args = {"Toyota", "Camry", 2025, 125000.0, 0.10, "Linear"};
            spawnAgent(name, "agents.DealerAgent", args);
        });

        btnSpawnBuyer.addActionListener(e -> {
            String name = "Buyer_Auto_" + buyerCount.getAndIncrement();
            // Passing: Target Make, Target Model, Initial Offer, Max Budget, Max Rounds, isManual
            Object[] args = {"Toyota", "Camry", 100000.0, 115000.0, 5, "false"};
            spawnAgent(name, "agents.BuyerAgent", args);
        });

        btnSpawnBuyerManual.addActionListener(e -> {
            String name = "Buyer_Manual_" + buyerCount.getAndIncrement();
            Object[] args = {"Toyota", "Camry", 100000.0, 115000.0, 5, "true"};
            spawnAgent(name, "agents.BuyerAgent", args);
        });
    }

    private void startJadePlatform() {
        if (mainContainer == null) {
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            p.setParameter(Profile.MAIN_HOST, "localhost");
            p.setParameter(Profile.MAIN_PORT, "1099");
            p.setParameter(Profile.GUI, "false"); 
            mainContainer = rt.createMainContainer(p);
            log("System", "JADE Platform Started Successfully.");
        }
    }

    private void spawnAgent(String name, String className, Object[] args) {
        if (mainContainer != null) {
            try {
                AgentController agent = mainContainer.createNewAgent(name, className, args);
                agent.start();
                log("System", "Spawned Agent: " + name);
            } catch (Exception ex) {
                log("ERROR", "Failed to spawn " + name + ": " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please start JADE Platform first!");
        }
    }

    public void log(String source, String message) {
        SwingUtilities.invokeLater(() -> {
            logModel.addElement(String.format("[%tT] [%s] %s", System.currentTimeMillis(), source, message));
            int lastIndex = logModel.getSize() - 1;
            if (lastIndex >= 0) {
                logList.ensureIndexIsVisible(lastIndex);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}
