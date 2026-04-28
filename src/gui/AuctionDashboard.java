package gui;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AuctionDashboard extends JFrame {
    private static final long serialVersionUID = 1L;

    /*
     * Change these only if your agent class names are different.
     */
    private static final String BROKER_AGENT_CLASS = "agents.BrokerAgent";
    private static final String DEALER_AGENT_CLASS = "agents.DealerAgent";
    private static final String BUYER_AGENT_CLASS = "agents.BuyerAgent";

    private static final String TAB_OVERVIEW = "Overview";
    private static final String TAB_SETUP = "Setup Agents";
    private static final String TAB_MARKET = "Market Boards";
    private static final String TAB_ANALYTICS = "Visual Analytics";
    private static final String TAB_LIVE_FEED = "Live Feed";

    private static final int LAYOUT_WIDE = 0;
    private static final int LAYOUT_MEDIUM = 1;
    private static final int LAYOUT_COMPACT = 2;

    private static final int MIN_PAGE_WIDTH = 980;
    private static final int CONTENT_AREA_HEIGHT = 690;

    private static final int HEADER_HEIGHT = 220;
    private static final int SUMMARY_HEIGHT = 130;
    private static final int NAV_HEIGHT = 90;

    private static final int BUTTON_HEIGHT = 46;
    private static final int LARGE_BUTTON_HEIGHT = 58;

    private static final int BASE_FONT_SIZE = 12;

    private static final Color APP_BG = new Color(17, 5, 10);
    private static final Color PANEL_BG = new Color(22, 24, 32);
    private static final Color CARD_BG = new Color(26, 29, 38);
    private static final Color INPUT_BG = new Color(32, 38, 48);
    private static final Color TABLE_BG = new Color(31, 37, 47);
    private static final Color TABLE_HEADER_BG = new Color(43, 47, 57);
    private static final Color TEXT_MAIN = new Color(244, 239, 224);
    private static final Color TEXT_MUTED = new Color(196, 188, 170);
    private static final Color ACCENT = new Color(218, 171, 57);
    private static final Color ACCENT_DARK = new Color(128, 91, 20);
    private static final Color BUTTON_DARK = new Color(31, 39, 50);
    private static final Color BUTTON_BLUE = new Color(67, 83, 99);
    private static final Color BUTTON_RED = new Color(168, 40, 52);
    private static final Color GREEN = new Color(88, 190, 132);
    private static final Color RED = new Color(230, 88, 92);
    private static final Color GRID_LINE = new Color(61, 68, 82);

    private final Font bodyFont = new Font("SansSerif", Font.PLAIN, BASE_FONT_SIZE);
    private final Font bodyBoldFont = new Font("SansSerif", Font.BOLD, BASE_FONT_SIZE);
    private final Font smallBoldFont = new Font("SansSerif", Font.BOLD, 11);
    private final Font titleFont = new Font("Serif", Font.BOLD, 31);
    private final Font sectionFont = new Font("Serif", Font.BOLD, 22);
    private final Font metricFont = new Font("Serif", Font.BOLD, 22);
    private final Font monoFont = new Font("Monospaced", Font.PLAIN, 11);

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    private DashboardPanel mainPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;

    private JPanel overviewPanel;
    private JPanel setupAgentsPanel;
    private JPanel marketBoardsPanel;
    private JPanel visualAnalyticsPanel;
    private JPanel liveFeedPanel;

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private JLabel brokerCountValue;
    private JLabel dealerCountValue;
    private JLabel buyerCountValue;
    private JLabel platformStatusValue;

    private JLabel negotiationsValue;
    private JLabel successfulValue;
    private JLabel failedValue;
    private JLabel successRateValue;
    private JLabel fixedFeesValue;
    private JLabel commissionsValue;
    private JLabel revenueValue;
    private JLabel latestGapValue;

    private JTextField dealerAgentNameField;
    private JTextField dealerMakeField;
    private JTextField dealerModelField;
    private JTextField dealerYearField;
    private JTextField dealerListPriceField;
    private JTextField dealerMinMarginField;
    private JComboBox<String> dealerStrategyComboBox;

    private JTextField buyerAgentNameField;
    private JTextField buyerMakeField;
    private JTextField buyerModelField;
    private JTextField buyerOpeningBidField;
    private JTextField buyerMaxBudgetField;
    private JTextField buyerMaxRoundsField;
    private JCheckBox manualNegotiationCheckBox;

    private JComboBox<String> deleteAgentComboBox;

    private JButton startFloorButton;
    private JButton runDemoButton;
    private JButton resetButton;
    private JButton deleteAgentButton;
    private JButton listVehicleButton;
    private JButton startBuyerButton;

    private DefaultTableModel dealerTableModel;
    private DefaultTableModel buyerTableModel;

    private JTextPane feedPane;

    private NegotiationGapChart negotiationGapChart;
    private TreasuryChart treasuryChart;

    private ContainerController mainContainer;
    private AgentController brokerController;

    private boolean platformLive = false;

    private final Map<String, AgentController> agentControllers = new LinkedHashMap<>();
    private final Map<String, String> agentTypes = new LinkedHashMap<>();

    private final java.util.List<DealerListing> dealerListings = new ArrayList<>();
    private final java.util.List<BuyerRequest> buyerRequests = new ArrayList<>();
    private final java.util.List<Double> buyerOfferHistory = new ArrayList<>();
    private final java.util.List<Double> dealerAskHistory = new ArrayList<>();

    private int negotiations = 0;
    private int successfulDeals = 0;
    private int failedDeals = 0;
    private double fixedFees = 0.0;
    private double commissions = 0.0;
    private double latestGap = 0.0;

    public AuctionDashboard() {
        super("Velvet Hammer Auto Auction");

        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        installLookAndFeelTweaks();
        buildUi();
        wireActions();

        appendFeed("Dashboard", "Auction dashboard is ready. Start the floor or launch the demo lineup.");
        updateDashboardStats();
        updateDeleteAgentComboBox();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshResponsiveLayout();
            }
        });

        SwingUtilities.invokeLater(this::refreshResponsiveLayout);
    }

    private void installLookAndFeelTweaks() {
        try {
            UIManager.put("ComboBox.background", INPUT_BG);
            UIManager.put("ComboBox.foreground", TEXT_MAIN);
            UIManager.put("ComboBox.selectionBackground", BUTTON_DARK);
            UIManager.put("ComboBox.selectionForeground", TEXT_MAIN);
            UIManager.put("TextField.background", INPUT_BG);
            UIManager.put("TextField.foreground", TEXT_MAIN);
            UIManager.put("TextField.caretForeground", TEXT_MAIN);
            UIManager.put("CheckBox.background", CARD_BG);
            UIManager.put("CheckBox.foreground", TEXT_MAIN);
        } catch (Exception ignored) {
            /*
             * Safe to ignore look and feel tweaks.
             */
        }
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 820));
        setPreferredSize(new Dimension(1280, 920));

        mainPanel = new DashboardPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(APP_BG);

        mainPanel.add(buildHeaderPanel());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(buildSummaryPanel());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(buildNavigationPanel());
        mainPanel.add(Box.createVerticalStrut(12));

        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));
        contentPanel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));
        contentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTENT_AREA_HEIGHT));

        overviewPanel = buildOverviewPanel();
        setupAgentsPanel = buildSetupAgentsPanel();
        marketBoardsPanel = buildMarketBoardsPanel();
        visualAnalyticsPanel = buildVisualAnalyticsPanel();
        liveFeedPanel = buildLiveFeedPanel();

        contentPanel.add(overviewPanel, TAB_OVERVIEW);
        contentPanel.add(setupAgentsPanel, TAB_SETUP);
        contentPanel.add(marketBoardsPanel, TAB_MARKET);
        contentPanel.add(visualAnalyticsPanel, TAB_ANALYTICS);
        contentPanel.add(liveFeedPanel, TAB_LIVE_FEED);

        mainPanel.add(contentPanel);

        JScrollPane pageScrollPane = new JScrollPane(mainPanel);
        pageScrollPane.setBorder(BorderFactory.createEmptyBorder());
        pageScrollPane.getViewport().setBackground(APP_BG);
        pageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(22);
        pageScrollPane.getHorizontalScrollBar().setUnitIncrement(22);

        setContentPane(pageScrollPane);

        showTab(TAB_OVERVIEW);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildHeaderPanel() {
        RoundPanel panel = createRoundPanel(new BorderLayout(), PANEL_BG, ACCENT_DARK, 10);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, HEADER_HEIGHT));
        panel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, HEADER_HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel courseLabel = createLabel("COS30018 Intelligent Systems", ACCENT, bodyBoldFont);
        JLabel titleLabel = createLabel("Velvet Hammer Auto Auction", TEXT_MAIN, titleFont);
        JLabel subtitleLabel = createLabel(
                "A JADE-based broker platform for automated car negotiation between dealer and buyer agents.",
                TEXT_MUTED,
                bodyFont
        );
        JLabel demoLabel = createLabel(
                "Demo: start the auction floor, add dealers and buyers, then observe matching, negotiation, and analytics.",
                TEXT_MAIN,
                bodyBoldFont
        );

        textPanel.add(courseLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(demoLabel);

        JPanel metaPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        metaPanel.setOpaque(false);
        metaPanel.add(createInfoBox("Theme", "Prestige car auction"));
        metaPanel.add(createInfoBox("Agents", "Broker + dealers + buyers"));
        metaPanel.add(createInfoBox("Stack", "JADE 4.6 + Swing"));

        panel.add(textPanel, BorderLayout.NORTH);
        panel.add(metaPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, SUMMARY_HEIGHT));
        panel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, SUMMARY_HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, SUMMARY_HEIGHT));

        brokerCountValue = createMetricValueLabel("0");
        dealerCountValue = createMetricValueLabel("0");
        buyerCountValue = createMetricValueLabel("0");
        platformStatusValue = createMetricValueLabel("OFF");

        panel.add(createSummaryCard("Broker", brokerCountValue, "Central facilitator"));
        panel.add(createSummaryCard("Dealers", dealerCountValue, "Vehicles on the floor"));
        panel.add(createSummaryCard("Buyers", buyerCountValue, "Automated bidders"));
        panel.add(createSummaryCard("Platform", platformStatusValue, "Container status"));

        return panel;
    }

    private JPanel buildNavigationPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 6, 0));
        panel.setOpaque(false);
        panel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, NAV_HEIGHT));
        panel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, NAV_HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, NAV_HEIGHT));

        addNavigationButton(panel, TAB_OVERVIEW);
        addNavigationButton(panel, TAB_SETUP);
        addNavigationButton(panel, TAB_MARKET);
        addNavigationButton(panel, TAB_ANALYTICS);
        addNavigationButton(panel, TAB_LIVE_FEED);

        return panel;
    }

    private void addNavigationButton(JPanel panel, String title) {
        JButton button = new JButton(title);
        button.setFont(bodyBoldFont);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setOpaque(true);
        button.setBackground(BUTTON_DARK);
        button.setForeground(TEXT_MAIN);
        button.addActionListener(e -> showTab(title));

        navButtons.put(title, button);
        panel.add(button);
    }

    private JPanel buildOverviewPanel() {
        RoundPanel panel = createRoundPanel(new BorderLayout(), PANEL_BG, ACCENT_DARK, 10);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));
        panel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(createLabel("System Workflow", TEXT_MAIN, sectionFont));
        header.add(createLabel("This dashboard demonstrates the full broker-based automated negotiation flow.", TEXT_MUTED, bodyFont));

        JPanel workflow = new JPanel(new GridBagLayout());
        workflow.setOpaque(false);

        String[][] steps = {
                {"1. Start Auction Floor", "Starts the embedded JADE main container and creates the Broker Agent."},
                {"2. Add Dealer Agents", "Dealers list cars with make, model, year, asking price, profit margin, and negotiation strategy."},
                {"3. Add Buyer Agents", "Buyers submit target car requirements, opening bid, maximum budget, and round limit."},
                {"4. Broker Matching", "The Broker Agent matches buyer requests against dealer vehicle listings."},
                {"5. Negotiation", "Buyer and Dealer Agents negotiate through JADE ACL messages using automated or manual mode."},
                {"6. Visual Analytics", "The analytics page shows buyer offer vs dealer ask, broker revenue, deal volume, and success rate."}
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.insets = new Insets(18, 12, 10, 18);
        gbc.anchor = GridBagConstraints.WEST;

        GridBagConstraints textGbc = new GridBagConstraints();
        textGbc.gridx = 1;
        textGbc.weightx = 1.0;
        textGbc.fill = GridBagConstraints.HORIZONTAL;
        textGbc.insets = new Insets(18, 0, 10, 12);
        textGbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < steps.length; i++) {
            gbc.gridy = i;
            textGbc.gridy = i;

            JLabel bullet = createLabel("•", ACCENT, new Font("Serif", Font.BOLD, 24));
            workflow.add(bullet, gbc);

            JPanel stepText = new JPanel();
            stepText.setOpaque(false);
            stepText.setLayout(new BoxLayout(stepText, BoxLayout.Y_AXIS));
            stepText.add(createLabel(steps[i][0], TEXT_MAIN, bodyBoldFont));
            stepText.add(createLabel(steps[i][1], TEXT_MUTED, bodyFont));

            workflow.add(stepText, textGbc);
        }

        panel.add(header, BorderLayout.NORTH);
        panel.add(workflow, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildSetupAgentsPanel() {
        RoundPanel panel = createRoundPanel(new GridBagLayout(), APP_BG, APP_BG, 0);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));
        panel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));

        JPanel auctionControl = buildAuctionControlPanel();
        JPanel agentManagement = buildAgentManagementPanel();
        JPanel dealerForm = buildDealerListingPanel();
        JPanel buyerForm = buildBuyerBiddingPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 6);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(auctionControl, gbc);

        gbc.insets = new Insets(0, 6, 12, 0);
        gbc.gridx = 1;
        panel.add(agentManagement, gbc);

        gbc.insets = new Insets(0, 0, 0, 6);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        panel.add(dealerForm, gbc);

        gbc.insets = new Insets(0, 6, 0, 0);
        gbc.gridx = 1;
        panel.add(buyerForm, gbc);

        return panel;
    }

    private JPanel buildAuctionControlPanel() {
        RoundPanel panel = createRoundPanel(new BorderLayout(), PANEL_BG, ACCENT_DARK, 10);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Increased height so the buttons and tip text do not get clipped
        panel.setPreferredSize(new Dimension(10, 175));
        panel.setMinimumSize(new Dimension(10, 175));

        JPanel header = createCardHeader(
                "Auction Control",
                "Start the JADE container, launch demo agents, or reset the platform."
        );

        startFloorButton = createButton("Start Auction Floor", ACCENT, Color.BLACK, BUTTON_HEIGHT);
        runDemoButton = createButton("Run Demo Lineup", BUTTON_RED, Color.WHITE, BUTTON_HEIGHT);
        resetButton = createButton("Reset Platform", BUTTON_BLUE, Color.WHITE, BUTTON_HEIGHT);

        JPanel buttonRow = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setPreferredSize(new Dimension(10, BUTTON_HEIGHT));
        buttonRow.setMinimumSize(new Dimension(10, BUTTON_HEIGHT));
        buttonRow.add(startFloorButton);
        buttonRow.add(runDemoButton);
        buttonRow.add(resetButton);

        JLabel tip = createLabel(
                "Tip: add dealers before buyers for immediate matching, or add buyers first to test retry-based matching.",
                TEXT_MUTED,
                bodyFont
        );

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buttonRow);
        body.add(Box.createVerticalStrut(10));
        body.add(tip);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildAgentManagementPanel() {
        RoundPanel panel = createRoundPanel(new GridBagLayout(), PANEL_BG, ACCENT_DARK, 10);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Increased height so the Delete Selected Agent button is fully visible
        panel.setPreferredSize(new Dimension(10, 175));
        panel.setMinimumSize(new Dimension(10, 175));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JLabel titleLabel = createLabel("Agent Management", TEXT_MAIN, sectionFont);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = createLabel(
                "Delete a selected dealer or buyer without resetting the full platform.",
                TEXT_MUTED,
                bodyFont
        );
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 10, 0);
        panel.add(subtitleLabel, gbc);

        JLabel selectLabel = createLabel("Select Agent", TEXT_MUTED, smallBoldFont);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(selectLabel, gbc);

        deleteAgentComboBox = createComboBox(new String[] {"No removable agents"});
        deleteAgentComboBox.setPreferredSize(new Dimension(10, 34));
        deleteAgentComboBox.setMinimumSize(new Dimension(10, 34));

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(deleteAgentComboBox, gbc);

        deleteAgentButton = createButton("Delete Selected Agent", BUTTON_RED, Color.WHITE, BUTTON_HEIGHT);
        deleteAgentButton.setFont(bodyBoldFont);
        deleteAgentButton.setPreferredSize(new Dimension(10, BUTTON_HEIGHT));
        deleteAgentButton.setMinimumSize(new Dimension(10, BUTTON_HEIGHT));
        deleteAgentButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(deleteAgentButton, gbc);

        return panel;
    }

    private JPanel buildDealerListingPanel() {
        RoundPanel panel = createRoundPanel(new BorderLayout(), PANEL_BG, ACCENT_DARK, 10);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel header = createCardHeader(
                "Dealer Listing Booth",
                "Register a dealer's vehicle listing before buyers begin negotiating."
        );

        dealerAgentNameField = createTextField("ViosDealer01");
        dealerMakeField = createTextField("Toyota");
        dealerModelField = createTextField("Vios 1.5E AT");
        dealerYearField = createTextField("2025");
        dealerListPriceField = createTextField("93242");
        dealerMinMarginField = createTextField("0.08");
        dealerStrategyComboBox = createComboBox(new String[] {"Stubborn", "Desperate", "Matcher"});

        listVehicleButton = createButton("List Vehicle", ACCENT, Color.BLACK, LARGE_BUTTON_HEIGHT);
        listVehicleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, LARGE_BUTTON_HEIGHT));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        addFormRow(form, "Agent Name", dealerAgentNameField);
        addFormRow(form, "Make", dealerMakeField);
        addFormRow(form, "Model", dealerModelField);
        addFormRow(form, "Year", dealerYearField);
        addFormRow(form, "List Price", dealerListPriceField);
        addFormRow(form, "Min Margin", dealerMinMarginField);
        addFormRow(form, "Strategy", dealerStrategyComboBox);

        form.add(Box.createVerticalStrut(12));
        form.add(listVehicleButton);

        panel.add(header, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBuyerBiddingPanel() {
        RoundPanel panel = createRoundPanel(new BorderLayout(), PANEL_BG, ACCENT_DARK, 10);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel header = createCardHeader(
                "Buyer Bidding Desk",
                "Create a buyer agent with target car requirements and negotiation limits."
        );

        buyerAgentNameField = createTextField("ViosBuyer01");
        buyerMakeField = createTextField("Toyota");
        buyerModelField = createTextField("Vios 1.5E AT");
        buyerOpeningBidField = createTextField("88000");
        buyerMaxBudgetField = createTextField("96000");
        buyerMaxRoundsField = createTextField("5");

        manualNegotiationCheckBox = new JCheckBox("Enable Manual Negotiation UI");
        manualNegotiationCheckBox.setOpaque(false);
        manualNegotiationCheckBox.setForeground(TEXT_MAIN);
        manualNegotiationCheckBox.setFont(bodyFont);
        manualNegotiationCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        startBuyerButton = createButton("Start Buyer Agent", BUTTON_RED, Color.WHITE, LARGE_BUTTON_HEIGHT);
        startBuyerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, LARGE_BUTTON_HEIGHT));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        addFormRow(form, "Agent Name", buyerAgentNameField);
        addFormRow(form, "Make", buyerMakeField);
        addFormRow(form, "Model", buyerModelField);
        addFormRow(form, "Opening Bid", buyerOpeningBidField);
        addFormRow(form, "Max Budget", buyerMaxBudgetField);
        addFormRow(form, "Max Rounds", buyerMaxRoundsField);

        form.add(Box.createVerticalStrut(8));
        form.add(manualNegotiationCheckBox);
        form.add(Box.createVerticalStrut(12));
        form.add(startBuyerButton);

        panel.add(header, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildMarketBoardsPanel() {
        RoundPanel panel = createRoundPanel(new GridLayout(1, 2, 12, 0), APP_BG, APP_BG, 0);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));
        panel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));

        dealerTableModel = createReadOnlyTableModel(new String[] {"Dealer", "Vehicle", "Year", "Ask", "Floor"});
        buyerTableModel = createReadOnlyTableModel(new String[] {"Buyer", "Target", "Opening", "Ceiling", "Rounds"});

        JTable dealerTable = createStyledTable(dealerTableModel);
        JTable buyerTable = createStyledTable(buyerTableModel);

        panel.add(createTableCard("Showroom Board", "Live dealer inventory available on the broker floor.", dealerTable));
        panel.add(createTableCard("Buyer Board", "Buyer agents currently scouting and negotiating.", buyerTable));

        return panel;
    }

    private JPanel buildVisualAnalyticsPanel() {
        RoundPanel panel = createRoundPanel(new BorderLayout(), PANEL_BG, ACCENT_DARK, 10);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));
        panel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));

        JPanel header = createCardHeader(
                "Visual Analytics",
                "Real-time negotiation graph and Broker Treasury Dashboard."
        );

        negotiationsValue = createMetricValueLabel("0");
        successfulValue = createMetricValueLabel("0");
        failedValue = createMetricValueLabel("0");
        successRateValue = createMetricValueLabel("0.0%");
        fixedFeesValue = createMetricValueLabel(formatRM(0));
        commissionsValue = createMetricValueLabel(formatRM(0));
        revenueValue = createMetricValueLabel(formatRM(0));
        latestGapValue = createMetricValueLabel(formatRM(0));

        JPanel metricsGrid = new JPanel(new GridLayout(2, 4, 10, 10));
        metricsGrid.setOpaque(false);
        metricsGrid.setPreferredSize(new Dimension(10, 180));
        metricsGrid.setMinimumSize(new Dimension(10, 180));
        metricsGrid.add(createAnalyticsCard("Negotiations", negotiationsValue, "Started sessions"));
        metricsGrid.add(createAnalyticsCard("Successful", successfulValue, "Closed deals"));
        metricsGrid.add(createAnalyticsCard("Failed", failedValue, "Walkaway / rejected"));
        metricsGrid.add(createAnalyticsCard("Success Rate", successRateValue, "Successful / total"));
        metricsGrid.add(createAnalyticsCard("Fixed Fees", fixedFeesValue, "Broker connection fees"));
        metricsGrid.add(createAnalyticsCard("Commissions", commissionsValue, "2% successful deals"));
        metricsGrid.add(createAnalyticsCard("Revenue", revenueValue, "Fees + commissions"));
        metricsGrid.add(createAnalyticsCard("Latest Gap", latestGapValue, "Dealer ask - buyer offer"));

        negotiationGapChart = new NegotiationGapChart();
        treasuryChart = new TreasuryChart();

        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        chartsGrid.setOpaque(false);
        chartsGrid.add(createChartCard("Negotiation Gap Chart", negotiationGapChart));
        chartsGrid.add(createChartCard("Broker Treasury Chart", treasuryChart));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(metricsGrid, BorderLayout.NORTH);
        center.add(chartsGrid, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildLiveFeedPanel() {
        RoundPanel panel = createRoundPanel(new BorderLayout(), PANEL_BG, ACCENT_DARK, 10);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));
        panel.setPreferredSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));

        JPanel header = createCardHeader(
                "Live Auction Feed",
                "Real-time broker updates, buyer requests, dealer decisions, negotiation rounds, and confirmed deals."
        );

        feedPane = new JTextPane();
        feedPane.setEditable(false);
        feedPane.setBackground(new Color(20, 22, 30));
        feedPane.setForeground(TEXT_MAIN);
        feedPane.setFont(monoFont);
        feedPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane feedScrollPane = new JScrollPane(feedPane);
        feedScrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 50, 62)));
        feedScrollPane.getViewport().setBackground(new Color(20, 22, 30));
        feedScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        panel.add(header, BorderLayout.NORTH);
        panel.add(feedScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void wireActions() {
        startFloorButton.addActionListener(e -> startAuctionFloor());
        runDemoButton.addActionListener(e -> runDemoLineup());
        resetButton.addActionListener(e -> resetPlatform());
        listVehicleButton.addActionListener(e -> listVehicleFromForm());
        startBuyerButton.addActionListener(e -> startBuyerFromForm());
        deleteAgentButton.addActionListener(e -> deleteSelectedAgent());
    }

    private void showTab(String tabName) {
        contentCardLayout.show(contentPanel, tabName);

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = Objects.equals(entry.getKey(), tabName);
            JButton button = entry.getValue();
            button.setBackground(active ? ACCENT : BUTTON_DARK);
            button.setForeground(active ? Color.BLACK : TEXT_MAIN);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private int determineLayoutMode(int width) {
        if (width >= 1180) {
            return LAYOUT_WIDE;
        }

        if (width >= 820) {
            return LAYOUT_MEDIUM;
        }

        return LAYOUT_COMPACT;
    }

    private void refreshResponsiveLayout() {
        int windowWidth = getContentPane().getWidth();

        if (windowWidth <= 0) {
            windowWidth = getWidth();
        }

        int layoutMode = determineLayoutMode(windowWidth);

        int pageWidth;
        if (layoutMode == LAYOUT_WIDE) {
            pageWidth = Math.max(windowWidth - 70, MIN_PAGE_WIDTH);
        } else if (layoutMode == LAYOUT_MEDIUM) {
            pageWidth = Math.max(windowWidth - 60, MIN_PAGE_WIDTH);
        } else {
            pageWidth = MIN_PAGE_WIDTH;
        }

        Dimension pageSize = new Dimension(pageWidth, mainPanel.getPreferredSize().height);
        mainPanel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, 0));
        mainPanel.setPreferredSize(pageSize);

        contentPanel.setMinimumSize(new Dimension(MIN_PAGE_WIDTH, CONTENT_AREA_HEIGHT));
        contentPanel.setPreferredSize(new Dimension(pageWidth, CONTENT_AREA_HEIGHT));
        contentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTENT_AREA_HEIGHT));

        mainPanel.revalidate();
        mainPanel.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void startAuctionFloor() {
        if (platformLive) {
            appendFeed("System", "Auction floor is already live.");
            return;
        }

        try {
            jade.core.Runtime runtime = jade.core.Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "false");

            mainContainer = runtime.createMainContainer(profile);
            platformLive = true;

            try {
                brokerController = mainContainer.createNewAgent("brokerKA", BROKER_AGENT_CLASS, new Object[] {this});
                brokerController.start();
                appendFeed("System", "JADE main container started and Broker Agent created.");
            } catch (Exception brokerError) {
                appendFeed("System", "JADE container started, but Broker Agent could not be created: " + brokerError.getMessage());
            }
        } catch (Exception e) {
            platformLive = false;
            mainContainer = null;
            brokerController = null;
            appendFeed("System", "Failed to start JADE container: " + e.getMessage());
            JOptionPane.showMessageDialog(
                    this,
                    "Could not start the JADE container.\n\n" + e.getMessage(),
                    "JADE Start Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        updateDashboardStats();
    }

    private void runDemoLineup() {
        startAuctionFloor();

        DealerListing[] dealers = {
                new DealerListing("ViosDealer01", "Toyota", "Vios 1.5G AT", 2025, 99242.00, 0.08, "Stubborn"),
                new DealerListing("ViosDealer02", "Toyota", "Vios 1.5E AT", 2025, 93242.00, 0.10, "Desperate"),
                new DealerListing("ViosDealer03", "Toyota", "Vios 1.5 HEV AT", 2025, 106542.00, 0.07, "Matcher"),

                new DealerListing("CityDealer01", "Honda", "City 1.5L S", 2025, 86668.00, 0.08, "Stubborn"),
                new DealerListing("CityDealer02", "Honda", "City 1.5L E", 2025, 91668.00, 0.10, "Desperate"),
                new DealerListing("CityDealer03", "Honda", "City 1.5L V", 2025, 96668.00, 0.07, "Matcher")
        };

        BuyerRequest[] buyers = {
                new BuyerRequest("ViosBuyer01", "Toyota", "Vios 1.5G AT", 91000.00, 100500.00, 5, false),
                new BuyerRequest("ViosBuyer02", "Toyota", "Vios 1.5E AT", 85000.00, 95000.00, 5, false),
                new BuyerRequest("ViosBuyer03", "Toyota", "Vios 1.5 HEV AT", 99000.00, 108000.00, 5, false),

                new BuyerRequest("CityBuyer01", "Honda", "City 1.5L S", 80000.00, 87500.00, 5, false),
                new BuyerRequest("CityBuyer02", "Honda", "City 1.5L E", 86000.00, 92500.00, 5, false),
                new BuyerRequest("CityBuyer03", "Honda", "City 1.5L V", 90000.00, 98000.00, 5, false)
        };

        for (DealerListing dealer : dealers) {
            if (!agentTypes.containsKey(dealer.agentName)) {
                addDealerListing(dealer, true);
            }
        }

        for (BuyerRequest buyer : buyers) {
            if (!agentTypes.containsKey(buyer.agentName)) {
                addBuyerRequest(buyer, true);
                performLocalNegotiation(buyer);
            }
        }

        appendFeed("System", "Demo lineup loaded with Toyota Vios and Honda City dealer and buyer agents.");
        updateDashboardStats();
        updateDeleteAgentComboBox();
        showTab(TAB_LIVE_FEED);
    }

    private void resetPlatform() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Reset the platform and clear all dealers, buyers, logs, and analytics?",
                "Reset Platform",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        for (String agentName : new ArrayList<>(agentControllers.keySet())) {
            killAgentSilently(agentName);
        }

        if (brokerController != null) {
            try {
                brokerController.kill();
            } catch (Exception ignored) {
                /*
                 * Ignore broker shutdown errors during reset.
                 */
            }
        }

        if (mainContainer != null) {
            try {
                mainContainer.kill();
            } catch (Exception ignored) {
                /*
                 * Ignore container shutdown errors during reset.
                 */
            }
        }

        mainContainer = null;
        brokerController = null;
        platformLive = false;

        agentControllers.clear();
        agentTypes.clear();
        dealerListings.clear();
        buyerRequests.clear();
        buyerOfferHistory.clear();
        dealerAskHistory.clear();

        negotiations = 0;
        successfulDeals = 0;
        failedDeals = 0;
        fixedFees = 0.0;
        commissions = 0.0;
        latestGap = 0.0;

        dealerTableModel.setRowCount(0);
        buyerTableModel.setRowCount(0);

        feedPane.setText("");

        appendFeed("Dashboard", "Auction dashboard is ready. Start the floor or launch the demo lineup.");

        updateDashboardStats();
        updateDeleteAgentComboBox();
        repaintCharts();
    }

    private void listVehicleFromForm() {
        try {
            String agentName = cleanText(dealerAgentNameField.getText());
            String make = cleanText(dealerMakeField.getText());
            String model = cleanText(dealerModelField.getText());
            int year = parseInt(dealerYearField.getText(), "Year");
            double listPrice = parseDouble(dealerListPriceField.getText(), "List Price");
            double minMargin = parseDouble(dealerMinMarginField.getText(), "Min Margin");
            String strategy = String.valueOf(dealerStrategyComboBox.getSelectedItem());

            if (agentName.isEmpty() || make.isEmpty() || model.isEmpty()) {
                throw new IllegalArgumentException("Agent name, make, and model cannot be empty.");
            }

            if (agentTypes.containsKey(agentName)) {
                throw new IllegalArgumentException("Agent name already exists: " + agentName);
            }

            DealerListing dealer = new DealerListing(agentName, make, model, year, listPrice, minMargin, strategy);
            addDealerListing(dealer, true);

            appendFeed(agentName, "Listed " + dealer.vehicleName() + " at " + formatRM(dealer.askPrice)
                    + " with floor " + formatRM(dealer.floorPrice()) + " using " + strategy + " strategy.");

            updateDashboardStats();
            updateDeleteAgentComboBox();
            showTab(TAB_MARKET);
        } catch (Exception e) {
            showInputError(e.getMessage());
        }
    }

    private void startBuyerFromForm() {
        try {
            String agentName = cleanText(buyerAgentNameField.getText());
            String make = cleanText(buyerMakeField.getText());
            String model = cleanText(buyerModelField.getText());
            double openingBid = parseDouble(buyerOpeningBidField.getText(), "Opening Bid");
            double maxBudget = parseDouble(buyerMaxBudgetField.getText(), "Max Budget");
            int maxRounds = parseInt(buyerMaxRoundsField.getText(), "Max Rounds");
            boolean manualMode = manualNegotiationCheckBox.isSelected();

            if (agentName.isEmpty() || make.isEmpty() || model.isEmpty()) {
                throw new IllegalArgumentException("Agent name, make, and model cannot be empty.");
            }

            if (agentTypes.containsKey(agentName)) {
                throw new IllegalArgumentException("Agent name already exists: " + agentName);
            }

            if (openingBid > maxBudget) {
                throw new IllegalArgumentException("Opening bid cannot be higher than max budget.");
            }

            BuyerRequest buyer = new BuyerRequest(agentName, make, model, openingBid, maxBudget, maxRounds, manualMode);
            addBuyerRequest(buyer, true);

            appendFeed(agentName, "Seeking " + buyer.targetName() + " with opening bid "
                    + formatRM(openingBid) + " and budget " + formatRM(maxBudget) + ".");

            if (manualMode) {
                appendFeed(agentName, "Manual Negotiation UI enabled. The buyer is created, but auto negotiation is paused.");
            } else {
                performLocalNegotiation(buyer);
            }

            updateDashboardStats();
            updateDeleteAgentComboBox();

            if (manualMode) {
                showTab(TAB_LIVE_FEED);
            } else {
                showTab(TAB_ANALYTICS);
            }
        } catch (Exception e) {
            showInputError(e.getMessage());
        }
    }

    private void addDealerListing(DealerListing dealer, boolean createAgent) {
        dealerListings.add(dealer);
        agentTypes.put(dealer.agentName, "Dealer");

        AgentController controller = null;
        if (createAgent && platformLive && mainContainer != null) {
            controller = createAgentSafely(
                    dealer.agentName,
                    DEALER_AGENT_CLASS,
                    new Object[] {
                            this,
                            dealer.make,
                            dealer.model,
                            dealer.year,
                            dealer.askPrice,
                            dealer.minMargin,
                            dealer.strategy
                    }
            );
        }

        agentControllers.put(dealer.agentName, controller);

        dealerTableModel.addRow(new Object[] {
                dealer.agentName,
                dealer.vehicleName(),
                String.valueOf(dealer.year),
                formatRM(dealer.askPrice),
                formatRM(dealer.floorPrice())
        });

        updateDashboardStats();
        updateDeleteAgentComboBox();
    }

    private void addBuyerRequest(BuyerRequest buyer, boolean createAgent) {
        buyerRequests.add(buyer);
        agentTypes.put(buyer.agentName, "Buyer");

        AgentController controller = null;
        if (createAgent && platformLive && mainContainer != null) {
            controller = createAgentSafely(
                    buyer.agentName,
                    BUYER_AGENT_CLASS,
                    new Object[] {
                            this,
                            buyer.make,
                            buyer.model,
                            buyer.openingBid,
                            buyer.maxBudget,
                            buyer.maxRounds,
                            buyer.manualMode
                    }
            );
        }

        agentControllers.put(buyer.agentName, controller);

        buyerTableModel.addRow(new Object[] {
                buyer.agentName,
                buyer.targetName(),
                formatRM(buyer.openingBid),
                formatRM(buyer.maxBudget),
                String.valueOf(buyer.maxRounds)
        });

        updateDashboardStats();
        updateDeleteAgentComboBox();
    }

    private AgentController createAgentSafely(String agentName, String className, Object[] args) {
        try {
            AgentController controller = mainContainer.createNewAgent(agentName, className, args);
            controller.start();
            appendFeed("System", "Started " + agentName + " using " + className + ".");
            return controller;
        } catch (StaleProxyException e) {
            appendFeed("System", "Could not start " + agentName + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            appendFeed("System", "Could not start " + agentName + ": " + e.getMessage());
            return null;
        }
    }

    private void deleteSelectedAgent() {
        Object selected = deleteAgentComboBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        String agentName = selected.toString();

        if (agentName.equals("No removable agents")) {
            appendFeed("System", "There are no dealer or buyer agents to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete agent: " + agentName + "?",
                "Delete Agent",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        killAgentSilently(agentName);

        String agentType = agentTypes.remove(agentName);
        agentControllers.remove(agentName);

        if ("Dealer".equals(agentType)) {
            dealerListings.removeIf(dealer -> dealer.agentName.equals(agentName));
            removeRowsByFirstColumn(dealerTableModel, agentName);
        } else if ("Buyer".equals(agentType)) {
            buyerRequests.removeIf(buyer -> buyer.agentName.equals(agentName));
            removeRowsByFirstColumn(buyerTableModel, agentName);
        }

        appendFeed("System", "Deleted agent " + agentName + ".");
        updateDashboardStats();
        updateDeleteAgentComboBox();
        showTab(TAB_SETUP);
    }

    private void killAgentSilently(String agentName) {
        AgentController controller = agentControllers.get(agentName);

        if (controller == null) {
            return;
        }

        try {
            controller.kill();
        } catch (Exception ignored) {
            /*
             * Agent may already be dead.
             */
        }
    }

    private void removeRowsByFirstColumn(DefaultTableModel model, String firstColumnValue) {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            Object value = model.getValueAt(i, 0);
            if (Objects.equals(String.valueOf(value), firstColumnValue)) {
                model.removeRow(i);
            }
        }
    }

    private void performLocalNegotiation(BuyerRequest buyer) {
        java.util.List<DealerListing> matches = findMatchingDealers(buyer);

        negotiations++;

        appendFeed("Broker", "Request for " + buyer.targetName() + " from " + buyer.agentName + ".");
        appendFeed("Broker", "Matched " + matches.size() + " dealer(s) for " + buyer.agentName + ".");

        if (matches.isEmpty()) {
            failedDeals++;
            appendFeed(buyer.agentName, "Broker matched 0 dealers. Waiting for future dealer listings.");
            updateDashboardStats();
            repaintCharts();
            return;
        }

        fixedFees += 50.0;

        DealerListing bestDealer = matches.get(0);
        boolean success = false;
        double finalPrice = 0.0;
        double finalBuyerOffer = buyer.openingBid;
        double finalDealerAsk = bestDealer.askPrice;

        for (int round = 0; round <= buyer.maxRounds; round++) {
            double progress = buyer.maxRounds == 0 ? 1.0 : (double) round / buyer.maxRounds;
            double buyerOffer = buyer.openingBid + ((buyer.maxBudget - buyer.openingBid) * progress);
            double dealerAsk = calculateDealerAsk(bestDealer, progress);

            finalBuyerOffer = buyerOffer;
            finalDealerAsk = dealerAsk;
            latestGap = Math.max(0.0, dealerAsk - buyerOffer);

            buyerOfferHistory.add(buyerOffer);
            dealerAskHistory.add(dealerAsk);

            appendFeed(
                    buyer.agentName,
                    "Round " + round + " offer " + formatRM(buyerOffer)
                            + " against " + bestDealer.agentName + " ask " + formatRM(dealerAsk) + "."
            );

            if (buyerOffer >= dealerAsk && buyerOffer <= buyer.maxBudget) {
                success = true;
                finalPrice = dealerAsk;
                break;
            }
        }

        if (success) {
            successfulDeals++;
            double commission = finalPrice * 0.02;
            commissions += commission;

            appendFeed(
                    bestDealer.agentName,
                    "Accepted " + buyer.agentName + " offer at " + formatRM(finalPrice) + "."
            );
            appendFeed(
                    "Broker",
                    "Deal closed between " + buyer.agentName + " and " + bestDealer.agentName
                            + " for " + formatRM(finalPrice) + "."
            );
        } else {
            failedDeals++;
            appendFeed(
                    "Broker",
                    "No deal between " + buyer.agentName + " and " + bestDealer.agentName
                            + ". Latest gap was " + formatRM(finalDealerAsk - finalBuyerOffer) + "."
            );
        }

        updateDashboardStats();
        repaintCharts();
    }

    private java.util.List<DealerListing> findMatchingDealers(BuyerRequest buyer) {
        java.util.List<DealerListing> matches = new ArrayList<>();

        for (DealerListing dealer : dealerListings) {
            boolean makeMatches = dealer.make.equalsIgnoreCase(buyer.make);
            boolean modelMatches = dealer.model.equalsIgnoreCase(buyer.model);

            if (makeMatches && modelMatches) {
                matches.add(dealer);
            }
        }

        return matches;
    }

    private double calculateDealerAsk(DealerListing dealer, double progress) {
        double ask = dealer.askPrice;
        double floor = dealer.floorPrice();
        double possibleDiscount = ask - floor;

        double concessionFactor;
        if ("Desperate".equalsIgnoreCase(dealer.strategy)) {
            concessionFactor = 0.95;
        } else if ("Matcher".equalsIgnoreCase(dealer.strategy)) {
            concessionFactor = 0.75;
        } else {
            concessionFactor = 0.45;
        }

        return ask - (possibleDiscount * progress * concessionFactor);
    }

    private void updateDashboardStats() {
        brokerCountValue.setText(brokerController != null ? "1" : "0");
        dealerCountValue.setText(String.valueOf(dealerListings.size()));
        buyerCountValue.setText(String.valueOf(buyerRequests.size()));
        platformStatusValue.setText(platformLive ? "LIVE" : "OFF");

        if (negotiationsValue != null) {
            negotiationsValue.setText(String.valueOf(negotiations));
            successfulValue.setText(String.valueOf(successfulDeals));
            failedValue.setText(String.valueOf(failedDeals));

            double successRate = negotiations == 0 ? 0.0 : ((double) successfulDeals / negotiations) * 100.0;
            successRateValue.setText(String.format(Locale.US, "%.1f%%", successRate));

            fixedFeesValue.setText(formatRM(fixedFees));
            commissionsValue.setText(formatRM(commissions));
            revenueValue.setText(formatRM(fixedFees + commissions));
            latestGapValue.setText(formatRM(latestGap));
        }

        repaintCharts();
    }

    private void updateDeleteAgentComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        for (String agentName : agentTypes.keySet()) {
            model.addElement(agentName);
        }

        boolean hasAgents = model.getSize() > 0;

        if (!hasAgents) {
            model.addElement("No removable agents");
        }

        deleteAgentComboBox.setModel(model);
        deleteAgentComboBox.setEnabled(hasAgents);
        deleteAgentButton.setEnabled(hasAgents);
    }

    private void repaintCharts() {
        if (negotiationGapChart != null) {
            negotiationGapChart.repaint();
        }

        if (treasuryChart != null) {
            treasuryChart.repaint();
        }
    }

    public void appendFeed(String source, String message) {
        SwingUtilities.invokeLater(() -> appendFeedOnUiThread(source, message));
    }

    public void log(String source, String message) {
        appendFeed(source, message);
    }

    public void addFeedLine(String source, String message) {
        appendFeed(source, message);
    }

    public void dealerListed(String agentName, String make, String model, int year, double askPrice, double floorPrice) {
        SwingUtilities.invokeLater(() -> {
            dealerTableModel.addRow(new Object[] {
                    agentName,
                    make + " " + model,
                    String.valueOf(year),
                    formatRM(askPrice),
                    formatRM(floorPrice)
            });
            appendFeed(agentName, "Listed " + make + " " + model + " at " + formatRM(askPrice) + ".");
        });
    }

    public void buyerJoined(String agentName, String make, String model, double openingBid, double maxBudget, int rounds) {
        SwingUtilities.invokeLater(() -> {
            buyerTableModel.addRow(new Object[] {
                    agentName,
                    make + " " + model,
                    formatRM(openingBid),
                    formatRM(maxBudget),
                    String.valueOf(rounds)
            });
            appendFeed(agentName, "Joined the floor for " + make + " " + model + ".");
        });
    }

    public void negotiationUpdate(String buyerName, String dealerName, double buyerOffer, double dealerAsk) {
        SwingUtilities.invokeLater(() -> {
            buyerOfferHistory.add(buyerOffer);
            dealerAskHistory.add(dealerAsk);
            latestGap = Math.max(0.0, dealerAsk - buyerOffer);
            negotiations++;
            fixedFees += 50.0;

            appendFeed(
                    "Broker",
                    buyerName + " offered " + formatRM(buyerOffer)
                            + " to " + dealerName + " asking " + formatRM(dealerAsk) + "."
            );

            updateDashboardStats();
            repaintCharts();
        });
    }

    public void dealClosed(String buyerName, String dealerName, double price) {
        SwingUtilities.invokeLater(() -> {
            successfulDeals++;
            commissions += price * 0.02;

            appendFeed(
                    "Broker",
                    "Deal closed between " + buyerName + " and " + dealerName + " for " + formatRM(price) + "."
            );

            updateDashboardStats();
            repaintCharts();
        });
    }

    public void dealFailed(String buyerName, String dealerName) {
        SwingUtilities.invokeLater(() -> {
            failedDeals++;

            appendFeed(
                    "Broker",
                    "Negotiation failed between " + buyerName + " and " + dealerName + "."
            );

            updateDashboardStats();
            repaintCharts();
        });
    }

    private void appendFeedOnUiThread(String source, String message) {
        if (feedPane == null) {
            return;
        }

        StyledDocument document = feedPane.getStyledDocument();

        SimpleAttributeSet timeStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(timeStyle, TEXT_MUTED);
        StyleConstants.setFontFamily(timeStyle, "Monospaced");
        StyleConstants.setFontSize(timeStyle, 11);

        SimpleAttributeSet sourceStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(sourceStyle, ACCENT);
        StyleConstants.setBold(sourceStyle, true);
        StyleConstants.setFontFamily(sourceStyle, "Monospaced");
        StyleConstants.setFontSize(sourceStyle, 11);

        SimpleAttributeSet messageStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(messageStyle, TEXT_MAIN);
        StyleConstants.setFontFamily(messageStyle, "Monospaced");
        StyleConstants.setFontSize(messageStyle, 11);

        try {
            document.insertString(document.getLength(), LocalTime.now().format(timeFormatter) + " ", timeStyle);
            document.insertString(document.getLength(), "[" + source + "] ", sourceStyle);
            document.insertString(document.getLength(), message + "\n", messageStyle);
            feedPane.setCaretPosition(document.getLength());
        } catch (BadLocationException ignored) {
            /*
             * Ignore feed append failure.
             */
        }
    }

    private JPanel createInfoBox(String title, String value) {
        RoundPanel box = createRoundPanel(new BorderLayout(), CARD_BG, ACCENT_DARK, 8);
        box.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel titleLabel = createLabel(title, ACCENT, smallBoldFont);
        JLabel valueLabel = createLabel(value, TEXT_MAIN, bodyFont);

        box.add(titleLabel, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);

        return box;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, String subtitle) {
        RoundPanel card = createRoundPanel(new BorderLayout(), CARD_BG, ACCENT_DARK, 8);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = createLabel(title, TEXT_MUTED, smallBoldFont);
        JLabel subtitleLabel = createLabel(subtitle, TEXT_MUTED, bodyFont);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(valueLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(subtitleLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    private JPanel createAnalyticsCard(String title, JLabel valueLabel, String subtitle) {
        RoundPanel card = createRoundPanel(new BorderLayout(), CARD_BG, ACCENT_DARK, 0);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = createLabel(title, TEXT_MUTED, smallBoldFont);
        JLabel subtitleLabel = createLabel(subtitle, TEXT_MUTED, bodyFont);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(valueLabel);
        center.add(Box.createVerticalStrut(5));
        center.add(subtitleLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTableCard(String title, String subtitle, JTable table) {
        RoundPanel card = createRoundPanel(new BorderLayout(), PANEL_BG, ACCENT_DARK, 10);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel header = createCardHeader(title, subtitle);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(215, 215, 215)));
        scrollPane.getViewport().setBackground(TABLE_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        card.add(header, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createChartCard(String title, JPanel chart) {
        RoundPanel card = createRoundPanel(new BorderLayout(), CARD_BG, ACCENT_DARK, 0);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel titleLabel = createLabel(title, TEXT_MAIN, new Font("Serif", Font.BOLD, 19));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCardHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(createLabel(title, TEXT_MAIN, sectionFont));
        header.add(createLabel(subtitle, TEXT_MUTED, bodyFont));
        header.add(Box.createVerticalStrut(10));
        return header;
    }

    private JLabel createLabel(String text, Color color, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font);
        return label;
    }

    private JLabel createMetricValueLabel(String text) {
        JLabel label = createLabel(text, TEXT_MAIN, metricFont);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private JTextField createTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(bodyFont);
        field.setForeground(TEXT_MAIN);
        field.setBackground(INPUT_BG);
        field.setCaretColor(TEXT_MAIN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(57, 64, 78)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        field.setMinimumSize(new Dimension(10, 28));
        field.setPreferredSize(new Dimension(10, 28));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return field;
    }

    private JComboBox<String> createComboBox(String[] values) {
        JComboBox<String> comboBox = new JComboBox<>(values);
        comboBox.setFont(bodyFont);
        comboBox.setForeground(TEXT_MAIN);
        comboBox.setBackground(INPUT_BG);
        comboBox.setOpaque(true);
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(57, 64, 78)));
        comboBox.setMinimumSize(new Dimension(10, 30));
        comboBox.setPreferredSize(new Dimension(10, 30));
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : value.toString());
            label.setOpaque(true);
            label.setFont(bodyFont);
            label.setBorder(new EmptyBorder(6, 10, 6, 10));
            label.setForeground(TEXT_MAIN);
            label.setBackground(isSelected ? BUTTON_DARK : INPUT_BG);
            return label;
        });

        return comboBox;
    }

    private JButton createButton(String text, Color background, Color foreground, int height) {
        JButton button = new JButton(text);
        button.setFont(bodyBoldFont);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setMinimumSize(new Dimension(120, height));
        button.setPreferredSize(new Dimension(160, height));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return button;
    }

    private void addFormRow(JPanel panel, String labelText, Component input) {
        JLabel label = createLabel(labelText, TEXT_MUTED, smallBoldFont);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        panel.add(label);
        panel.add(Box.createVerticalStrut(3));
        panel.add(input);
        panel.add(Box.createVerticalStrut(7));
    }

    private DefaultTableModel createReadOnlyTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(bodyFont);
        table.setRowHeight(28);
        table.setForeground(TEXT_MAIN);
        table.setBackground(TABLE_BG);
        table.setGridColor(new Color(47, 54, 65));
        table.setSelectionBackground(new Color(63, 73, 89));
        table.setSelectionForeground(TEXT_MAIN);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(bodyBoldFont);
        table.getTableHeader().setForeground(TEXT_MAIN);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBackground(TABLE_BG);
        renderer.setForeground(TEXT_MAIN);
        renderer.setBorder(new EmptyBorder(0, 4, 0, 4));

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        return table;
    }

    private RoundPanel createRoundPanel(java.awt.LayoutManager layout, Color background, Color border, int arc) {
        RoundPanel panel = new RoundPanel(background, border, arc);
        panel.setLayout(layout);
        return panel;
    }

    private String cleanText(String text) {
        return text == null ? "" : text.trim();
    }

    private int parseInt(String text, String fieldName) {
        try {
            return Integer.parseInt(cleanText(text));
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer.");
        }
    }

    private double parseDouble(String text, String fieldName) {
        try {
            return Double.parseDouble(cleanText(text).replace(",", ""));
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    private String formatRM(double value) {
        return "RM" + numberFormat.format(value);
    }

    private void showInputError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Input Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private final class NegotiationGapChart extends JPanel {
        private static final long serialVersionUID = 1L;

        private NegotiationGapChart() {
            setBackground(TABLE_BG);
            setOpaque(true);
            setPreferredSize(new Dimension(10, 260));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 55;
            int right = 25;
            int top = 30;
            int bottom = 55;

            g2.setColor(TABLE_BG);
            g2.fillRect(0, 0, width, height);

            g2.setColor(GRID_LINE);
            g2.drawLine(left, top, left, height - bottom);
            g2.drawLine(left, height - bottom, width - right, height - bottom);

            if (buyerOfferHistory.isEmpty() || dealerAskHistory.isEmpty()) {
                g2.setColor(TEXT_MUTED);
                g2.setFont(bodyFont);
                g2.drawString("Run a negotiation to display Buyer Offer vs Dealer Ask.", left + 10, top + 55);
                g2.dispose();
                return;
            }

            double max = 0;
            double min = Double.MAX_VALUE;

            for (double value : buyerOfferHistory) {
                max = Math.max(max, value);
                min = Math.min(min, value);
            }

            for (double value : dealerAskHistory) {
                max = Math.max(max, value);
                min = Math.min(min, value);
            }

            if (max <= min) {
                max = min + 1;
            }

            drawLineSeries(g2, buyerOfferHistory, min, max, left, top, width - right, height - bottom, GREEN);
            drawLineSeries(g2, dealerAskHistory, min, max, left, top, width - right, height - bottom, ACCENT);

            g2.setFont(bodyFont);
            g2.setColor(GREEN);
            g2.fillRect(left, height - 28, 10, 10);
            g2.setColor(TEXT_MUTED);
            g2.drawString("Buyer Offer", left + 16, height - 19);

            g2.setColor(ACCENT);
            g2.fillRect(left + 115, height - 28, 10, 10);
            g2.setColor(TEXT_MUTED);
            g2.drawString("Dealer Ask", left + 131, height - 19);

            g2.dispose();
        }

        private void drawLineSeries(
                Graphics2D g2,
                java.util.List<Double> values,
                double min,
                double max,
                int left,
                int top,
                int right,
                int bottom,
                Color color
        ) {
            if (values.size() == 1) {
                int x = left;
                int y = scaleY(values.get(0), min, max, top, bottom);
                g2.setColor(color);
                g2.fillOval(x - 4, y - 4, 8, 8);
                return;
            }

            g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(color);

            int previousX = 0;
            int previousY = 0;

            for (int i = 0; i < values.size(); i++) {
                int x = left + (int) ((right - left) * (i / (double) (values.size() - 1)));
                int y = scaleY(values.get(i), min, max, top, bottom);

                g2.fillOval(x - 4, y - 4, 8, 8);

                if (i > 0) {
                    g2.drawLine(previousX, previousY, x, y);
                }

                previousX = x;
                previousY = y;
            }
        }

        private int scaleY(double value, double min, double max, int top, int bottom) {
            double ratio = (value - min) / (max - min);
            return bottom - (int) ((bottom - top) * ratio);
        }
    }

    private final class TreasuryChart extends JPanel {
        private static final long serialVersionUID = 1L;

        private TreasuryChart() {
            setBackground(TABLE_BG);
            setOpaque(true);
            setPreferredSize(new Dimension(10, 260));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2.setColor(TABLE_BG);
            g2.fillRect(0, 0, width, height);

            double revenue = fixedFees + commissions;
            double max = Math.max(1.0, Math.max(fixedFees, Math.max(commissions, revenue)));

            int baseY = height - 60;
            int barWidth = 52;
            int gap = 70;
            int startX = 55;

            drawBar(g2, startX, baseY, barWidth, fixedFees, max, ACCENT, "Fees");
            drawBar(g2, startX + barWidth + gap, baseY, barWidth, commissions, max, RED, "Commission");
            drawBar(g2, startX + (barWidth + gap) * 2, baseY, barWidth, revenue, max, GREEN, "Revenue");

            int textX = Math.max(startX + (barWidth + gap) * 3 + 20, width - 230);
            int textY = 70;

            g2.setFont(bodyBoldFont);
            g2.setColor(TEXT_MAIN);
            g2.drawString("Deal Volume", textX, textY);

            g2.setFont(bodyFont);
            g2.setColor(GREEN);
            g2.drawString("Successful: " + successfulDeals, textX, textY + 28);

            g2.setColor(RED);
            g2.drawString("Failed: " + failedDeals, textX, textY + 52);

            g2.setColor(TEXT_MUTED);
            g2.drawString("Total: " + negotiations, textX, textY + 76);

            g2.dispose();
        }

        private void drawBar(
                Graphics2D g2,
                int x,
                int baseY,
                int width,
                double value,
                double max,
                Color color,
                String label
        ) {
            int maxHeight = 150;
            int barHeight = (int) ((value / max) * maxHeight);
            barHeight = Math.max(2, barHeight);

            g2.setColor(color);
            g2.fillRoundRect(x, baseY - barHeight, width, barHeight, 8, 8);

            g2.setFont(bodyBoldFont);
            g2.setColor(TEXT_MAIN);
            g2.drawString(formatRM(value), x - 8, baseY - barHeight - 8);

            g2.setFont(bodyFont);
            g2.setColor(TEXT_MUTED);
            g2.drawString(label, x, baseY + 24);
        }
    }

    private static final class DealerListing {
        private final String agentName;
        private final String make;
        private final String model;
        private final int year;
        private final double askPrice;
        private final double minMargin;
        private final String strategy;

        private DealerListing(
                String agentName,
                String make,
                String model,
                int year,
                double askPrice,
                double minMargin,
                String strategy
        ) {
            this.agentName = agentName;
            this.make = make;
            this.model = model;
            this.year = year;
            this.askPrice = askPrice;
            this.minMargin = minMargin;
            this.strategy = strategy;
        }

        private String vehicleName() {
            return make + " " + model;
        }

        private double floorPrice() {
            return askPrice * (1.0 - minMargin);
        }
    }

    private static final class BuyerRequest {
        private final String agentName;
        private final String make;
        private final String model;
        private final double openingBid;
        private final double maxBudget;
        private final int maxRounds;
        private final boolean manualMode;

        private BuyerRequest(
                String agentName,
                String make,
                String model,
                double openingBid,
                double maxBudget,
                int maxRounds,
                boolean manualMode
        ) {
            this.agentName = agentName;
            this.make = make;
            this.model = model;
            this.openingBid = openingBid;
            this.maxBudget = maxBudget;
            this.maxRounds = maxRounds;
            this.manualMode = manualMode;
        }

        private String targetName() {
            return make + " " + model;
        }
    }

    private static final class RoundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final Color backgroundColor;
        private final Color borderColor;
        private final int arc;

        private RoundPanel(Color backgroundColor, Color borderColor, int arc) {
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth() - 1;
            int height = getHeight() - 1;

            g2.setColor(backgroundColor);

            if (arc > 0) {
                g2.fillRoundRect(0, 0, width, height, arc, arc);
            } else {
                g2.fillRect(0, 0, width, height);
            }

            if (borderColor != null) {
                g2.setColor(borderColor);

                if (arc > 0) {
                    g2.drawRoundRect(0, 0, width, height, arc, arc);
                } else {
                    g2.drawRect(0, 0, width, height);
                }
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class DashboardPanel extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(120, visibleRect.height - 80);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            Container parent = getParent();

            if (parent == null) {
                return true;
            }

            return parent.getWidth() >= MIN_PAGE_WIDTH;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuctionDashboard dashboard = new AuctionDashboard();
            dashboard.setVisible(true);

            JScrollBar verticalBar = ((JScrollPane) dashboard.getContentPane()).getVerticalScrollBar();
            verticalBar.setValue(0);
        });
    }
}