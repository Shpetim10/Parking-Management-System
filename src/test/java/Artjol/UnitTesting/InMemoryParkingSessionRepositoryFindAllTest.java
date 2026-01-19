package Artjol.UnitTesting;

import Model.ParkingSession;
import Enum.*;
import Repository.impl.InMemoryParkingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Collection;

// Unit Tests for M-45: InMemoryParkingSessionRepository.findAll

class InMemoryParkingSessionRepositoryFindAllTest {

    private InMemoryParkingSessionRepository repository;

    @Mock
    private ParkingSession mockSession1;

    @Mock
    private ParkingSession mockSession2;

    @Mock
    private ParkingSession mockSession3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new InMemoryParkingSessionRepository();
    }

    @Test
    @DisplayName("findAll returns empty collection for new repository")
    void testFindAll_EmptyRepository() {
        Collection<ParkingSession> sessions = repository.findAll();

        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
        assertEquals(0, sessions.size());
    }

    @Test
    @DisplayName("findAll returns all saved sessions")
    void testFindAll_AllSavedSessions() {
        when(mockSession1.getId()).thenReturn("session-1");
        when(mockSession2.getId()).thenReturn("session-2");
        when(mockSession3.getId()).thenReturn("session-3");

        repository.save(mockSession1);
        repository.save(mockSession2);
        repository.save(mockSession3);

        Collection<ParkingSession> sessions = repository.findAll();

        assertEquals(3, sessions.size());
        assertTrue(sessions.contains(mockSession1));
        assertTrue(sessions.contains(mockSession2));
        assertTrue(sessions.contains(mockSession3));
    }

    @Test
    @DisplayName("findAll with single session")
    void testFindAll_SingleSession() {
        ParkingSession session = new ParkingSession(
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

        repository.save(session);

        Collection<ParkingSession> sessions = repository.findAll();

        assertEquals(1, sessions.size());
        assertTrue(sessions.contains(session));
    }


    @Test
    @DisplayName("findAll after delete returns remaining sessions")
    void testFindAll_AfterDelete() {
        when(mockSession1.getId()).thenReturn("session-1");
        when(mockSession2.getId()).thenReturn("session-2");

        repository.save(mockSession1);
        repository.save(mockSession2);

        repository.delete(mockSession1);

        Collection<ParkingSession> sessions = repository.findAll();

        assertEquals(1, sessions.size());
        assertFalse(sessions.contains(mockSession1));
        assertTrue(sessions.contains(mockSession2));
    }

    @Test
    @DisplayName("findAll does not include duplicate sessions")
    void testFindAll_NoDuplicates() {
        when(mockSession1.getId()).thenReturn("session-1");

        repository.save(mockSession1);
        repository.save(mockSession1); // Save again with same ID

        Collection<ParkingSession> sessions = repository.findAll();

        assertEquals(1, sessions.size());
    }


    @Test
    @DisplayName("findAll does not modify internal state")
    void testFindAll_DoesNotModifyState() {
        when(mockSession1.getId()).thenReturn("session-1");
        repository.save(mockSession1);

        Collection<ParkingSession> sessions1 = repository.findAll();
        Collection<ParkingSession> sessions2 = repository.findAll();

        assertEquals(sessions1.size(), sessions2.size());
    }

    @Test
    @DisplayName("findAll returns collection with all zone types")
    void testFindAll_AllZoneTypes() {
        ParkingSession regularSession = createSession("s1", ZoneType.STANDARD);
        ParkingSession vipSession = createSession("s2", ZoneType.VIP);
        ParkingSession evSession = createSession("s3", ZoneType.EV);

        repository.save(regularSession);
        repository.save(vipSession);
        repository.save(evSession);

        Collection<ParkingSession> sessions = repository.findAll();

        assertEquals(3, sessions.size());
        assertTrue(sessions.stream().anyMatch(s -> s.getZoneType() == ZoneType.STANDARD));
        assertTrue(sessions.stream().anyMatch(s -> s.getZoneType() == ZoneType.VIP));
        assertTrue(sessions.stream().anyMatch(s -> s.getZoneType() == ZoneType.EV));
    }


    @Test
    @DisplayName("findAll returns non-null collection")
    void testFindAll_NonNull() {
        Collection<ParkingSession> sessions = repository.findAll();

        assertNotNull(sessions);
    }

    private ParkingSession createSession(String id, ZoneType zoneType) {
        return new ParkingSession(
                id,
                "user-" + id,
                "PLATE-" + id,
                "zone-" + id,
                "spot-" + id,
                TimeOfDayBand.PEAK,
                DayType.WEEKDAY,
                zoneType,
                LocalDateTime.now()
        );
    }
}