package UnitTesting.ArtjolZaimi;

import Model.ParkingZone;
import Model.ParkingSpot;
import Enum.ZoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit Tests for M-21: ParkingZone.addSpot

class ParkingZoneAddSpotTest {

    private ParkingZone zone;

    @Mock
    private ParkingSpot mockSpot;

    @Mock
    private ParkingZone mockSpotZone;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
    }

    @Test
    @DisplayName("addSpot with valid spot succeeds")
    void testAddSpot_ValidSpot() {
        when(mockSpot.getParkingZone()).thenReturn(mockSpotZone);
        when(mockSpotZone.getZoneType()).thenReturn(ZoneType.STANDARD);

        assertEquals(0, zone.getTotalSpots());

        zone.addSpot(mockSpot);

        assertEquals(1, zone.getTotalSpots());
        verify(mockSpot, atLeastOnce()).getParkingZone();
    }

    @Test
    @DisplayName("addSpot throws exception for null spot")
    void testAddSpot_NullSpot() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            zone.addSpot(null);
        });

        assertTrue(exception.getMessage().contains("Parking spot cannot be null"));
    }

    @Test
    @DisplayName("addSpot throws exception for mismatched zone type")
    void testAddSpot_MismatchedZoneType() {
        when(mockSpot.getParkingZone()).thenReturn(mockSpotZone);
        when(mockSpotZone.getZoneType()).thenReturn(ZoneType.VIP);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            zone.addSpot(mockSpot);
        });

        assertTrue(exception.getMessage().contains("Spot zone type does not match parking zone type"));
    }

    @Test
    @DisplayName("addSpot multiple spots increases total count")
    void testAddSpot_MultipleSpots() {
        ParkingSpot spot1 = mock(ParkingSpot.class);
        ParkingSpot spot2 = mock(ParkingSpot.class);
        ParkingSpot spot3 = mock(ParkingSpot.class);

        when(spot1.getParkingZone()).thenReturn(mockSpotZone);
        when(spot2.getParkingZone()).thenReturn(mockSpotZone);
        when(spot3.getParkingZone()).thenReturn(mockSpotZone);
        when(mockSpotZone.getZoneType()).thenReturn(ZoneType.STANDARD);

        zone.addSpot(spot1);
        zone.addSpot(spot2);
        zone.addSpot(spot3);

        assertEquals(3, zone.getTotalSpots());
    }


    @Test
    @DisplayName("addSpot updates spots list")
    void testAddSpot_UpdatesSpotsList() {
        when(mockSpot.getParkingZone()).thenReturn(mockSpotZone);
        when(mockSpotZone.getZoneType()).thenReturn(ZoneType.STANDARD);

        assertTrue(zone.getSpots().isEmpty());

        zone.addSpot(mockSpot);

        assertFalse(zone.getSpots().isEmpty());
        assertEquals(1, zone.getSpots().size());
        assertTrue(zone.getSpots().contains(mockSpot));
    }

    @Test
    @DisplayName("addSpot with real spot object")
    void testAddSpot_WithRealSpot() {
        ParkingZone realZone = new ParkingZone("zone-2", ZoneType.STANDARD, 0.8);
        ParkingSpot realSpot = new ParkingSpot("spot-1", realZone);

        realZone.addSpot(realSpot);

        assertEquals(1, realZone.getTotalSpots());
        assertTrue(realZone.getSpots().contains(realSpot));
    }



}