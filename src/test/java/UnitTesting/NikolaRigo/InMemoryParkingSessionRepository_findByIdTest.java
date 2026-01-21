package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import Model.ParkingSession;
import Repository.impl.InMemoryParkingSessionRepository;
import Enum.TimeOfDayBand;
import Enum.DayType;
import Enum.ZoneType;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryParkingSessionRepository_findByIdTest {

    private InMemoryParkingSessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingSessionRepository();
    }

    @Test
    void findById_WhenSessionExists_ShouldReturnOptionalWithSession() {
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
        Optional<ParkingSession> result = repository.findById("session-123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(session, result.get());
    }

    @Test
    void findById_WhenSessionDoesNotExist_ShouldReturnEmptyOptional() {
        // Act
        Optional<ParkingSession> result = repository.findById("non-existent-id");

        // Assert
        assertTrue(result.isEmpty());
        assertFalse(result.isPresent());
    }

    @Test
    void findById_WhenRepositoryIsEmpty_ShouldReturnEmptyOptional() {
        // Act
        Optional<ParkingSession> result = repository.findById("any-id");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_WithNullSessionId_ShouldReturnEmptyOptional() {
        // Act
        Optional<ParkingSession> result = repository.findById(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_AfterSavingMultipleSessions_ShouldReturnCorrectSession() {
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
                "XYZ-5678",
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
                "DEF-9012",
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
        Optional<ParkingSession> result1 = repository.findById("session-1");
        Optional<ParkingSession> result2 = repository.findById("session-2");
        Optional<ParkingSession> result3 = repository.findById("session-3");

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertEquals(session1, result1.get());
        assertEquals(session2, result2.get());
        assertEquals(session3, result3.get());
    }

    @Test
    void findById_ShouldReturnSameInstanceAsSaved() {
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
        Optional<ParkingSession> result = repository.findById("session-123");

        // Assert
        assertTrue(result.isPresent());
        assertSame(session, result.get());
    }

    @Test
    void findById_AfterDeletingSession_ShouldReturnEmptyOptional() {
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
        repository.delete(session);

        // Act
        Optional<ParkingSession> result = repository.findById("session-123");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_AfterUpdatingSession_ShouldReturnUpdatedSession() {
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

        // Modify session (e.g., mark as paid)
        session.markPaid();
        repository.save(session); // Save updated session

        // Act
        Optional<ParkingSession> result = repository.findById("session-123");

        // Assert
        assertTrue(result.isPresent());
        assertSame(session, result.get());
    }

    @Test
    void findById_WithEmptyStringId_ShouldReturnEmptyOptional() {
        // Act
        Optional<ParkingSession> result = repository.findById("");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_WithWhitespaceId_ShouldReturnEmptyOptional() {
        // Act
        Optional<ParkingSession> result = repository.findById("   ");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_CalledMultipleTimes_ShouldReturnSameSession() {
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
        Optional<ParkingSession> result1 = repository.findById("session-123");
        Optional<ParkingSession> result2 = repository.findById("session-123");
        Optional<ParkingSession> result3 = repository.findById("session-123");

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertSame(result1.get(), result2.get());
        assertSame(result1.get(), result3.get());
    }

    @Test
    void findById_WithSimilarButDifferentIds_ShouldReturnCorrectSessions() {
        // Arrange
        ParkingSession session1 = new ParkingSession(
                "session-1",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession session2 = new ParkingSession(
                "session-10",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession session3 = new ParkingSession(
                "session-100",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );

        repository.save(session1);
        repository.save(session2);
        repository.save(session3);

        // Act & Assert
        Optional<ParkingSession> result1 = repository.findById("session-1");
        assertTrue(result1.isPresent());
        assertEquals("session-1", result1.get().getId());

        Optional<ParkingSession> result2 = repository.findById("session-10");
        assertTrue(result2.isPresent());
        assertEquals("session-10", result2.get().getId());

        Optional<ParkingSession> result3 = repository.findById("session-100");
        assertTrue(result3.isPresent());
        assertEquals("session-100", result3.get().getId());
    }

    @Test
    void findById_WithCaseSensitiveIds_ShouldDistinguishBetweenCases() {
        // Arrange
        ParkingSession session1 = new ParkingSession(
                "session-ABC",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );
        ParkingSession session2 = new ParkingSession(
                "session-abc",
                "user-456",
                "ABC-1234",
                "zone-789",
                "spot-001",
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                ZoneType.STANDARD,
                LocalDateTime.now()
        );

        repository.save(session1);
        repository.save(session2);

        // Act
        Optional<ParkingSession> resultUppercase = repository.findById("session-ABC");
        Optional<ParkingSession> resultLowercase = repository.findById("session-abc");

        // Assert
        assertTrue(resultUppercase.isPresent());
        assertTrue(resultLowercase.isPresent());
        assertEquals("session-ABC", resultUppercase.get().getId());
        assertEquals("session-abc", resultLowercase.get().getId());
        assertNotSame(resultUppercase.get(), resultLowercase.get());
    }

    @Test
    void findById_WhenSessionIdContainsSpecialCharacters_ShouldFindSession() {
        // Arrange
        ParkingSession session = new ParkingSession(
                "session-123-abc-xyz_001",
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
        Optional<ParkingSession> result = repository.findById("session-123-abc-xyz_001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(session, result.get());
    }

    @Test
    void findById_AfterSavingAndDeletingDifferentSession_ShouldStillFindOriginal() {
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
                "XYZ-5678",
                "zone-2",
                "spot-002",
                TimeOfDayBand.OFF_PEAK,
                DayType.WEEKEND,
                ZoneType.VIP,
                LocalDateTime.now()
        );

        repository.save(session1);
        repository.save(session2);
        repository.delete(session2);

        // Act
        Optional<ParkingSession> result = repository.findById("session-1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(session1, result.get());
    }
}
