package UnitTesting.ArtjolZaimi;

import Model.ParkingSession;
import Enum.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;



// Unit Tests for M-13: ParkingSession.constructor

class ParkingSessionConstructorTest {

    @Test
    @DisplayName("Constructor with valid parameters")
    void testConstructor_ValidParameters() {
        LocalDateTime startTime = LocalDateTime.now();

        ParkingSession session = new ParkingSession(
                "session-1",
                "user-1",
                "ABC123",
                "zone-1",
                "spot-1",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                startTime
        );

        assertEquals("session-1", session.getId());
        assertEquals("user-1", session.getUserId());
        assertEquals("ABC123", session.getVehiclePlate());
        assertEquals("zone-1", session.getZoneId());
        assertEquals("spot-1", session.getSpotId());
        assertEquals(TimeOfDayBand.PEAK, session.getTimeOfDayBand());
        assertEquals(DayType.WEEKDAY, session.getDayType());
        assertEquals(ZoneType.STANDARD, session.getZoneType());
        assertEquals(startTime, session.getStartTime());
        assertEquals(SessionState.OPEN, session.getState());
    }

    @Test
    @DisplayName("Constructor throws exception for null id")
    void testConstructor_NullId() {
        assertThrows(NullPointerException.class, () -> {
            new ParkingSession(
                    null,
                    "user-1",
                    "ABC123",
                    "zone-1",
                    "spot-1",
                    TimeOfDayBand.PEAK,
                    DayType.WEEKDAY,
                    ZoneType.STANDARD,
                    LocalDateTime.now()
            );
        });
    }

    @Test
    @DisplayName("Constructor throws exception for null userId")
    void testConstructor_NullUserId() {
        assertThrows(NullPointerException.class, () -> {
            new ParkingSession(
                    "session-1",
                    null,
                    "ABC123",
                    "zone-1",
                    "spot-1",
                    TimeOfDayBand.PEAK,
                    DayType.WEEKDAY,
                    ZoneType.STANDARD,
                    LocalDateTime.now()
            );
        });
    }

    @Test
    @DisplayName("Constructor throws exception for null startTime")
    void testConstructor_NullStartTime() {
        assertThrows(NullPointerException.class, () -> {
            new ParkingSession(
                    "session-1",
                    "user-1",
                    "ABC123",
                    "zone-1",
                    "spot-1",
                    TimeOfDayBand.PEAK,
                    DayType.WEEKDAY,
                    ZoneType.STANDARD,
                    null
            );
        });
    }

    @Test
    @DisplayName("Constructor initializes with OPEN state")
    void testConstructor_InitialStateOpen() {
        ParkingSession session = new ParkingSession(
                "session-1",
                "user-1",
                "ABC123",
                "zone-1",
                "spot-1",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        assertEquals(SessionState.OPEN, session.getState());
        assertNull(session.getEndTime());
    }

    @Test
    @DisplayName("Constructor with OFF_PEAK and HOLIDAY")
    void testConstructor_OffPeakHoliday() {
        LocalDateTime startTime = LocalDateTime.now();

        ParkingSession session = new ParkingSession(
                "session-2",
                "user-2",
                "XYZ789",
                "zone-2",
                "spot-2",
                TimeOfDayBand.OFF_PEAK,
                DayType.HOLIDAY,
                ZoneType.EV,
                startTime
        );

        assertEquals(TimeOfDayBand.OFF_PEAK, session.getTimeOfDayBand());
        assertEquals(DayType.HOLIDAY, session.getDayType());
        assertEquals(ZoneType.EV, session.getZoneType());
    }

    @Test
    @DisplayName("Constructor with VIP zone type")
    void testConstructor_VIPZone() {
        ParkingSession session = new ParkingSession(
                "session-vip",
                "user-vip",
                "VIP001",
                "zone-vip",
                "spot-vip",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        assertEquals(ZoneType.VIP, session.getZoneType());
    }
}
