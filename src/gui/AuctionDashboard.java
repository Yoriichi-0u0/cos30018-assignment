package gui;

import controller.AuctionPlatformController;
import logging.AuctionEvent;
import logging.AuctionLog;
import models.BuyerProfile;
import models.Car;
import models.DealerListing;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionDashboard extends JFrame {

    private static final int LAYOUT_COMPACT = 0;
    private static final int LAYOUT_MEDIUM = 1;
    private static final int LAYOUT_WIDE = 2;

    private static final Color APP_BACKGROUND = new Color(11, 14, 18);
    private static final Color CARD_BACKGROUND = new Color(23, 27, 35, 228);
    private static final Color CARD_BORDER = new Color(201, 162, 39, 110);
    private static final Color CARD_INSET = new Color(31, 36, 46);
    private static final Color TEXT_PRIMARY = new Color(244, 238, 221);
    private static final Color TEXT_MUTED = new Color(176, 168, 147);
    private static final Color GOLD = new Color(212, 166, 61);
    private static final Color GOLD_HOVER = new Color(236, 192, 87);
    private static final Color CRIMSON = new Color(150, 39, 48);
    private static final Color CRIMSON_HOVER = new Color(181, 55, 66);
    private static final Color SLATE = new Color(62, 73, 88);
    private static final Color SLATE_HOVER = new Color(83, 95, 111);
    private static final DecimalFormat MONEY = new DecimalFormat("RM#,##0.00");

    private final AuctionPlatformController controller = new AuctionPlatformController();
    private final ExecutorService actionExecutor = Executors.newSingleThreadExecutor();
    private GradientPanel rootPanel;
    private JPanel workspacePanel;
    private JPanel feedPanel;
    private JSplitPane mainSplitPane;
    private int activeLayoutMode = -1;

    private final JLabel brokerCountValue = createMetricValue();
    private final JLabel dealerCountValue = createMetricValue();
    private final JLabel buyerCountValue = createMetricValue();
    private final JLabel platformStatusValue = createMetricValue();

    private final JTextField dealerNameField = createField("dealerHarbor");
    private final JTextField dealerMakeField = createField("Toyota");
    private final JTextField dealerModelField = createField("Camry");
    private final JTextField dealerYearField = createField("2022");
    private final JTextField dealerPriceField = createField("123000");
    private final JTextField dealerMarginField = createField("0.10");
    private final JComboBox<String> dealerStrategyCombo = createComboBox(new String[]{"Stubborn", "Desperate", "Matcher"});

    private final JTextField buyerNameField = createField("buyerAva");
    private final JTextField buyerMakeField = createField("Toyota");
    private final JTextField buyerModelField = createField("Camry");
    private final JTextField buyerOpeningField = createField("108000");
    private final JTextField buyerMaxField = createField("128000");
    private final JTextField buyerRoundsField = createField("5");
    private final JCheckBox manualModeCheck = new JCheckBox("Enable Manual Negotiation UI");

    private final DefaultTableModel showroomModel = new DefaultTableModel(new Object[]{"Dealer", "Vehicle", "Year", "Ask", "Floor"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel buyerModel = new DefaultTableModel(new Object[]{"Buyer", "Target", "Opening", "Ceiling", "Rounds"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTextPane activityFeed = new JTextPane();
    private final AuctionLog.Listener logListener = new AuctionLog.Listener() {
        @Override
        public void onEvent(AuctionEvent event) {
            appendLog(event);
            refreshSnapshot();
        }
    };

    public AuctionDashboard() {
        super("Velvet Hammer Auto Auction");
        configureWindow();
        setContentPane(buildContent());
        configureLogStyles();
        loadDefaults();
        AuctionLog.addListener(logListener);
        AuctionLog.info("Dashboard", "Auction dashboard is ready. Start the floor or launch the demo lineup.");
        refreshSnapshot();
    }

    private void configureWindow() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(860, 640));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowWidth = Math.max(980, Math.min(1520, screenSize.width - 80));
        int windowHeight = Math.max(720, Math.min(940, screenSize.height - 80));
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshResponsiveLayout();
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                AuctionLog.removeListener(logListener);
                actionExecutor.shutdownNow();
                controller.shutdownPlatform();
            }
        });
    }

    private JComponent buildContent() {
        rootPanel = new GradientPanel();
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        workspacePanel = new JPanel();
        workspacePanel.setOpaque(false);
        workspacePanel.setLayout(new BoxLayout(workspacePanel, BoxLayout.Y_AXIS));

        JScrollPane workspaceScroll = createWorkspaceScrollPane(workspacePanel);

        feedPanel = new JPanel(new BorderLayout());
        feedPanel.setOpaque(false);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, workspaceScroll, feedPanel);
        mainSplitPane.setOpaque(false);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder());
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setDividerSize(10);
        mainSplitPane.setResizeWeight(0.72);

        rootPanel.add(mainSplitPane, BorderLayout.CENTER);
        refreshResponsiveLayout();
        return rootPanel;
    }

    private JComponent buildHeroCard(int layoutMode) {
        CardPanel card = new CardPanel();
        boolean stacked = layoutMode != LAYOUT_WIDE;
        card.setLayout(new BorderLayout(stacked ? 0 : 18, stacked ? 14 : 0));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("COS30018 Intelligent Systems");
        eyebrow.setForeground(GOLD);
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel title = new JLabel("Velvet Hammer Auto Auction");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font("Georgia", Font.BOLD, stacked ? 28 : 34));

        JLabel subtitle = new JLabel("<html>A broker-run negotiation floor for dealers and buyers, tailored for the car-auction theme in your assignment.</html>");
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JLabel note = new JLabel("<html>Use <b>Run Demo Lineup</b> to spawn 1 broker, 3 dealers, and 5 buyers with staged timing so the broker has inventory before negotiations begin.</html>");
        note.setForeground(new Color(228, 214, 183));
        note.setFont(new Font("SansSerif", Font.PLAIN, 14));

        textBlock.add(eyebrow);
        textBlock.add(Box.createVerticalStrut(8));
        textBlock.add(title);
        textBlock.add(Box.createVerticalStrut(10));
        textBlock.add(subtitle);
        textBlock.add(Box.createVerticalStrut(14));
        textBlock.add(note);

        JPanel badgeRail = buildResponsiveGrid(stacked ? (layoutMode == LAYOUT_MEDIUM ? 3 : 1) : 1, 12,
                createBadge("Theme", "Prestige car auction"),
                createBadge("Agents", "Broker + dealers + buyers"),
                createBadge("Stack", "JADE 4.6 + Swing")
        );
        if (!stacked) {
            badgeRail.setPreferredSize(new Dimension(260, 0));
        }

        card.add(textBlock, BorderLayout.CENTER);
        card.add(badgeRail, stacked ? BorderLayout.SOUTH : BorderLayout.EAST);
        return card;
    }

    private JComponent buildMetricStrip(int width) {
        int columns = width >= 1320 ? 4 : width >= 860 ? 2 : 1;
        return buildResponsiveGrid(columns, 16,
                createMetricCard("Broker", "Central facilitator", brokerCountValue),
                createMetricCard("Dealers", "Vehicles on the floor", dealerCountValue),
                createMetricCard("Buyers", "Automated bidders", buyerCountValue),
                createMetricCard("Platform", "Container status", platformStatusValue)
        );
    }

    private JComponent buildActionRow(int width) {
        int columns = width >= 1420 ? 3 : width >= 980 ? 2 : 1;
        return buildResponsiveGrid(columns, 16,
                buildControlCard(),
                buildDealerCard(),
                buildBuyerCard()
        );
    }

    private JComponent buildDataRow(int width) {
        int columns = width >= 1240 ? 2 : 1;
        return buildResponsiveGrid(columns, 16,
                buildShowroomCard(width),
                buildBuyerBoardCard(width)
        );
    }

    private JComponent buildControlCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(createSectionTitle("Auction Control", "Start the embedded JADE container, populate the floor, or clear the room."));
        card.add(Box.createVerticalStrut(18));

        JButton startButton = new ActionButton("Start Auction Floor", GOLD, GOLD_HOVER, APP_BACKGROUND);
        startButton.addActionListener(e -> submitAction("Starting auction floor", new UiTask() {
            @Override
            public void run() throws Exception {
                controller.ensurePlatformStarted();
            }
        }));

        JButton demoButton = new ActionButton("Run Demo Lineup", CRIMSON, CRIMSON_HOVER, TEXT_PRIMARY);
        demoButton.addActionListener(e -> submitAction("Launching staged demo lineup", new UiTask() {
            @Override
            public void run() throws Exception {
                controller.launchDemoScenario();
            }
        }));

        JButton resetButton = new ActionButton("Reset Platform", SLATE, SLATE_HOVER, TEXT_PRIMARY);
        resetButton.addActionListener(e -> submitAction("Resetting platform", new UiTask() {
            @Override
            public void run() {
                controller.shutdownPlatform();
            }
        }));

        card.add(startButton);
        card.add(Box.createVerticalStrut(12));
        card.add(demoButton);
        card.add(Box.createVerticalStrut(12));
        card.add(resetButton);
        card.add(Box.createVerticalGlue());

        JLabel hint = new JLabel("<html><b>Tip:</b> the demo lineup is the fastest way to satisfy the assignment baseline for 3 dealers and 5 buyers.</html>");
        hint.setForeground(TEXT_MUTED);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(hint);
        return card;
    }

    private JComponent buildDealerCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(createSectionTitle("Dealer Listing Booth", "Register a vehicle, asking price, and floor tolerance with the broker."));
        card.add(Box.createVerticalStrut(14));
        card.add(createFieldRow("Agent Name", dealerNameField));
        card.add(createFieldRow("Make", dealerMakeField));
        card.add(createFieldRow("Model", dealerModelField));
        card.add(createFieldRow("Year", dealerYearField));
        card.add(createFieldRow("List Price", dealerPriceField));
        card.add(createFieldRow("Min Margin", dealerMarginField));
        card.add(createFieldRow("Strategy", dealerStrategyCombo));
        card.add(Box.createVerticalStrut(12));

        JButton launchDealerButton = new ActionButton("List Vehicle", GOLD, GOLD_HOVER, APP_BACKGROUND);
        launchDealerButton.addActionListener(e -> handleDealerLaunch());
        card.add(launchDealerButton);
        return card;
    }

    private JComponent buildBuyerCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(createSectionTitle("Buyer Bidding Desk", "Spawn a buyer agent with an opening bid, a ceiling, and round limits."));
        card.add(Box.createVerticalStrut(14));
        card.add(createFieldRow("Agent Name", buyerNameField));
        card.add(createFieldRow("Make", buyerMakeField));
        card.add(createFieldRow("Model", buyerModelField));
        card.add(createFieldRow("Opening Bid", buyerOpeningField));
        card.add(createFieldRow("Max Budget", buyerMaxField));
        card.add(createFieldRow("Max Rounds", buyerRoundsField));
        card.add(Box.createVerticalStrut(12));
        
        manualModeCheck.setOpaque(false);
        manualModeCheck.setForeground(TEXT_PRIMARY);
        manualModeCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
        manualModeCheck.setFocusPainted(false);
        card.add(manualModeCheck);
        card.add(Box.createVerticalStrut(12));

        JButton launchBuyerButton = new ActionButton("Start Buyer Agent", CRIMSON, CRIMSON_HOVER, TEXT_PRIMARY);
        launchBuyerButton.addActionListener(e -> handleBuyerLaunch());
        card.add(launchBuyerButton);
        return card;
    }

    private JComponent buildShowroomCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 14));
        card.add(createSectionTitle("Showroom Board", "Live dealer inventory available on the broker floor."), BorderLayout.NORTH);
        card.add(createTablePane(createTable(showroomModel)), BorderLayout.CENTER);
        return card;
    }

    private JComponent buildShowroomCard(int width) {
        JComponent card = buildShowroomCard();
        card.setPreferredSize(new Dimension(0, width >= 1240 ? 320 : 290));
        return card;
    }

    private JComponent buildBuyerBoardCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 14));
        card.add(createSectionTitle("Buyer Board", "Automated bidders currently scouting and negotiating."), BorderLayout.NORTH);
        card.add(createTablePane(createTable(buyerModel)), BorderLayout.CENTER);
        return card;
    }

    private JComponent buildBuyerBoardCard(int width) {
        JComponent card = buildBuyerBoardCard();
        card.setPreferredSize(new Dimension(0, width >= 1240 ? 320 : 290));
        return card;
    }

    private JComponent buildFeedCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 14));
        card.setPreferredSize(new Dimension(390, 440));
        card.add(createSectionTitle("Live Auction Feed", "Broker updates, bidding moves, and confirmed deals."), BorderLayout.NORTH);

        activityFeed.setEditable(false);
        activityFeed.setOpaque(false);
        activityFeed.setForeground(TEXT_PRIMARY);
        activityFeed.setFont(new Font("Monospaced", Font.PLAIN, 12));
        activityFeed.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(activityFeed);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 24)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void refreshResponsiveLayout() {
        if (rootPanel == null || workspacePanel == null || feedPanel == null || mainSplitPane == null) {
            return;
        }

        int width = Math.max(getWidth(), getContentPane().getWidth());
        int layoutMode = determineLayoutMode(width);
        if (layoutMode != activeLayoutMode || workspacePanel.getComponentCount() == 0) {
            activeLayoutMode = layoutMode;
            int contentWidth = Math.max(760, width - (layoutMode == LAYOUT_WIDE ? 430 : 80));

            workspacePanel.removeAll();
            workspacePanel.add(buildHeroCard(layoutMode));
            workspacePanel.add(Box.createVerticalStrut(18));
            workspacePanel.add(buildMetricStrip(contentWidth));
            workspacePanel.add(Box.createVerticalStrut(18));
            workspacePanel.add(buildActionRow(contentWidth));
            workspacePanel.add(Box.createVerticalStrut(18));
            workspacePanel.add(buildDataRow(contentWidth));
            workspacePanel.add(Box.createVerticalStrut(18));

            feedPanel.removeAll();
            feedPanel.add(buildFeedCard(), BorderLayout.CENTER);
        }

        if (layoutMode == LAYOUT_WIDE) {
            mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            mainSplitPane.setResizeWeight(0.72);
            mainSplitPane.setDividerLocation(Math.max(680, (int) (width * 0.7)));
            rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        } else {
            mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            mainSplitPane.setResizeWeight(0.68);
            mainSplitPane.setDividerLocation(Math.max(360, (int) (getHeight() * 0.62)));
            rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        }

        workspacePanel.revalidate();
        workspacePanel.repaint();
        feedPanel.revalidate();
        feedPanel.repaint();
        mainSplitPane.revalidate();
    }

    private int determineLayoutMode(int width) {
        if (width >= 1360) {
            return LAYOUT_WIDE;
        }
        if (width >= 980) {
            return LAYOUT_MEDIUM;
        }
        return LAYOUT_COMPACT;
    }

    private void handleDealerLaunch() {
        final String requestedName = dealerNameField.getText().trim();
        final String make = dealerMakeField.getText().trim();
        final String model = dealerModelField.getText().trim();
        final String strategy = (String) dealerStrategyCombo.getSelectedItem();

        try {
            final int year = Integer.parseInt(dealerYearField.getText().trim());
            final double listPrice = Double.parseDouble(dealerPriceField.getText().trim());
            final double minMargin = Double.parseDouble(dealerMarginField.getText().trim());

            submitAction("Opening dealer booth", new UiTask() {
                @Override
                public void run() throws Exception {
                    controller.launchDealer(requestedName, new Car(make, model, year, listPrice), minMargin, strategy);
                }
            });
        } catch (NumberFormatException ex) {
            showInputError("Dealer year, list price, and min margin must be valid numbers.");
        }
    }

    private void handleBuyerLaunch() {
        final String requestedName = buyerNameField.getText().trim();
        final String make = buyerMakeField.getText().trim();
        final String model = buyerModelField.getText().trim();

        try {
            final double openingBid = Double.parseDouble(buyerOpeningField.getText().trim());
            final double maxPrice = Double.parseDouble(buyerMaxField.getText().trim());
            final int maxRounds = Integer.parseInt(buyerRoundsField.getText().trim());
            final boolean isManual = manualModeCheck.isSelected();

            if (openingBid > maxPrice) {
                showInputError("Opening bid should not be higher than the buyer's max budget.");
                return;
            }

            submitAction("Sending buyer to floor", new UiTask() {
                @Override
                public void run() throws Exception {
                    controller.launchBuyer(requestedName, make, model, openingBid, maxPrice, maxRounds, isManual);
                }
            });
        } catch (NumberFormatException ex) {
            showInputError("Buyer opening bid, max budget, and rounds must be valid numbers.");
        }
    }

    private void submitAction(final String description, final UiTask task) {
        AuctionLog.info("Dashboard", description + "...");
        actionExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                    refreshSnapshot();
                } catch (Exception ex) {
                    AuctionLog.error("Dashboard", ex.getMessage());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(AuctionDashboard.this, ex.getMessage(), "Action Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            }
        });
    }

    private void refreshSnapshot() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                brokerCountValue.setText(String.valueOf(controller.getBrokerCount()));
                dealerCountValue.setText(String.valueOf(controller.getDealerCount()));
                buyerCountValue.setText(String.valueOf(controller.getBuyerCount()));
                platformStatusValue.setText(controller.isRunning() ? "LIVE" : "OFF");

                rebuildShowroom(controller.getDealerListings());
                rebuildBuyerBoard(controller.getBuyerProfiles());
            }
        });
    }

    private void rebuildShowroom(List<DealerListing> listings) {
        showroomModel.setRowCount(0);
        for (DealerListing listing : listings) {
            showroomModel.addRow(new Object[]{
                    listing.getAgentName(),
                    listing.getCar().getMake() + " " + listing.getCar().getModel(),
                    listing.getCar().getYear(),
                    MONEY.format(listing.getCar().getPrice()),
                    MONEY.format(listing.getFloorPrice())
            });
        }
    }

    private void rebuildBuyerBoard(List<BuyerProfile> buyers) {
        buyerModel.setRowCount(0);
        for (BuyerProfile buyer : buyers) {
            buyerModel.addRow(new Object[]{
                    buyer.getAgentName(),
                    buyer.getTargetLabel(),
                    MONEY.format(buyer.getInitialOffer()),
                    MONEY.format(buyer.getMaxPrice()),
                    buyer.getMaxNegotiationRounds()
            });
        }
    }

    private void configureLogStyles() {
        StyledDocument doc = activityFeed.getStyledDocument();

        Style timeStyle = doc.addStyle("time", null);
        StyleConstants.setForeground(timeStyle, TEXT_MUTED);
        StyleConstants.setFontFamily(timeStyle, "Monospaced");

        Style sourceStyle = doc.addStyle("source", null);
        StyleConstants.setForeground(sourceStyle, GOLD);
        StyleConstants.setBold(sourceStyle, true);

        Style infoStyle = doc.addStyle("INFO", null);
        StyleConstants.setForeground(infoStyle, TEXT_PRIMARY);

        Style warnStyle = doc.addStyle("WARN", null);
        StyleConstants.setForeground(warnStyle, new Color(255, 204, 102));

        Style errorStyle = doc.addStyle("ERROR", null);
        StyleConstants.setForeground(errorStyle, new Color(255, 120, 120));
    }

    private void appendLog(final AuctionEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StyledDocument doc = activityFeed.getStyledDocument();
                try {
                    doc.insertString(doc.getLength(), event.getTimestamp() + " ", doc.getStyle("time"));
                    doc.insertString(doc.getLength(), "[" + event.getSource() + "] ", doc.getStyle("source"));
                    doc.insertString(doc.getLength(), event.getMessage() + "\n", doc.getStyle(event.getLevel()));
                    activityFeed.setCaretPosition(doc.getLength());
                } catch (BadLocationException ignored) {
                }
            }
        });
    }

    private void loadDefaults() {
        java.util.Properties props = new java.util.Properties();
        try (java.io.InputStream in = new java.io.FileInputStream("config.properties")) {
            props.load(in);
            if (props.getProperty("default_buyer_budget") != null) buyerMaxField.setText(props.getProperty("default_buyer_budget"));
            if (props.getProperty("max_rounds") != null) buyerRoundsField.setText(props.getProperty("max_rounds"));
            if (props.getProperty("default_dealer_margin") != null) dealerMarginField.setText(props.getProperty("default_dealer_margin"));
            AuctionLog.info("System", "Loaded external defaults from config.properties.");
        } catch (Exception e) {
            AuctionLog.warn("System", "Could not load config.properties, using hardcoded GUI defaults.");
        }
    }

    private JPanel createMetricCard(String title, String subtitle, JLabel valueLabel) {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(0, 150));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitleLabel);
        return card;
    }

    private JLabel createMetricValue() {
        JLabel value = new JLabel("0");
        value.setForeground(TEXT_PRIMARY);
        value.setFont(new Font("Georgia", Font.BOLD, 30));
        value.setHorizontalAlignment(SwingConstants.LEFT);
        return value;
    }

    private JPanel createBadge(String heading, String content) {
        CardPanel badge = new CardPanel();
        badge.setLayout(new BoxLayout(badge, BoxLayout.Y_AXIS));
        badge.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setForeground(GOLD);
        headingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel contentLabel = new JLabel(content);
        contentLabel.setForeground(TEXT_PRIMARY);
        contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        badge.add(headingLabel);
        badge.add(Box.createVerticalStrut(6));
        badge.add(contentLabel);
        return badge;
    }

    private JPanel createSectionTitle(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 22));

        JLabel subtitleLabel = new JLabel("<html>" + subtitle + "</html>");
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);
        return header;
    }

    private JPanel createFieldRow(String label, Component field) {
        JPanel row = new JPanel(new BorderLayout(0, 6));
        row.setOpaque(false);

        JLabel rowLabel = new JLabel(label);
        rowLabel.setForeground(TEXT_MUTED);
        rowLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        row.add(rowLabel, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        return row;
    }

    private JTextField createField(String value) {
        JTextField field = new JTextField(value);
        field.setBackground(CARD_INSET);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(GOLD);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 28)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    private JComboBox<String> createComboBox(String[] options) {
        JComboBox<String> combo = new JComboBox<String>(options);
        combo.setBackground(CARD_INSET);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 28)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return combo;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(32);
        table.setBackground(CARD_INSET);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(255, 255, 255, 18));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(212, 166, 61, 70));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.getTableHeader().setBackground(new Color(38, 43, 54));
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    private JScrollPane createTablePane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 24)));
        scrollPane.getViewport().setBackground(CARD_INSET);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    private JScrollPane createWorkspaceScrollPane(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JPanel buildResponsiveGrid(int columns, int gap, JComponent... components) {
        int safeColumns = Math.max(1, columns);
        int rows = (int) Math.ceil(components.length / (double) safeColumns);
        JPanel panel = new JPanel(new GridLayout(rows, safeColumns, gap, gap));
        panel.setOpaque(false);
        for (JComponent component : components) {
            panel.add(component);
        }
        return panel;
    }

    private void showInputError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    private interface UiTask {
        void run() throws Exception;
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint background = new GradientPaint(0, 0, new Color(8, 12, 16), getWidth(), getHeight(), new Color(41, 18, 20));
            g2.setPaint(background);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(212, 166, 61, 26));
            g2.fill(new Ellipse2D.Double(getWidth() - 360, -120, 420, 420));
            g2.setColor(new Color(169, 43, 52, 24));
            g2.fill(new Ellipse2D.Double(-180, getHeight() - 260, 460, 460));
            g2.dispose();
        }
    }

    private static class CardPanel extends JPanel {
        CardPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, getWidth() - 1.0, getHeight() - 1.0, 24, 24);
            g2.setColor(CARD_BACKGROUND);
            g2.fill(shape);
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(shape);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ActionButton extends JButton {

        private final Color baseColor;
        private final Color hoverColor;
        private final Color textColor;
        private boolean hovered;

        ActionButton(String text, Color baseColor, Color hoverColor, Color textColor) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;
            setForeground(textColor);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 42));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? hoverColor : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
