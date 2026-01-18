package UnitTesting.ShpetimShabanaj;

import Model.ParkingSession;
import Repository.ParkingSessionRepository;
import Repository.impl.InMemoryParkingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParkingSessionRepositoryDeleteTest {
    ParkingSessionRepository parkingSessionRepository;

    @BeforeEach
    void setUp(){
        parkingSessionRepository=new InMemoryParkingSessionRepository();
    }

    //TC-01
    @Test
    @DisplayName("TC-01: Verify successful deletion")
    void testSuccessfulDeletion(){
        String sessionId="S1";
        ParkingSession session1=mock(ParkingSession.class);
        when(session1.getId()).thenReturn(sessionId);
        parkingSessionRepository.save(session1);
        assertTrue(parkingSessionRepository.findById(sessionId).isPresent());

        parkingSessionRepository.delete(session1);
        assertFalse(parkingSessionRepository.findById(sessionId).isPresent());
    }

    //TC-02
    @Test
    @DisplayName("TC-02: Verify deletion of non-existent session")
    void testNonExistentSession(){
        String sessionId="S2";
        ParkingSession session1=mock(ParkingSession.class);
        when(session1.getId()).thenReturn(sessionId);
        assertDoesNotThrow(()->parkingSessionRepository.delete(session1));
    }

    //TC-03
    @Test
    @DisplayName("TC-03: Verify deleting one session doesn't affect others")
    void testDeletingOneSessionAndRemainingAnotherOne(){
        String sessionId1="S1";
        String sessionId2="S2";
        ParkingSession session1=mock(ParkingSession.class);
        when(session1.getId()).thenReturn(sessionId1);
        ParkingSession session2=mock(ParkingSession.class);
        when(session2.getId()).thenReturn(sessionId2);
        parkingSessionRepository.save(session1);
        parkingSessionRepository.save(session2);

        assertAll(
                ()->assertDoesNotThrow(()->parkingSessionRepository.delete(session1)),
                ()->assertFalse(parkingSessionRepository.findById(sessionId1).isPresent()),
                ()->assertTrue(parkingSessionRepository.findById(sessionId2).isPresent())
        );
    }

    //TC-04
    @Test
    @DisplayName("TC-04: Preventing deleting null objects")
    void testPreventingNull(){
        assertThrows(NullPointerException.class,()->parkingSessionRepository.delete(null));
    }

}
