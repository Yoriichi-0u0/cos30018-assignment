/**
 * What this does
 *
 * This creates the actual Visual Analytics section.
 *
 * It shows:
 *
 * Negotiations
 * Successful deals
 * Failed deals
 * Success rate
 * Fixed fees
 * Commissions
 * Total revenue
 * Latest gap
 * Buyer Offer vs Dealer Ask chart
 * Broker Treasury chart
 */

package gui;

import analytics.AnalyticsStore;
import analytics.NegotiationRoundRecord;
import analytics.TreasurySnapshot;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Visual analytics section for the negotiation dashboard.
 *
 * This panel adds:
 * 1. real-time negotiation line chart
 * 2. broker treasury dashboard
 * 3. success/failure and revenue summary
 *
 * This directly supports the assignment requirement for GUI visualization.
 */
public class VisualAnalyticsPanel extends JPanel implements AnalyticsStore.Listener {

    private static final Color CARD_BACKGROUND = new Color(23, 27, 35);
    private static final Color CARD_INSET = new Color(31, 36, 46);
    private static final Color CARD_BORDER = new Color(201, 162, 39, 110);
    private static final Color TEXT_PRIMARY = new Color(244, 238, 221);
    private static final Color TEXT_MUTED = new Color(176, 168, 147);
    private static final Color GOLD = new Color(212, 166, 61);
    private static final Color GREEN = new Color(87, 186, 128);
    private static final Color RED = new Color(220, 92, 92);
    private static final Color BLUE = new Color(95, 169, 255);

    private static final DecimalFormat MONEY = new DecimalFormat("RM#,##0.00");
    private static final DecimalFormat PERCENT = new DecimalFormat("0.0'%'");

    private final JLabel totalNegotiationsValue = createMetricValue();
    private final JLabel successfulDealsValue = createMetricValue();
    private final JLabel failedDealsValue = createMetricValue();
    private final JLabel successRateValue = createMetricValue();
    private final JLabel feesValue = createMetricValue();
    private final JLabel commissionsValue = createMetricValue();
    private final JLabel revenueValue = createMetricValue();

    private final NegotiationChartPanel negotiationChartPanel =
            new NegotiationChartPanel();

    private final TreasuryChartPanel treasuryChartPanel =
            new TreasuryChartPanel();

    public VisualAnalyticsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        AnalyticsStore.addListener(this);
        refreshFromStore();
    }

    /**
     * Call this when the main dashboard window closes.
     * It prevents the old panel from staying registered as a listener.
     */
    public void disposePanel() {
        AnalyticsStore.removeListener(this);
    }

    @Override
    public void onAnalyticsChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshFromStore();
            }
        });
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Visual Analytics");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font("Georgia", Font.BOLD, 24));

        JLabel subtitle = new JLabel(
                "<html>Real-time negotiation graph and Broker Treasury Dashboard.</html>"
        );
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel metricGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        metricGrid.setOpaque(false);
        metricGrid.add(createMetricCard("Negotiations", totalNegotiationsValue, "Started sessions"));
        metricGrid.add(createMetricCard("Successful", successfulDealsValue, "Closed deals"));
        metricGrid.add(createMetricCard("Failed", failedDealsValue, "Walkaway / rejected"));
        metricGrid.add(createMetricCard("Success Rate", successRateValue, "Successful / total"));
        metricGrid.add(createMetricCard("Fixed Fees", feesValue, "Broker connection fees"));
        metricGrid.add(createMetricCard("Commissions", commissionsValue, "2% successful deals"));
        metricGrid.add(createMetricCard("Revenue", revenueValue, "Fees + commissions"));
        metricGrid.add(createMetricCard("Latest Gap", negotiationChartPanel.getGapLabel(), "Dealer ask - buyer offer"));

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 14));
        charts.setOpaque(false);
        charts.add(wrapChart("Negotiation Gap Chart", negotiationChartPanel));
        charts.add(wrapChart("Broker Treasury Chart", treasuryChartPanel));

        body.add(metricGrid);
        body.add(Box.createVerticalStrut(14));
        body.add(charts);

        return body;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle) {
        JPanel card = new AnalyticsCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);

        return card;
    }

    private JLabel createMetricValue() {
        JLabel label = new JLabel("0");
        label.setForeground(TEXT_PRIMARY);
        label.setFont(new Font("Georgia", Font.BOLD, 22));
        return label;
    }

    private JPanel wrapChart(String title, JPanel chartPanel) {
        JPanel wrapper = new AnalyticsCard();
        wrapper.setLayout(new BorderLayout(0, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 18));

        wrapper.add(titleLabel, BorderLayout.NORTH);
        wrapper.add(chartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private void refreshFromStore() {
        TreasurySnapshot treasury = AnalyticsStore.getTreasurySnapshot();
        List<NegotiationRoundRecord> latestRounds =
                AnalyticsStore.getLatestSessionRoundsSnapshot();

        totalNegotiationsValue.setText(String.valueOf(treasury.getTotalNegotiations()));
        successfulDealsValue.setText(String.valueOf(treasury.getSuccessfulDeals()));
        failedDealsValue.setText(String.valueOf(treasury.getFailedDeals()));
        successRateValue.setText(PERCENT.format(treasury.getSuccessRate()));
        feesValue.setText(MONEY.format(treasury.getTotalFees()));
        commissionsValue.setText(MONEY.format(treasury.getTotalCommissions()));
        revenueValue.setText(MONEY.format(treasury.getTotalRevenue()));

        negotiationChartPanel.setRounds(latestRounds);
        treasuryChartPanel.setTreasury(treasury);
    }

    private static class AnalyticsCard extends JPanel {
        public AnalyticsCard() {
            setBackground(CARD_BACKGROUND);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)
            ));
        }
    }

    private static class NegotiationChartPanel extends JPanel {

        private List<NegotiationRoundRecord> rounds =
                new ArrayList<NegotiationRoundRecord>();

        private final JLabel gapLabel = new JLabel("RM0.00");

        public NegotiationChartPanel() {
            setBackground(CARD_INSET);
            setPreferredSize(new Dimension(360, 260));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            gapLabel.setForeground(TEXT_PRIMARY);
            gapLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        }

        public JLabel getGapLabel() {
            return gapLabel;
        }

        public void setRounds(List<NegotiationRoundRecord> rounds) {
            this.rounds = new ArrayList<NegotiationRoundRecord>(rounds);

            if (this.rounds.isEmpty()) {
                gapLabel.setText("RM0.00");
            } else {
                NegotiationRoundRecord latest =
                        this.rounds.get(this.rounds.size() - 1);
                gapLabel.setText(MONEY.format(latest.getGap()));
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            int left = 48;
            int right = 20;
            int top = 24;
            int bottom = 42;

            int chartWidth = width - left - right;
            int chartHeight = height - top - bottom;

            g2.setColor(new Color(255, 255, 255, 35));
            g2.drawLine(left, top, left, top + chartHeight);
            g2.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);

            if (rounds.isEmpty()) {
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Run a negotiation to display Buyer Offer vs Dealer Ask.", left + 10, top + 40);
                g2.dispose();
                return;
            }

            double minPrice = Double.MAX_VALUE;
            double maxPrice = 0.0;

            for (NegotiationRoundRecord record : rounds) {
                minPrice = Math.min(minPrice, record.getBuyerOffer());
                minPrice = Math.min(minPrice, record.getDealerAsk());
                maxPrice = Math.max(maxPrice, record.getBuyerOffer());
                maxPrice = Math.max(maxPrice, record.getDealerAsk());
            }

            double padding = Math.max(1000.0, (maxPrice - minPrice) * 0.12);
            minPrice = Math.max(0.0, minPrice - padding);
            maxPrice = maxPrice + padding;

            drawLine(g2, rounds, left, top, chartWidth, chartHeight, minPrice, maxPrice, true);
            drawLine(g2, rounds, left, top, chartWidth, chartHeight, minPrice, maxPrice, false);

            drawLegend(g2, left, height - 16);

            g2.dispose();
        }

        private void drawLine(
                Graphics2D g2,
                List<NegotiationRoundRecord> data,
                int left,
                int top,
                int chartWidth,
                int chartHeight,
                double minPrice,
                double maxPrice,
                boolean buyerLine
        ) {
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(buyerLine ? GREEN : GOLD);

            int previousX = -1;
            int previousY = -1;

            int count = data.size();

            for (int i = 0; i < count; i++) {
                NegotiationRoundRecord record = data.get(i);
                double value = buyerLine ? record.getBuyerOffer() : record.getDealerAsk();

                int x;
                if (count == 1) {
                    x = left + chartWidth / 2;
                } else {
                    x = left + (int) ((i / (double) (count - 1)) * chartWidth);
                }

                int y = top + chartHeight
                        - (int) (((value - minPrice) / (maxPrice - minPrice)) * chartHeight);

                if (previousX != -1) {
                    g2.drawLine(previousX, previousY, x, y);
                }

                g2.fillOval(x - 4, y - 4, 8, 8);

                previousX = x;
                previousY = y;
            }
        }

        private void drawLegend(Graphics2D g2, int x, int y) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

            g2.setColor(GREEN);
            g2.fillRect(x, y - 10, 12, 8);
            g2.setColor(TEXT_MUTED);
            g2.drawString("Buyer Offer", x + 18, y);

            g2.setColor(GOLD);
            g2.fillRect(x + 120, y - 10, 12, 8);
            g2.setColor(TEXT_MUTED);
            g2.drawString("Dealer Ask", x + 138, y);
        }
    }

    private static class TreasuryChartPanel extends JPanel {

        private TreasurySnapshot treasury =
                new TreasurySnapshot(0, 0, 0, 0.0, 0.0);

        public TreasuryChartPanel() {
            setBackground(CARD_INSET);
            setPreferredSize(new Dimension(360, 260));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        }

        public void setTreasury(TreasurySnapshot treasury) {
            this.treasury = treasury;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            int left = 40;
            int top = 28;
            int bottom = 48;
            int chartHeight = height - top - bottom;

            double revenue = treasury.getTotalRevenue();
            double maxRevenue = Math.max(1.0, revenue);

            int barWidth = 58;
            int gap = 34;

            drawMoneyBar(g2, "Fees", treasury.getTotalFees(), maxRevenue,
                    left, top, chartHeight, barWidth, GOLD);

            drawMoneyBar(g2, "Commission", treasury.getTotalCommissions(), maxRevenue,
                    left + barWidth + gap, top, chartHeight, barWidth, BLUE);

            drawMoneyBar(g2, "Revenue", treasury.getTotalRevenue(), maxRevenue,
                    left + (barWidth + gap) * 2, top, chartHeight, barWidth, GREEN);

            drawDealSummary(g2, left + (barWidth + gap) * 3 + 8, top + 28);

            g2.dispose();
        }

        private void drawMoneyBar(
                Graphics2D g2,
                String label,
                double value,
                double maxValue,
                int x,
                int top,
                int chartHeight,
                int barWidth,
                Color color
        ) {
            int barHeight = (int) ((value / maxValue) * chartHeight);
            int y = top + chartHeight - barHeight;

            g2.setColor(color);
            g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(label, x - 2, top + chartHeight + 18);

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(MONEY.format(value), x - 8, Math.max(18, y - 6));
        }

        private void drawDealSummary(Graphics2D g2, int x, int y) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(TEXT_PRIMARY);
            g2.drawString("Deal Volume", x, y);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

            g2.setColor(GREEN);
            g2.drawString("Successful: " + treasury.getSuccessfulDeals(), x, y + 28);

            g2.setColor(RED);
            g2.drawString("Failed: " + treasury.getFailedDeals(), x, y + 50);

            g2.setColor(TEXT_MUTED);
            g2.drawString("Total: " + treasury.getTotalNegotiations(), x, y + 72);
        }
    }
}
