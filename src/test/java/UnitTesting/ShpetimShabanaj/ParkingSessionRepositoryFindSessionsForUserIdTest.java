package UnitTesting.ShpetimShabanaj;

import Model.ParkingSession;
import Repository.impl.InMemoryParkingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParkingSessionRepositoryFindSessionsForUserIdTest {
    private InMemoryParkingSessionRepository repository;
    private final String TARGET_USER = "U1";

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingSessionRepository();
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should return only active sessions belonging to the specified user")
    void testFindActiveSessionsForCorrectUser() {
        ParkingSession user1Active1 = createMockSession("S1", TARGET_USER, true);
        ParkingSession user1Active2 = createMockSession("S2", TARGET_USER, true);
        ParkingSession user2Active = createMockSession("S3", "USER_2", true);

        repository.save(user1Active1);
        repository.save(user1Active2);
        repository.save(user2Active);

        List<ParkingSession> result = repository.findActiveSessionsForUser(TARGET_USER);

        assertAll("Verify user filtering",
                () -> assertEquals(2, result.size(), "Should find exactly 2 sessions for User 1"),
                () -> assertTrue(result.contains(user1Active1)),
                () -> assertTrue(result.contains(user1Active2)),
                () -> assertFalse(result.contains(user2Active), "Should not contain User 2's session")
        );
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should exclude inactive sessions for the user")
    void testExcludeInactiveSessions() {
        ParkingSession activeSession = createMockSession("S1", TARGET_USER, true);
        ParkingSession inactiveSession = createMockSession("S2", TARGET_USER, false);

        repository.save(activeSession);
        repository.save(inactiveSession);

        List<ParkingSession> result = repository.findActiveSessionsForUser(TARGET_USER);

        assertAll("Verify activity filtering",
                () -> assertEquals(1, result.size()),
                () -> assertTrue(result.contains(activeSession)),
                () -> assertFalse(result.contains(inactiveSession), "Inactive session should be filtered out")
        );
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should return empty list when no matching active sessions exist")
    void testNoMatchingSessions() {
        repository.save(createMockSession("S1", "U2", true));

        List<ParkingSession> result = repository.findActiveSessionsForUser(TARGET_USER);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be an empty list");
    }

    //TC-04
    @Test
    @DisplayName("TC-04: Verify behavior with empty repository")
    void testWithEmptyRepository(){
        List<ParkingSession> result = repository.findActiveSessionsForUser(TARGET_USER);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be an empty list");
    }

    @Test
    @DisplayName("TC-05: Should return empty list when the user id is with uppercase but searched with lowercase")
    void testNoMatchingSessionsForCaseSensitive() {
        repository.save(createMockSession("S1", TARGET_USER, true));

        List<ParkingSession> result = repository.findActiveSessionsForUser("u1");

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be an empty list");
    }

    //Helper for creating session mocks
    private ParkingSession createMockSession(String id, String userId, boolean isActive) {
        ParkingSession session = mock(ParkingSession.class);
        when(session.getId()).thenReturn(id);
        when(session.getUserId()).thenReturn(userId);
        when(session.isActive()).thenReturn(isActive);
        return session;
    }
}