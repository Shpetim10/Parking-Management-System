package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import Model.ParkingSession;
import Repository.impl.InMemoryParkingSessionRepository;
import Enum.TimeOfDayBand;
import Enum.DayType;
import Enum.ZoneType;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryParkingSessionRepository_findActiveSessionsForVehicleTest {

    private InMemoryParkingSessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingSessionRepository();
    }

    @Test
    void findActiveSessionsForVehicle_WhenVehicleHasOneActiveSession_ShouldReturnListWithOneSession() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-123",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        repository.save(session);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertEquals(1, result.size());
        assertEquals(session, result.get(0));
    }

    @Test
    void findActiveSessionsForVehicle_WhenVehicleHasNoSessions_ShouldReturnEmptyList() {
        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void findActiveSessionsForVehicle_WhenRepositoryIsEmpty_ShouldReturnEmptyList() {
        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("any-plate");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveSessionsForVehicle_WhenVehicleHasMultipleActiveSessions_ShouldReturnAllActiveSessions() {
        // Arrange
        ParkingSession session1 = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession session2 = new ParkingSession(
                "session-2",
                "user-2",
                "ABC-1234",
                "zone-2",
                "spot-002",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );
        ParkingSession session3 = new ParkingSession(
                "session-3",
                "user-3",
                "ABC-1234",
                "zone-3",
                "spot-003",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );

        repository.save(session1);
        repository.save(session2);
        repository.save(session3);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains(session1));
        assertTrue(result.contains(session2));
        assertTrue(result.contains(session3));
    }

    @Test
    void findActiveSessionsForVehicle_WhenVehicleHasPaidSession_ShouldNotReturnPaidSession() {
        // Arrange
        ParkingSession activeSession = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );

        ParkingSession paidSession = new ParkingSession(
                "session-2",
                "user-2",
                "ABC-1234",
                "zone-2",
                "spot-002",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        repository.save(activeSession);
        repository.save(paidSession);
        paidSession.close(LocalDateTime.now());
        repository.save(paidSession);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertEquals(1, result.size());
        assertEquals(activeSession, result.get(0));
        assertFalse(result.contains(paidSession));
    }

    @Test
    void findActiveSessionsForVehicle_ShouldOnlyReturnSessionsForSpecifiedVehicle() {
        // Arrange
        ParkingSession sessionVehicle1 = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession sessionVehicle2 = new ParkingSession(
                "session-2",
                "user-2",
                "XYZ-5678",
                "zone-2",
                "spot-002",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );
        ParkingSession sessionVehicle3 = new ParkingSession(
                "session-3",
                "user-3",
                "DEF-9012",
                "zone-3",
                "spot-003",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );

        repository.save(sessionVehicle1);
        repository.save(sessionVehicle2);
        repository.save(sessionVehicle3);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertEquals(1, result.size());
        assertEquals(sessionVehicle1, result.get(0));
        assertFalse(result.contains(sessionVehicle2));
        assertFalse(result.contains(sessionVehicle3));
    }

    @Test
    void findActiveSessionsForVehicle_WhenAllSessionsForVehicleArePaid_ShouldReturnEmptyList() {
        // Arrange
        ParkingSession session1 = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession session2 = new ParkingSession(
                "session-2",
                "user-2",
                "ABC-1234",
                "zone-2",
                "spot-002",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        repository.save(session1);
        repository.save(session2);
        session1.close(LocalDateTime.now());
        session2.close(LocalDateTime.now());
        repository.save(session1);
        repository.save(session2);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveSessionsForVehicle_WithMixedActiveAndPaidSessions_ShouldReturnOnlyActive() {
        // Arrange
        ParkingSession activeSession1 = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession paidSession = new ParkingSession(
                "session-2",
                "user-2",
                "ABC-1234",
                "zone-2",
                "spot-002",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );
        ParkingSession activeSession2 = new ParkingSession(
                "session-3",
                "user-3",
                "ABC-1234",
                "zone-3",
                "spot-003",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );

        repository.save(activeSession1);
        repository.save(paidSession);
        repository.save(activeSession2);
        paidSession.close(LocalDateTime.now());
        repository.save(paidSession);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(activeSession1));
        assertTrue(result.contains(activeSession2));
        assertFalse(result.contains(paidSession));
    }

    @Test
    void findActiveSessionsForVehicle_WithNullPlate_ShouldReturnEmptyList() {
        // Arrange
        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.isActive()).thenReturn(true);
        when(mockSession.getVehiclePlate()).thenReturn("ABC-1234");

        repository.save(mockSession);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(mockSession).isActive();
        verify(mockSession).getVehiclePlate();
    }

    @Test
    void findActiveSessionsForVehicle_WithEmptyStringPlate_ShouldReturnEmptyList() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        repository.save(session);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveSessionsForVehicle_IsCaseSensitive_ShouldNotMatchDifferentCase() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        repository.save(session);

        // Act
        List<ParkingSession> resultUppercase = repository.findActiveSessionsForVehicle("ABC-1234");
        List<ParkingSession> resultLowercase = repository.findActiveSessionsForVehicle("abc-1234");

        // Assert
        assertEquals(1, resultUppercase.size());
        assertTrue(resultLowercase.isEmpty());
    }

    @Test
    void findActiveSessionsForVehicle_AfterDeletingSession_ShouldNotReturnDeletedSession() {
        // Arrange
        ParkingSession session1 = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession session2 = new ParkingSession(
                "session-2",
                "user-2",
                "ABC-1234",
                "zone-2",
                "spot-002",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        repository.save(session1);
        repository.save(session2);
        repository.delete(session1);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertEquals(1, result.size());
        assertEquals(session2, result.get(0));
        assertFalse(result.contains(session1));
    }

    @Test
    void findActiveSessionsForVehicle_CalledMultipleTimes_ShouldReturnConsistentResults() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        repository.save(session);

        // Act
        List<ParkingSession> result1 = repository.findActiveSessionsForVehicle("ABC-1234");
        List<ParkingSession> result2 = repository.findActiveSessionsForVehicle("ABC-1234");
        List<ParkingSession> result3 = repository.findActiveSessionsForVehicle("ABC-1234");

        // Assert
        assertEquals(result1.size(), result2.size());
        assertEquals(result1.size(), result3.size());
        assertEquals(result1.get(0), result2.get(0));
        assertEquals(result1.get(0), result3.get(0));
    }

    @Test
    void findActiveSessionsForVehicle_WithSameUserDifferentVehicles_ShouldSeparateByVehicle() {
        // Arrange
        ParkingSession sessionVehicle1 = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession sessionVehicle2 = new ParkingSession(
                "session-2",
                "user-1",
                "XYZ-5678",
                "zone-2",
                "spot-002",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        repository.save(sessionVehicle1);
        repository.save(sessionVehicle2);

        // Act
        List<ParkingSession> resultVehicle1 = repository.findActiveSessionsForVehicle("ABC-1234");
        List<ParkingSession> resultVehicle2 = repository.findActiveSessionsForVehicle("XYZ-5678");

        // Assert
        assertEquals(1, resultVehicle1.size());
        assertEquals(1, resultVehicle2.size());
        assertEquals(sessionVehicle1, resultVehicle1.get(0));
        assertEquals(sessionVehicle2, resultVehicle2.get(0));
    }

    @Test
    void findActiveSessionsForVehicle_WithSpecialCharactersInPlate_ShouldFindSession() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-123_XYZ",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        repository.save(session);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("ABC-123_XYZ");

        // Assert
        assertEquals(1, result.size());
        assertEquals(session, result.get(0));
    }

    @Test
    void findActiveSessionsForVehicle_WithWhitespacePlate_ShouldReturnEmptyList() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-1",
                "user-1",
                "ABC-1234",
                "zone-1",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        repository.save(session);

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle("   ");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveSessionsForVehicle_WithManyActiveSessions_ShouldReturnAll() {
        // Arrange
        String vehiclePlate = "ABC-1234";
        for (int i = 1; i <= 10; i++) {
            ParkingSession session = new ParkingSession(
                    "session-" + i,
                    "user-" + i,
                    vehiclePlate,
                    "zone-" + i,
                    "spot-" + String.format("%03d", i),
                    TimeOfDayBand.PEAK,
                    DayType.WEEKDAY,
                    ZoneType.STANDARD,
                    LocalDateTime.now()
            );
            repository.save(session);
        }

        // Act
        List<ParkingSession> result = repository.findActiveSessionsForVehicle(vehiclePlate);

        // Assert
        assertEquals(10, result.size());
    }
}
