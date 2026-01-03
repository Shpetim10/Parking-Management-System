package Model;

import java.time.Instant;

public class LogEvent {

    private final Instant timestamp;
    private final String type;
    private final String details;

    public LogEvent(Instant timestamp, String type, String details) {
        if (timestamp == null || type == null || details == null) {
            throw new IllegalArgumentException("LogEvent fields cannot be null");
        }
        this.timestamp = timestamp;
        this.type = type;
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }
}