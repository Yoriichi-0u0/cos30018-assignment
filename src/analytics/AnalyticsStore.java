/**
 * What this does
 *
 * This is the bridge between JADE agents and the GUI.
 *
 * Your agents will call:
 *
 * AnalyticsStore.recordRound(...)
 * AnalyticsStore.recordSuccessfulDeal(...)
 * AnalyticsStore.recordFailedDeal(...)
 */

package analytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central storage for visual analytics data.
 *
 * Why this exists:
 * The JADE agents run the negotiation.
 * The Swing GUI draws charts.
 * This class sits in the middle and stores structured analytics data.
 *
 * It avoids the bad practice of parsing text from the Live Auction Feed.
 */
public final class AnalyticsStore {

    public interface Listener {
        void onAnalyticsChanged();
    }

    private static final Object LOCK = new Object();

    private static final List<NegotiationRoundRecord> ROUND_RECORDS =
            new ArrayList<NegotiationRoundRecord>();

    private static final List<Listener> LISTENERS =
            new CopyOnWriteArrayList<Listener>();

    private static final Set<String> STARTED_SESSIONS =
            new HashSet<String>();

    private static final Set<String> COMPLETED_SESSIONS =
            new HashSet<String>();

    private static String latestSessionId = null;

    private static int totalNegotiations = 0;
    private static int successfulDeals = 0;
    private static int failedDeals = 0;

    private static double totalFees = 0.0;
    private static double totalCommissions = 0.0;

    private AnalyticsStore() {
    }

    public static void addListener(Listener listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(Listener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Clears all analytics data.
     * Call this when the platform is reset.
     */
    public static void clear() {
        synchronized (LOCK) {
            ROUND_RECORDS.clear();
            STARTED_SESSIONS.clear();
            COMPLETED_SESSIONS.clear();

            latestSessionId = null;

            totalNegotiations = 0;
            successfulDeals = 0;
            failedDeals = 0;

            totalFees = 0.0;
            totalCommissions = 0.0;
        }

        notifyListeners();
    }

    /**
     * Records that a negotiation session has started.
     *
     * This is useful for the Broker Treasury Dashboard because the broker
     * earns a fixed fee for each negotiation connection.
     */
    public static void recordNegotiationStarted(
            String sessionId,
            String buyerName,
            String dealerName,
            double fixedFee
    ) {
        synchronized (LOCK) {
            if (!STARTED_SESSIONS.contains(sessionId)) {
                STARTED_SESSIONS.add(sessionId);
                totalNegotiations++;
                totalFees += fixedFee;
            }

            latestSessionId = sessionId;
        }

        notifyListeners();
    }

    /**
     * Records one round of buyer-offer vs dealer-ask data.
     *
     * This is what powers the real-time line chart.
     */
    public static void recordRound(
            String sessionId,
            String buyerName,
            String dealerName,
            int round,
            double buyerOffer,
            double dealerAsk,
            String status
    ) {
        synchronized (LOCK) {
            ROUND_RECORDS.add(new NegotiationRoundRecord(
                    sessionId,
                    buyerName,
                    dealerName,
                    round,
                    buyerOffer,
                    dealerAsk,
                    status
            ));

            latestSessionId = sessionId;
        }

        notifyListeners();
    }

    /**
     * Records a successful deal.
     *
     * The commission should usually be:
     * finalPrice * commissionRate
     */
    public static void recordSuccessfulDeal(
            String sessionId,
            double finalPrice,
            double commission
    ) {
        synchronized (LOCK) {
            if (!COMPLETED_SESSIONS.contains(sessionId)) {
                COMPLETED_SESSIONS.add(sessionId);
                successfulDeals++;
                totalCommissions += commission;
                latestSessionId = sessionId;
            }
        }

        notifyListeners();
    }

    /**
     * Records a failed negotiation.
     *
     * A failed negotiation can happen when:
     * 1. max rounds are reached
     * 2. buyer refuses
     * 3. dealer walks away
     */
    public static void recordFailedDeal(String sessionId) {
        synchronized (LOCK) {
            if (!COMPLETED_SESSIONS.contains(sessionId)) {
                COMPLETED_SESSIONS.add(sessionId);
                failedDeals++;
                latestSessionId = sessionId;
            }
        }

        notifyListeners();
    }

    public static List<NegotiationRoundRecord> getAllRoundsSnapshot() {
        synchronized (LOCK) {
            return new ArrayList<NegotiationRoundRecord>(ROUND_RECORDS);
        }
    }

    public static List<NegotiationRoundRecord> getLatestSessionRoundsSnapshot() {
        synchronized (LOCK) {
            ArrayList<NegotiationRoundRecord> latestRounds =
                    new ArrayList<NegotiationRoundRecord>();

            if (latestSessionId == null) {
                return latestRounds;
            }

            for (NegotiationRoundRecord record : ROUND_RECORDS) {
                if (latestSessionId.equals(record.getSessionId())) {
                    latestRounds.add(record);
                }
            }

            return latestRounds;
        }
    }

    public static TreasurySnapshot getTreasurySnapshot() {
        synchronized (LOCK) {
            return new TreasurySnapshot(
                    totalNegotiations,
                    successfulDeals,
                    failedDeals,
                    totalFees,
                    totalCommissions
            );
        }
    }

    private static void notifyListeners() {
        for (Listener listener : LISTENERS) {
            listener.onAnalyticsChanged();
        }
    }
}