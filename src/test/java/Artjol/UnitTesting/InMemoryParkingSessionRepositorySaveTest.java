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
import java.util.Optional;

// Unit Tests for M-48: InMemoryParkingSessionRepository.save

class InMemoryParkingSessionRepositorySaveTest {

    private InMemoryParkingSessionRepository repository;

    @Mock
    private ParkingSession mockSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new InMemoryParkingSessionRepository();
    }

    @Test
    @DisplayName("save stores session successfully")
    void testSave_StoresSession() {
        when(mockSession.getId()).thenReturn("session-1");

        repository.save(mockSession);

        Optional<ParkingSession> found = repository.findById("session-1");
        assertTrue(found.isPresent());
        assertEquals(mockSession, found.get());
    }

    @Test
    @DisplayName("save overwrites existing session with same id")
    void testSave_OverwritesExisting() {
        ParkingSession session1 = new ParkingSession(
                "session-1", "user-1", "ABC123", "zone-1", "spot-1",
                TimeOfDayBand.PEAK, DayType.WEEKDAY, ZoneType.STANDARD,
                LocalDateTime.now()
        );

        ParkingSession session2 = new ParkingSession(
                "session-1", "user-2", "XYZ789", "zone-2", "spot-2",
                TimeOfDayBand.OFF_PEAK, DayType.WEEKEND, ZoneType.VIP,
                LocalDateTime.now()
        );

        repository.save(session1);
        repository.save(session2);

        Optional<ParkingSession> found = repository.findById("session-1");
        assertTrue(found.isPresent());
        assertEquals("user-2", found.get().getUserId());
    }

    @Test
    @DisplayName("save multiple sessions")
    void testSave_MultipleSessions() {
        when(mockSession.getId()).thenReturn("session-1");
        ParkingSession session2 = mock(ParkingSession.class);
        when(session2.getId()).thenReturn("session-2");

        repository.save(mockSession);
        repository.save(session2);

        assertTrue(repository.findById("session-1").isPresent());
        assertTrue(repository.findById("session-2").isPresent());
    }


}