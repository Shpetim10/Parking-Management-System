package Artjol.UnitTesting;

import Model.ParkingSession;
import Enum.*;
import Repository.impl.InMemoryParkingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

// Unit Tests for M-51: InMemoryParkingSessionRepository.getActiveSessionsCountForVehicle

class InMemoryParkingSessionRepositoryGetActiveSessionsCountForVehicleTest {

    private InMemoryParkingSessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingSessionRepository();
    }

    @Test
    @DisplayName("returns 0 for vehicle with no sessions")
    void testGetActiveCount_NoSessions() {
        int count = repository.getActiveSessionsCountForVehicle("ABC123");
        assertEquals(0, count);
    }

    @Test
    @DisplayName("returns 1 for vehicle with single active session")
    void testGetActiveCount_SingleActiveSession() {
        ParkingSession session = createSession("s1", "user-1", "ABC123");
        repository.save(session);

        assertEquals(1, repository.getActiveSessionsCountForVehicle("ABC123"));
    }

    @Test
    @DisplayName("returns correct count for multiple active sessions")
    void testGetActiveCount_MultipleActiveSessions() {
        repository.save(createSession("s1", "user-1", "ABC123"));
        repository.save(createSession("s2", "user-2", "ABC123"));
        repository.save(createSession("s3", "user-3", "ABC123"));

        assertEquals(3, repository.getActiveSessionsCountForVehicle("ABC123"));
    }

    @Test
    @DisplayName("excludes closed sessions from count")
    void testGetActiveCount_ExcludesClosedSessions() {
        ParkingSession active = createSession("s1", "user-1", "ABC123");
        ParkingSession closed = createSession("s2", "user-2", "ABC123");
        closed.close(LocalDateTime.now());

        repository.save(active);
        repository.save(closed);

        assertEquals(1, repository.getActiveSessionsCountForVehicle("ABC123"));
    }

    @Test
    @DisplayName("counts only sessions for specified vehicle")
    void testGetActiveCount_OnlySpecifiedVehicle() {
        repository.save(createSession("s1", "user-1", "ABC123"));
        repository.save(createSession("s2", "user-2", "XYZ789"));
        repository.save(createSession("s3", "user-3", "ABC123"));

        assertEquals(2, repository.getActiveSessionsCountForVehicle("ABC123"));
        assertEquals(1, repository.getActiveSessionsCountForVehicle("XYZ789"));
    }

    @Test
    @DisplayName("includes PAID sessions in count")
    void testGetActiveCount_IncludesPaidSessions() {
        ParkingSession open = createSession("s1", "user-1", "ABC123");
        ParkingSession paid = createSession("s2", "user-2", "ABC123");
        paid.markPaid();

        repository.save(open);
        repository.save(paid);

        assertEquals(2, repository.getActiveSessionsCountForVehicle("ABC123"));
    }


    private ParkingSession createSession(String id, String userId, String plate) {
        return new ParkingSession(
                id, userId, plate, "zone-1", "spot-1",
                TimeOfDayBand.PEAK, DayType.WEEKDAY, ZoneType.STANDARD,
                LocalDateTime.now()
        );
    }
}