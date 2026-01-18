package Artjol.UnitTesting;

import Model.ParkingSession;
import Enum.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

// Unit Tests for M-15: ParkingSession.isActive

class ParkingSessionIsActiveTest {

    private ParkingSession session;

    @BeforeEach
    void setUp() {
        session = new ParkingSession(
                "session-1",
                "user-1",
                "ABC123",
                "zone-1",
                "spot-1",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("isActive returns true for OPEN state")
    void testIsActive_OpenState() {
        assertTrue(session.isActive());
        assertEquals(SessionState.OPEN, session.getState());
    }

    @Test
    @DisplayName("isActive returns true for PAID state")
    void testIsActive_PaidState() {
        session.markPaid();

        assertTrue(session.isActive());
        assertEquals(SessionState.PAID, session.getState());
    }

    @Test
    @DisplayName("isActive returns false for CLOSED state")
    void testIsActive_ClosedState() {
        session.close(LocalDateTime.now());

        assertFalse(session.isActive());
        assertEquals(SessionState.CLOSED, session.getState());
    }

    @Test
    @DisplayName("isActive transitions correctly through states")
    void testIsActive_StateTransitions() {
        // Initially OPEN
        assertTrue(session.isActive());
        assertEquals(SessionState.OPEN, session.getState());

        // Mark as PAID
        session.markPaid();
        assertTrue(session.isActive());
        assertEquals(SessionState.PAID, session.getState());

        // Close session
        session.close(LocalDateTime.now());
        assertFalse(session.isActive());
        assertEquals(SessionState.CLOSED, session.getState());
    }

    @Test
    @DisplayName("isActive for newly created session is true")
    void testIsActive_NewlyCreated() {
        ParkingSession newSession = new ParkingSession(
                "session-2",
                "user-2",
                "XYZ789",
                "zone-2",
                "spot-2",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        assertTrue(newSession.isActive());
    }

    @Test
    @DisplayName("isActive remains true after marking paid")
    void testIsActive_AfterMarkingPaid() {
        assertTrue(session.isActive());

        session.markPaid();

        assertTrue(session.isActive());
    }

    @Test
    @DisplayName("isActive becomes false after closing")
    void testIsActive_AfterClosing() {
        assertTrue(session.isActive());

        session.close(LocalDateTime.now().plusHours(2));

        assertFalse(session.isActive());
    }

    @Test
    @DisplayName("isActive with manual state change to CLOSED")
    void testIsActive_ManualStateChangeToClosed() {
        session.setState(SessionState.CLOSED);

        assertFalse(session.isActive());
    }

    @Test
    @DisplayName("isActive multiple checks do not change state")
    void testIsActive_MultipleChecksConsistent() {
        assertTrue(session.isActive());
        assertTrue(session.isActive());
        assertTrue(session.isActive());

        session.markPaid();

        assertTrue(session.isActive());
        assertTrue(session.isActive());

        session.close(LocalDateTime.now());

        assertFalse(session.isActive());
        assertFalse(session.isActive());
    }

    @Test
    @DisplayName("isActive for PAID state does not have endTime")
    void testIsActive_PaidStateNoEndTime() {
        session.markPaid();

        assertTrue(session.isActive());
        assertNull(session.getEndTime());
    }
}