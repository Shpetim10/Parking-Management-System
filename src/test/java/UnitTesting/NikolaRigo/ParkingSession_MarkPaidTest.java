package UnitTesting.NikolaRigo;

import Model.ParkingSession;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import Enum.TimeOfDayBand;
import Enum.DayType;
import Enum.ZoneType;
import Enum.SessionState;

class ParkingSession_MarkPaidTest {

    @Test
    void markPaid_ShouldSetStateToPaid() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-123",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        // Act
        session.markPaid();

        // Assert
        assertEquals(SessionState.PAID, session.getState());
    }

    @Test
    void markPaid_WhenCalledMultipleTimes_ShouldRemainPaid() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-123",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        // Act
        session.markPaid();
        session.markPaid();
        session.markPaid();

        // Assert
        assertEquals(SessionState.PAID, session.getState());
    }

    @Test
    void markPaid_WhenSessionIsOpen_ShouldChangeToPaid() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-123",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        // Assert initial state is OPEN
        assertEquals(SessionState.OPEN, session.getState());

        // Act
        session.markPaid();

        // Assert
        assertEquals(SessionState.PAID, session.getState());
    }

    @Test
    void markPaid_ShouldNotAffectOtherProperties() {
        // Arrange
        String id = "session-123";
        String userId = "user-456";
        String vehiclePlate = "ABC-1234";
        String zoneId = "zone-789";
        String spotId = "spot-001";
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;
        DayType dayType = DayType.WEEKDAY;
        ZoneType zoneType = ZoneType.VIP;
        LocalDateTime startTime = LocalDateTime.now();

        ParkingSession session = new ParkingSession(
                id, userId, vehiclePlate, zoneId, spotId,
                timeOfDayBand, dayType, zoneType, startTime
        );

        // Act
        session.markPaid();

        // Assert state changed
        assertEquals(SessionState.PAID, session.getState());

        // Assert other properties unchanged
        assertEquals(id, session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(vehiclePlate, session.getVehiclePlate());
        assertEquals(zoneId, session.getZoneId());
        assertEquals(spotId, session.getSpotId());
        assertEquals(timeOfDayBand, session.getTimeOfDayBand());
        assertEquals(dayType, session.getDayType());
        assertEquals(zoneType, session.getZoneType());
        assertEquals(startTime, session.getStartTime());
    }

    @Test
    void markPaid_ShouldTransitionFromOpenToPaid() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-123",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        SessionState initialState = session.getState();

        // Act
        session.markPaid();

        // Assert
        assertEquals(SessionState.OPEN, initialState);
        assertEquals(SessionState.PAID, session.getState());
    }
}
