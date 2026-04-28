package logging;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AuctionEvent {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String level;
    private final String source;
    private final String message;
    private final String timestamp;

    public AuctionEvent(String level, String source, String message) {
        this.level = level;
        this.source = source;
        this.message = message;
        this.timestamp = LocalTime.now().format(TIME_FORMAT);
    }

    public String getLevel() {
        return level;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
