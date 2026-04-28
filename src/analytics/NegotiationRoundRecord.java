/**
 * What this does
 *
 * This class stores one negotiation round. The chart will use this to draw:
 *
 * Buyer Offer line
 * Dealer Ask line
 * Gap between both prices
 */

package analytics;

/**
 * Stores one row of negotiation data for the visual analytics chart.
 *
 * Example:
 * Round 1:
 * Buyer Offer = RM108000
 * Dealer Ask = RM128000
 * Gap = RM20000
 *
 * This class is intentionally simple because the GUI only needs to read
 * structured values and draw them.
 */
public class NegotiationRoundRecord {

    private final String sessionId;
    private final String buyerName;
    private final String dealerName;
    private final int round;
    private final double buyerOffer;
    private final double dealerAsk;
    private final double gap;
    private final String status;

    public NegotiationRoundRecord(
            String sessionId,
            String buyerName,
            String dealerName,
            int round,
            double buyerOffer,
            double dealerAsk,
            String status
    ) {
        this.sessionId = sessionId;
        this.buyerName = buyerName;
        this.dealerName = dealerName;
        this.round = round;
        this.buyerOffer = buyerOffer;
        this.dealerAsk = dealerAsk;
        this.gap = Math.max(0.0, dealerAsk - buyerOffer);
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getDealerName() {
        return dealerName;
    }

    public int getRound() {
        return round;
    }

    public double getBuyerOffer() {
        return buyerOffer;
    }

    public double getDealerAsk() {
        return dealerAsk;
    }

    public double getGap() {
        return gap;
    }

    public String getStatus() {
        return status;
    }
}