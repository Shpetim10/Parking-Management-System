package UnitTesting.NikolaRigo;

import Model.ParkingSession;
import Repository.impl.InMemoryParkingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryParkingSessionRepositoryTest_getHoursUsedTodayForUser {

    private InMemoryParkingSessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingSessionRepository();
    }

    @Test
    void withNoSessions_ShouldReturnZero() {
        // Act
        int hours = repository.getHoursUsedTodayForUser("user-1");

        // Assert
        assertEquals(0, hours);
    }

    @Test
    void withCompletedSession_ShouldCalculateCorrectHours() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();

        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getId()).thenReturn("session-1");
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(start);
        when(mockSession.getEndTime()).thenReturn(end);

        repository.save(mockSession);

        // Act
        int hours = repository.getHoursUsedTodayForUser("user-1");

        // Assert
        assertEquals(3, hours);
        verify(mockSession, atLeastOnce()).getUserId();
        verify(mockSession, atLeastOnce()).getStartTime();
        verify(mockSession, atLeastOnce()).getEndTime();
    }

    @Test
    void withActiveSession_ShouldUseCurrentTimeAsEnd() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusHours(2);

        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getId()).thenReturn("session-1");
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(start);
        when(mockSession.getEndTime()).thenReturn(null); // Active session

        repository.save(mockSession);

        // Act
        int hours = repository.getHoursUsedTodayForUser("user-1");

        // Assert
        assertEquals(2, hours);
    }

    @Test
    void withMultipleSessions_ShouldSumAllHours() {
        // Arrange
        LocalDateTime start1 = LocalDateTime.now().minusHours(5);
        LocalDateTime end1 = LocalDateTime.now().minusHours(3);

        LocalDateTime start2 = LocalDateTime.now().minusHours(2);
        LocalDateTime end2 = LocalDateTime.now();

        ParkingSession mockSession1 = mock(ParkingSession.class);
        when(mockSession1.getId()).thenReturn("session-1");
        when(mockSession1.getUserId()).thenReturn("user-1");
        when(mockSession1.getStartTime()).thenReturn(start1);
        when(mockSession1.getEndTime()).thenReturn(end1);

        ParkingSession mockSession2 = mock(ParkingSession.class);
        when(mockSession2.getId()).thenReturn("session-2");
        when(mockSession2.getUserId()).thenReturn("user-1");
        when(mockSession2.getStartTime()).thenReturn(start2);
        when(mockSession2.getEndTime()).thenReturn(end2);

        repository.save(mockSession1);
        repository.save(mockSession2);

        // Act
        int hours = repository.getHoursUsedTodayForUser("user-1");

        // Assert
        assertEquals(4, hours); // 2 hours + 2 hours
    }

    @Test
    void withPartialHour_ShouldRoundUp() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusMinutes(90); // 1.5 hours
        LocalDateTime end = LocalDateTime.now();

        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getId()).thenReturn("session-1");
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(start);
        when(mockSession.getEndTime()).thenReturn(end);

        repository.save(mockSession);

        // Act
        int hours = repository.getHoursUsedTodayForUser("user-1");

        // Assert
        assertEquals(2, hours); // Math.ceil(1.5) = 2
    }

    @Test
    void withDifferentUsers_ShouldOnlyCountMatchingUser() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();

        ParkingSession user1Session = mock(ParkingSession.class);
        when(user1Session.getId()).thenReturn("session-1");
        when(user1Session.getUserId()).thenReturn("user-1");
        when(user1Session.getStartTime()).thenReturn(start);
        when(user1Session.getEndTime()).thenReturn(end);

        ParkingSession user2Session = mock(ParkingSession.class);
        when(user2Session.getId()).thenReturn("session-2");
        when(user2Session.getUserId()).thenReturn("user-2");
        when(user2Session.getStartTime()).thenReturn(start);
        when(user2Session.getEndTime()).thenReturn(end);

        repository.save(user1Session);
        repository.save(user2Session);

        // Act
        int hours = repository.getHoursUsedTodayForUser("user-1");

        // Assert
        assertEquals(3, hours);
    }

    @Test
    void withMixedActiveAndCompleted_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime completedStart = LocalDateTime.now().minusHours(4);
        LocalDateTime completedEnd = LocalDateTime.now().minusHours(2);

        LocalDateTime activeStart = LocalDateTime.now().minusHours(1);

        ParkingSession completedSession = mock(ParkingSession.class);
        when(completedSession.getId()).thenReturn("session-1");
        when(completedSession.getUserId()).thenReturn("user-1");
        when(completedSession.getStartTime()).thenReturn(completedStart);
        when(completedSession.getEndTime()).thenReturn(completedEnd);

        ParkingSession activeSession = mock(ParkingSession.class);
        when(activeSession.getId()).thenReturn("session-2");
        when(activeSession.getUserId()).thenReturn("user-1");
        when(activeSession.getStartTime()).thenReturn(activeStart);
        when(activeSession.getEndTime()).thenReturn(null);

        repository.save(completedSession);
        repository.save(activeSession);

        // Act
        int hours = repository.getHoursUsedTodayForUser("user-1");

        // Assert
        assertEquals(3, hours); // 2 hours (completed) + 1 hour (active)
    }

    @Test
    void withNullUserId_ShouldReturnZero() {
        // Arrange
        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getId()).thenReturn("session-1");
        when(mockSession.getUserId()).thenReturn("user-1");
        when(mockSession.getStartTime()).thenReturn(LocalDateTime.now().minusHours(2));
        when(mockSession.getEndTime()).thenReturn(LocalDateTime.now());

        repository.save(mockSession);

        // Act
        int hours = repository.getHoursUsedTodayForUser(null);

        // Assert
        assertEquals(0, hours);
    }
}
