package UnitTesting.ShpetimShabanaj;

import Model.ParkingSession;
import Repository.ParkingSessionRepository;
import Repository.impl.InMemoryParkingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParkingSessionRepositorySessionCountForTodayTest {
    ParkingSessionRepository parkingSessionRepository;

    @BeforeEach
    void setup(){
        parkingSessionRepository = new InMemoryParkingSessionRepository();
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Verify sessions from today are counted")
    void testCountingSessionsForToday(){
        ParkingSession parkingSession1 = createParkingSession("S1","U1",LocalDateTime.now().plusMinutes(1));
        ParkingSession parkingSession2= createParkingSession("S2","U1",LocalDateTime.now().plusMinutes(2));

        parkingSessionRepository.save(parkingSession1);
        parkingSessionRepository.save(parkingSession2);

        assertEquals(2,parkingSessionRepository.getSessionsCountForToday("U1"));
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Verify sessions from other days are ignore")
    void testThatOtherDaysSessionsAreIngnored(){
        ParkingSession parkingSessionToday = createParkingSession("S1","U1",LocalDateTime.now());
        parkingSessionRepository.save(parkingSessionToday);
        ParkingSession parkingSessionYesterday= createParkingSession("S2","U1",LocalDateTime.now().minusDays(1));
        parkingSessionRepository.save(parkingSessionYesterday);

        assertEquals(1,parkingSessionRepository.getSessionsCountForToday("U1"));
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Verify filtering by User ID works")
    void testFilteringForUsers(){
        ParkingSession parkingSession1= createParkingSession("S1","U1",LocalDateTime.now());
        parkingSessionRepository.save(parkingSession1);
        ParkingSession parkingSession2= createParkingSession("S2","U2",LocalDateTime.now());
        parkingSessionRepository.save(parkingSession2);

        assertEquals(1,parkingSessionRepository.getSessionsCountForToday("U1"));
    }

    //TC-04
    @Test
    @DisplayName("TC-04: Verify behavior with no sessions today")
    void testForNoSessionsToday(){
        ParkingSession parkingSession1= createParkingSession("S1","U1",LocalDateTime.now().minusDays(1));
        parkingSessionRepository.save(parkingSession1);
        ParkingSession parkingSession2= createParkingSession("S2","U2",LocalDateTime.now().minusDays(2));
        parkingSessionRepository.save(parkingSession2);

        assertEquals(0,parkingSessionRepository.getSessionsCountForToday("U1"));
    }

    //TC-05
    @Test
    @DisplayName("TC-05: Verify for null user Id")
    void testForNullUserId(){
        assertDoesNotThrow(()->parkingSessionRepository.getSessionsCountForToday(null));
    }
    private ParkingSession createParkingSession(String id, String userId, LocalDateTime startTime) {
        ParkingSession parkingSession=mock(ParkingSession.class);
        when(parkingSession.getId()).thenReturn(id);
        when(parkingSession.getUserId()).thenReturn(userId);
        when(parkingSession.getStartTime()).thenReturn(startTime);

        return parkingSession;
    }
}
