package UnitTesting.NikolaRigo;

import Model.ParkingSession;
import Repository.impl.InMemoryParkingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryParkingSessionRepositoryTest {

    private InMemoryParkingSessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingSessionRepository();
    }

    @Test
    void getActiveSessionsCountForUser_WithNoSessions_ShouldReturnZero() {
        // Act
        int count = repository.getActiveSessionsCountForUser("user-1");

        // Assert
        assertEquals(0, count);
    }

    @Test
    void getActiveSessionsCountForUser_WithOneActiveSession_ShouldReturnOne() {
        // Arrange
        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getId()).thenReturn("session-1");
        when(mockSession.isActive()).thenReturn(true);
        when(mockSession.getUserId()).thenReturn("user-1");

        repository.save(mockSession);

        // Act
        int count = repository.getActiveSessionsCountForUser("user-1");

        // Assert
        assertEquals(1, count);
        verify(mockSession, atLeastOnce()).isActive();
        verify(mockSession, atLeastOnce()).getUserId();
    }

    @Test
    void getActiveSessionsCountForUser_WithMultipleActiveSessions_ShouldReturnCorrectCount() {
        // Arrange
        ParkingSession mockSession1 = mock(ParkingSession.class);
        when(mockSession1.getId()).thenReturn("session-1");
        when(mockSession1.isActive()).thenReturn(true);
        when(mockSession1.getUserId()).thenReturn("user-1");

        ParkingSession mockSession2 = mock(ParkingSession.class);
        when(mockSession2.getId()).thenReturn("session-2");
        when(mockSession2.isActive()).thenReturn(true);
        when(mockSession2.getUserId()).thenReturn("user-1");

        ParkingSession mockSession3 = mock(ParkingSession.class);
        when(mockSession3.getId()).thenReturn("session-3");
        when(mockSession3.isActive()).thenReturn(true);
        when(mockSession3.getUserId()).thenReturn("user-1");

        repository.save(mockSession1);
        repository.save(mockSession2);
        repository.save(mockSession3);

        // Act
        int count = repository.getActiveSessionsCountForUser("user-1");

        // Assert
        assertEquals(3, count);
    }

    @Test
    void getActiveSessionsCountForUser_WithInactiveSessions_ShouldNotCountThem() {
        // Arrange
        ParkingSession activeSession = mock(ParkingSession.class);
        when(activeSession.getId()).thenReturn("session-1");
        when(activeSession.isActive()).thenReturn(true);
        when(activeSession.getUserId()).thenReturn("user-1");

        ParkingSession inactiveSession = mock(ParkingSession.class);
        when(inactiveSession.getId()).thenReturn("session-2");
        when(inactiveSession.isActive()).thenReturn(false);
        when(inactiveSession.getUserId()).thenReturn("user-1");

        repository.save(activeSession);
        repository.save(inactiveSession);

        // Act
        int count = repository.getActiveSessionsCountForUser("user-1");

        // Assert
        assertEquals(1, count);
        verify(activeSession, atLeastOnce()).isActive();
        verify(inactiveSession, atLeastOnce()).isActive();
    }

    @Test
    void getActiveSessionsCountForUser_WithDifferentUsers_ShouldOnlyCountMatchingUser() {
        // Arrange
        ParkingSession user1Session = mock(ParkingSession.class);
        when(user1Session.getId()).thenReturn("session-1");
        when(user1Session.isActive()).thenReturn(true);
        when(user1Session.getUserId()).thenReturn("user-1");

        ParkingSession user2Session = mock(ParkingSession.class);
        when(user2Session.getId()).thenReturn("session-2");
        when(user2Session.isActive()).thenReturn(true);
        when(user2Session.getUserId()).thenReturn("user-2");

        repository.save(user1Session);
        repository.save(user2Session);

        // Act
        int count = repository.getActiveSessionsCountForUser("user-1");

        // Assert
        assertEquals(1, count);
    }

    @Test
    void getActiveSessionsCountForUser_WithMixedSessionStates_ShouldReturnCorrectCount() {
        // Arrange
        ParkingSession activeUser1Session1 = mock(ParkingSession.class);
        when(activeUser1Session1.getId()).thenReturn("session-1");
        when(activeUser1Session1.isActive()).thenReturn(true);
        when(activeUser1Session1.getUserId()).thenReturn("user-1");

        ParkingSession activeUser1Session2 = mock(ParkingSession.class);
        when(activeUser1Session2.getId()).thenReturn("session-2");
        when(activeUser1Session2.isActive()).thenReturn(true);
        when(activeUser1Session2.getUserId()).thenReturn("user-1");

        ParkingSession inactiveUser1Session = mock(ParkingSession.class);
        when(inactiveUser1Session.getId()).thenReturn("session-3");
        when(inactiveUser1Session.isActive()).thenReturn(false);
        when(inactiveUser1Session.getUserId()).thenReturn("user-1");

        ParkingSession activeUser2Session = mock(ParkingSession.class);
        when(activeUser2Session.getId()).thenReturn("session-4");
        when(activeUser2Session.isActive()).thenReturn(true);
        when(activeUser2Session.getUserId()).thenReturn("user-2");

        repository.save(activeUser1Session1);
        repository.save(activeUser1Session2);
        repository.save(inactiveUser1Session);
        repository.save(activeUser2Session);

        // Act
        int count = repository.getActiveSessionsCountForUser("user-1");

        // Assert
        assertEquals(2, count);
    }

    @Test
    void getActiveSessionsCountForUser_WithNullUserId_ShouldReturnZero() {
        // Arrange
        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getId()).thenReturn("session-1");
        when(mockSession.isActive()).thenReturn(true);
        when(mockSession.getUserId()).thenReturn("user-1");

        repository.save(mockSession);

        // Act
        int count = repository.getActiveSessionsCountForUser(null);

        // Assert
        assertEquals(0, count);
    }

    @Test
    void getActiveSessionsCountForUser_WithNonExistentUser_ShouldReturnZero() {
        // Arrange
        ParkingSession mockSession = mock(ParkingSession.class);
        when(mockSession.getId()).thenReturn("session-1");
        when(mockSession.isActive()).thenReturn(true);
        when(mockSession.getUserId()).thenReturn("user-1");

        repository.save(mockSession);

        // Act
        int count = repository.getActiveSessionsCountForUser("non-existent-user");

        // Assert
        assertEquals(0, count);
    }
}
