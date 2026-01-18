package UnitTesting.ShpetimShabanaj;

import Model.ParkingZone;
import Repository.ParkingZoneRepository;
import Repository.impl.InMemoryParkingZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParkingZoneRepositorySaveTest {
    ParkingZoneRepository parkingZoneRepository;

    @BeforeEach
    void setup(){
        parkingZoneRepository = new InMemoryParkingZoneRepository();
    }

    //TC-1
    @Test
    @DisplayName("TC-01: Verify successful storage of a zone")
    void testSuccessfulSave(){
        ParkingZone parkingZone=mock(ParkingZone.class);
        when(parkingZone.getZoneId()).thenReturn("Z1");

        assertAll(
                ()->assertDoesNotThrow(()->parkingZoneRepository.save(parkingZone)),
                ()->assertEquals(parkingZone, parkingZoneRepository.findZoneById("Z1"))
        );
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Prevent saving a null zone object")
    void testPreventSaveNull(){
        assertThrows(NullPointerException.class, ()->parkingZoneRepository.save(null));
    }

    //TC-03
    @Test
    @DisplayName("TC-03: Verify overwriting an existing zone")
    void testOverwritingExistingZone(){
        ParkingZone parkingZone1=mock(ParkingZone.class);
        when(parkingZone1.getZoneId()).thenReturn("Z1");
        parkingZoneRepository.save(parkingZone1);
        ParkingZone parkingZone2=mock(ParkingZone.class);
        when(parkingZone2.getZoneId()).thenReturn("Z1");
        parkingZoneRepository.save(parkingZone2);

        assertEquals(parkingZone2, parkingZoneRepository.findZoneById("Z1"));
    }
}
