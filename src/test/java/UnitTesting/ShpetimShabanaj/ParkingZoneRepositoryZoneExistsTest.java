package UnitTesting.ShpetimShabanaj;

import Model.ParkingZone;
import Repository.ParkingZoneRepository;
import Repository.impl.InMemoryParkingZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParkingZoneRepositoryZoneExistsTest {
    ParkingZoneRepository parkingZoneRepository = new InMemoryParkingZoneRepository();

    @BeforeEach
    void setup(){
        parkingZoneRepository = new InMemoryParkingZoneRepository();
    }

    //TC-01
    @Test
    @DisplayName("TC-01: Verify returns true for an existing zone")
    void testForAnExistingZone(){
        ParkingZone parkingZone=mock(ParkingZone.class);
        when(parkingZone.getZoneId()).thenReturn("Z1");
        parkingZoneRepository.save(parkingZone);

        assertTrue(parkingZoneRepository.zoneExists("Z1"));
    }

    //TC-02
    @Test
    @DisplayName("TC-02: Verify returns false for a non-existent ID")
    void testForAnNonExistingZone(){
        assertFalse(parkingZoneRepository.zoneExists("Z1"));
    }

    //TC-03
    @Test
    @DisplayName("TC-03: Verify returns false for null input")
    void testForNullInput(){
        assertFalse(parkingZoneRepository.zoneExists(null));
    }
}
