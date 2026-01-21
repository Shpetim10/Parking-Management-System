package UnitTesting.NikolaRigo;

import Model.LogEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class LogEventConstructorTest {

    @Test
    void constructor_WithValidParameters_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "INFO";
        String details = "User logged in successfully";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertNotNull(logEvent);
        assertEquals(timestamp, logEvent.getTimestamp());
        assertEquals(type, logEvent.getType());
        assertEquals(details, logEvent.getDetails());
    }

    @Test
    void constructor_WithNullTimestamp_ShouldThrowIllegalArgumentException() {
        // Arrange
        String type = "INFO";
        String details = "User logged in";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LogEvent(null, type, details)
        );

        assertEquals("LogEvent fields cannot be null", exception.getMessage());
    }

    @Test
    void constructor_WithNullType_ShouldThrowIllegalArgumentException() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String details = "User logged in";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LogEvent(timestamp, null, details)
        );

        assertEquals("LogEvent fields cannot be null", exception.getMessage());
    }

    @Test
    void constructor_WithNullDetails_ShouldThrowIllegalArgumentException() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "INFO";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LogEvent(timestamp, type, null)
        );

        assertEquals("LogEvent fields cannot be null", exception.getMessage());
    }

    @Test
    void constructor_WithAllNullParameters_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new LogEvent(null, null, null)
        );
    }

    @Test
    void constructor_WithEmptyType_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "";
        String details = "Some details";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertNotNull(logEvent);
        assertEquals("", logEvent.getType());
    }

    @Test
    void constructor_WithEmptyDetails_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "ERROR";
        String details = "";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertNotNull(logEvent);
        assertEquals("", logEvent.getDetails());
    }

    @Test
    void constructor_WithPastTimestamp_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.parse("2020-01-01T00:00:00Z");
        String type = "DEBUG";
        String details = "Historical log entry";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertEquals(timestamp, logEvent.getTimestamp());
    }

    @Test
    void constructor_WithFutureTimestamp_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.parse("2030-12-31T23:59:59Z");
        String type = "WARN";
        String details = "Scheduled event";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertEquals(timestamp, logEvent.getTimestamp());
    }

    @Test
    void constructor_WithLongDetails_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "ERROR";
        String details = "A".repeat(1000);

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertEquals(1000, logEvent.getDetails().length());
    }

    @Test
    void constructor_WithSpecialCharactersInType_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "USER-ACTION_123!@#";
        String details = "Special event";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertEquals(type, logEvent.getType());
    }

    @Test
    void constructor_WithSpecialCharactersInDetails_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "INFO";
        String details = "User: john@example.com | Action: login | Status: success";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertEquals(details, logEvent.getDetails());
    }

    @Test
    void constructor_WithWhitespaceType_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "   ";
        String details = "Some details";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertEquals("   ", logEvent.getType());
    }

    @Test
    void constructor_WithWhitespaceDetails_ShouldCreateInstance() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String type = "INFO";
        String details = "   ";

        // Act
        LogEvent logEvent = new LogEvent(timestamp, type, details);

        // Assert
        assertEquals("   ", logEvent.getDetails());
    }

    @Test
    void constructor_WithDifferentLogTypes_ShouldCreateInstances() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        String[] types = {"INFO", "DEBUG", "WARN", "ERROR", "FATAL"};

        // Act & Assert
        for (String type : types) {
            LogEvent logEvent = new LogEvent(timestamp, type, "Test details");
            assertEquals(type, logEvent.getType());
        }
    }
}