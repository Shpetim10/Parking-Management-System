package UnitTesting.ShpetimShabanaj;

import Model.ParkingSpot;
import Model.ParkingZone;
import Enum.ZoneType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParkingZoneHasFreeSpotTest {
    private ParkingZone zone;

    @BeforeEach
    void setUp() {
        zone = new ParkingZone("Z1", ZoneType.STANDARD, 0.8);
    }

    //TC-01
    @Test
    @DisplayName("TC-01: Spots are empty")
    void testWhenThereAreNoSpots(){
        assertFalse(zone.hasFreeSpot());
    }

    //TC-02
    @Test
    @DisplayName("TC-02: There are no Free Spots")
    void testWhenThereAreNoFreeSpots(){
        ParkingSpot spot1=mock(ParkingSpot.class);
        ParkingSpot spot2=mock(ParkingSpot.class);
        //for add
        when(spot1.getParkingZone()).thenReturn(zone);
        when(spot2.getParkingZone()).thenReturn(zone);
        // for method test
        when(spot1.isFree()).thenReturn(false);
        when(spot2.isFree()).thenReturn(false);

        zone.addSpot(spot1);
        zone.addSpot(spot2);

        assertFalse(zone.hasFreeSpot());
    }

    //TC-03
    @Test
    @DisplayName("TC-03: There is one Free Spot")
    void testWhenThereAreFreeSpots(){
        ParkingSpot spot1=mock(ParkingSpot.class);
        ParkingSpot spot2=mock(ParkingSpot.class);
        //for add
        when(spot1.getParkingZone()).thenReturn(zone);
        when(spot2.getParkingZone()).thenReturn(zone);
        // for method test
        when(spot1.isFree()).thenReturn(false);
        when(spot2.isFree()).thenReturn(true);

        zone.addSpot(spot1);
        zone.addSpot(spot2);

        assertTrue(zone.hasFreeSpot());
    }
}
