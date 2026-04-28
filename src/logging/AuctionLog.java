package logging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuctionLog {

    public interface Listener {
        void onEvent(AuctionEvent event);
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<Listener>();

    private AuctionLog() {
    }

    public static void addListener(Listener listener) {
        LISTENERS.add(listener);
    }

    public static void removeListener(Listener listener) {
        LISTENERS.remove(listener);
    }

    public static void info(String source, String message) {
        publish(new AuctionEvent("INFO", source, message));
    }

    public static void warn(String source, String message) {
        publish(new AuctionEvent("WARN", source, message));
    }

    public static void error(String source, String message) {
        publish(new AuctionEvent("ERROR", source, message));
    }

    private static void publish(AuctionEvent event) {
        for (Listener listener : LISTENERS) {
            listener.onEvent(event);
        }
    }
}
