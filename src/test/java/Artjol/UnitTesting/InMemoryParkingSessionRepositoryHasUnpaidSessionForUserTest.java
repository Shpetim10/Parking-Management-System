package Artjol.UnitTesting;

import Model.ParkingSession;
import Enum.*;
import Repository.impl.InMemoryParkingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

// Unit Tests for M-54: InMemoryParkingSessionRepository.hasUnpaidSessionForUser

class InMemoryParkingSessionRepositoryHasUnpaidSessionForUserTest {

    private InMemoryParkingSessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingSessionRepository();
    }

    @Test
    @DisplayName("returns false when user has no sessions")
    void testHasUnpaid_NoSessions() {
        assertFalse(repository.hasUnpaidSessionsForUser("user-1"));
    }

    @Test
    @DisplayName("returns true when user has OPEN session")
    void testHasUnpaid_OpenSession() {
        ParkingSession session = createSession("s1", "user-1");
        repository.save(session);

        assertTrue(repository.hasUnpaidSessionsForUser("user-1"));
    }

    @Test
    @DisplayName("returns false when user has only PAID sessions")
    void testHasUnpaid_OnlyPaidSessions() {
        ParkingSession session = createSession("s1", "user-1");
        session.markPaid();
        repository.save(session);

        assertFalse(repository.hasUnpaidSessionsForUser("user-1"));
    }

    @Test
    @DisplayName("returns false when user has only CLOSED sessions")
    void testHasUnpaid_OnlyClosedSessions() {
        ParkingSession session = createSession("s1", "user-1");
        session.close(LocalDateTime.now());
        repository.save(session);

        assertFalse(repository.hasUnpaidSessionsForUser("user-1"));
    }

    @Test
    @DisplayName("returns true when user has at least one OPEN session")
    void testHasUnpaid_MixedSessions() {
        ParkingSession open = createSession("s1", "user-1");
        ParkingSession paid = createSession("s2", "user-1");
        paid.markPaid();

        repository.save(open);
        repository.save(paid);

        assertTrue(repository.hasUnpaidSessionsForUser("user-1"));
    }


    private ParkingSession createSession(String id, String userId) {
        return new ParkingSession(
                id, userId, "ABC123", "zone-1", "spot-1",
                TimeOfDayBand.PEAK, DayType.WEEKDAY, ZoneType.STANDARD,
                LocalDateTime.now()
        );
    }
}