package gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public final class ManualNegotiationUI {

    private static final Color PANEL_BG = new Color(22, 24, 32);
    private static final Color CARD_BG = new Color(31, 36, 46);
    private static final Color INPUT_BG = new Color(32, 38, 48);
    private static final Color TEXT_MAIN = new Color(244, 239, 224);
    private static final Color TEXT_MUTED = new Color(196, 188, 170);
    private static final Color ACCENT = new Color(218, 171, 57);
    private static final Color GREEN = new Color(88, 190, 132);
    private static final Color RED = new Color(168, 40, 52);
    private static final Color BLUE = new Color(67, 83, 99);
    private static final Color BORDER = new Color(57, 64, 78);

    private static final Font BODY = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 12);
    private static final Font TITLE = new Font("Serif", Font.BOLD, 22);
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.US);

    static {
        MONEY.setMinimumFractionDigits(2);
        MONEY.setMaximumFractionDigits(2);
    }

    private ManualNegotiationUI() {
    }

    public static Decision showNegotiationDialog(
            JFrame owner,
            String buyerName,
            String dealerName,
            String vehicle,
            double currentBuyerOffer,
            double currentDealerAsk,
            int roundNumber,
            double maxBudget,
            String negotiationLog
    ) {
        if (SwingUtilities.isEventDispatchThread()) {
            return showNegotiationDialogOnEdt(
                    owner,
                    buyerName,
                    dealerName,
                    vehicle,
                    currentBuyerOffer,
                    currentDealerAsk,
                    roundNumber,
                    maxBudget,
                    negotiationLog
            );
        }

        AtomicReference<Decision> result = new AtomicReference<>(Decision.closed());
        try {
            SwingUtilities.invokeAndWait(() -> result.set(showNegotiationDialogOnEdt(
                    owner,
                    buyerName,
                    dealerName,
                    vehicle,
                    currentBuyerOffer,
                    currentDealerAsk,
                    roundNumber,
                    maxBudget,
                    negotiationLog
            )));
        } catch (Exception ignored) {
            result.set(Decision.rejected());
        }
        return result.get();
    }

    public static void showNoMatchDialog(JFrame owner, String buyerName, String vehicle, String message) {
        Runnable task = () -> {
            JDialog dialog = createDialog(owner, "Manual Negotiation - No Matching Dealer", false);
            JPanel panel = createRootPanel();
            panel.add(createTitle("Manual Negotiation"), BorderLayout.NORTH);

            JTextArea logArea = createLogArea(
                    "Buyer: " + buyerName + "\n"
                            + "Dealer: No matching dealer\n"
                            + "Vehicle: " + vehicle + "\n"
                            + "Round: N/A\n\n"
                            + message
            );

            JScrollPane scrollPane = new JScrollPane(logArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
            scrollPane.getViewport().setBackground(INPUT_BG);
            panel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);
            JButton close = createButton("Close", BLUE, Color.WHITE);
            close.addActionListener(e -> dialog.dispose());
            buttons.add(close);
            panel.add(buttons, BorderLayout.SOUTH);

            dialog.setContentPane(panel);
            dialog.setVisible(true);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    public static String getCounterOffer(
            String buyerName,
            String dealerName,
            String carDetails,
            double dealerPrice,
            double maxBudget,
            int round
    ) {
        double suggestedOffer = Math.min(maxBudget, dealerPrice * 0.95);
        Decision decision = showNegotiationDialog(
                null,
                buyerName,
                dealerName,
                carDetails,
                suggestedOffer,
                dealerPrice,
                round,
                maxBudget,
                "Dealer proposed " + formatRM(dealerPrice) + "."
        );

        if (decision.isAccepted()) {
            return String.valueOf(dealerPrice);
        }

        if (decision.isCountered()) {
            return String.valueOf(decision.getCounterOffer());
        }

        return "REJECT";
    }

    private static Decision showNegotiationDialogOnEdt(
            JFrame owner,
            String buyerName,
            String dealerName,
            String vehicle,
            double currentBuyerOffer,
            double currentDealerAsk,
            int roundNumber,
            double maxBudget,
            String negotiationLog
    ) {
        AtomicReference<Decision> result = new AtomicReference<>(Decision.closed());
        JDialog dialog = createDialog(owner, "Manual Negotiation - " + buyerName, true);

        JPanel root = createRootPanel();
        root.add(createTitle("Manual Negotiation"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(createDetailsPanel(
                buyerName,
                dealerName,
                vehicle,
                currentBuyerOffer,
                currentDealerAsk,
                roundNumber,
                maxBudget
        ), BorderLayout.NORTH);

        JTextArea logArea = createLogArea(negotiationLog == null ? "" : negotiationLog);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        logScroll.getViewport().setBackground(INPUT_BG);
        body.add(logScroll, BorderLayout.CENTER);

        JTextField counterField = createCounterField(currentBuyerOffer);
        body.add(counterField, BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);
        root.add(createButtonPanel(dialog, counterField, result), BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
        return result.get();
    }

    private static JDialog createDialog(Frame owner, String title, boolean modal) {
        JDialog dialog = new JDialog(owner, title, modal);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setMinimumSize(new Dimension(560, 500));
        dialog.setPreferredSize(new Dimension(620, 540));
        dialog.setSize(new Dimension(620, 540));
        dialog.setLocationRelativeTo(owner);
        return dialog;
    }

    private static JPanel createRootPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(true);
        root.setBackground(PANEL_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        return root;
    }

    private static JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_MAIN);
        label.setFont(TITLE);
        return label;
    }

    private static JPanel createDetailsPanel(
            String buyerName,
            String dealerName,
            String vehicle,
            double currentBuyerOffer,
            double currentDealerAsk,
            int roundNumber,
            double maxBudget
    ) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 8));
        panel.setOpaque(true);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 12, 12, 12)
        ));

        panel.add(createInfoLabel("Buyer", buyerName));
        panel.add(createInfoLabel("Dealer", dealerName));
        panel.add(createInfoLabel("Vehicle", vehicle));
        panel.add(createInfoLabel("Round", String.valueOf(roundNumber)));
        panel.add(createInfoLabel("Current Buyer Offer", formatRM(currentBuyerOffer)));
        panel.add(createInfoLabel("Current Dealer Ask", formatRM(currentDealerAsk) + " | Budget " + formatRM(maxBudget)));

        return panel;
    }

    private static JPanel createInfoLabel(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setFont(BODY_BOLD);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(TEXT_MAIN);
        valueLabel.setFont(BODY);
        valueLabel.setToolTipText(value);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private static JTextArea createLogArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(true);
        area.setBackground(INPUT_BG);
        area.setForeground(TEXT_MAIN);
        area.setCaretColor(TEXT_MAIN);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        return area;
    }

    private static JTextField createCounterField(double suggestedOffer) {
        JTextField field = new JTextField(MONEY.format(suggestedOffer).replace(",", ""));
        field.setFont(BODY);
        field.setForeground(TEXT_MAIN);
        field.setBackground(INPUT_BG);
        field.setCaretColor(TEXT_MAIN);
        field.setSelectionColor(new Color(74, 85, 103));
        field.setSelectedTextColor(TEXT_MAIN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Counter Offer (RM)",
                        0,
                        0,
                        BODY_BOLD,
                        TEXT_MUTED
                )
        ));
        return field;
    }

    private static JPanel createButtonPanel(
            JDialog dialog,
            JTextField counterField,
            AtomicReference<Decision> result
    ) {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton counter = createButton("Send Counter Offer", ACCENT, Color.BLACK);
        JButton accept = createButton("Accept Deal", GREEN, Color.BLACK);
        JButton reject = createButton("Reject Deal", RED, Color.WHITE);
        JButton close = createButton("Close", BLUE, Color.WHITE);

        counter.addActionListener(e -> {
            try {
                double value = Double.parseDouble(counterField.getText().trim().replace(",", ""));
                if (value <= 0) {
                    throw new NumberFormatException("Counter offer must be positive.");
                }
                result.set(Decision.counter(value));
                dialog.dispose();
            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Please enter a valid counter offer.",
                        "Manual Negotiation",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        accept.addActionListener(e -> {
            result.set(Decision.accepted());
            dialog.dispose();
        });

        reject.addActionListener(e -> {
            result.set(Decision.rejected());
            dialog.dispose();
        });

        close.addActionListener(e -> {
            result.set(Decision.closed());
            dialog.dispose();
        });

        buttons.add(counter);
        buttons.add(accept);
        buttons.add(reject);
        buttons.add(Box.createHorizontalStrut(6));
        buttons.add(close);

        return buttons;
    }

    private static JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setRolloverEnabled(false);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(BODY_BOLD);
        button.setBorder(new EmptyBorder(9, 12, 9, 12));
        return button;
    }

    private static String formatRM(double value) {
        return "RM" + MONEY.format(value);
    }

    public static final class Decision {
        private final String action;
        private final double counterOffer;

        private Decision(String action, double counterOffer) {
            this.action = action;
            this.counterOffer = counterOffer;
        }

        public static Decision counter(double counterOffer) {
            return new Decision("COUNTER", counterOffer);
        }

        public static Decision accepted() {
            return new Decision("ACCEPT", 0.0);
        }

        public static Decision rejected() {
            return new Decision("REJECT", 0.0);
        }

        public static Decision closed() {
            return new Decision("CLOSE", 0.0);
        }

        public boolean isCountered() {
            return "COUNTER".equals(action);
        }

        public boolean isAccepted() {
            return "ACCEPT".equals(action);
        }

        public boolean isRejected() {
            return "REJECT".equals(action);
        }

        public boolean isClosed() {
            return "CLOSE".equals(action);
        }

        public double getCounterOffer() {
            return counterOffer;
        }
    }
}
