/**
 * What this does
 *
 * This stores the Broker Treasury Dashboard values:
 *
 * Total negotiations
 * Successful deals
 * Failed deals
 * Fixed fees
 * Commissions
 * Total broker revenue
 * Success rate
 */

package analytics;

/**
 * Stores the Broker Agent's treasury statistics at one point in time.
 *
 * The assignment says the Broker Agent receives:
 * 1. fixed fee for each negotiation
 * 2. commission for each successful deal
 *
 * This class makes those values easy to display in the GUI.
 */
public class TreasurySnapshot {

    private final int totalNegotiations;
    private final int successfulDeals;
    private final int failedDeals;
    private final double totalFees;
    private final double totalCommissions;

    public TreasurySnapshot(
            int totalNegotiations,
            int successfulDeals,
            int failedDeals,
            double totalFees,
            double totalCommissions
    ) {
        this.totalNegotiations = totalNegotiations;
        this.successfulDeals = successfulDeals;
        this.failedDeals = failedDeals;
        this.totalFees = totalFees;
        this.totalCommissions = totalCommissions;
    }

    public int getTotalNegotiations() {
        return totalNegotiations;
    }

    public int getSuccessfulDeals() {
        return successfulDeals;
    }

    public int getFailedDeals() {
        return failedDeals;
    }

    public double getTotalFees() {
        return totalFees;
    }

    public double getTotalCommissions() {
        return totalCommissions;
    }

    public double getTotalRevenue() {
        return totalFees + totalCommissions;
    }

    public double getSuccessRate() {
        if (totalNegotiations == 0) {
            return 0.0;
        }

        return (successfulDeals * 100.0) / totalNegotiations;
    }
}