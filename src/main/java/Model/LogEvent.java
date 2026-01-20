package Model;

import java.time.Instant;
import java.time.LocalDateTime;

public class LogEvent {

    private final LocalDateTime timestamp;
    private final String type;
    private final String details;

    public LogEvent(LocalDateTime timestamp, String type, String details) {
        if (timestamp == null || type == null || details == null) {
            throw new IllegalArgumentException("LogEvent fields cannot be null");
        }
        this.timestamp = timestamp;
        this.type = type;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }
}