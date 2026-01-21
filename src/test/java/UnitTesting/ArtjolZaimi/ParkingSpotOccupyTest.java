package UnitTesting.ArtjolZaimi;

import Model.ParkingSpot;
import Model.ParkingZone;
import Enum.SpotState;
import Enum.ZoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit Tests for M-18: ParkingSpot.occupy

class ParkingSpotOccupyTest {

    @Mock
    private ParkingZone mockZone;

    private ParkingSpot spot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        spot = new ParkingSpot("spot-1", mockZone);
    }

    @Test
    @DisplayName("occupy from FREE state succeeds")
    void testOccupy_FromFreeState() {
        assertEquals(SpotState.FREE, spot.getState());

        spot.occupy();

        assertEquals(SpotState.OCCUPIED, spot.getState());
    }

    @Test
    @DisplayName("occupy from RESERVED state succeeds")
    void testOccupy_FromReservedState() {
        spot.reserve();
        assertEquals(SpotState.RESERVED, spot.getState());

        spot.occupy();

        assertEquals(SpotState.OCCUPIED, spot.getState());
    }

    @Test
    @DisplayName("occupy throws exception when already OCCUPIED")
    void testOccupy_AlreadyOccupied() {
        spot.occupy();
        assertEquals(SpotState.OCCUPIED, spot.getState());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            spot.occupy();
        });

        assertTrue(exception.getMessage().contains("already OCCUPIED"));
    }

    @Test
    @DisplayName("occupy changes state correctly")
    void testOccupy_StateChange() {
        assertFalse(spot.isOccupied());
        assertTrue(spot.isFree());

        spot.occupy();

        assertTrue(spot.isOccupied());
        assertFalse(spot.isFree());
    }

    @Test
    @DisplayName("occupy multiple times throws exception")
    void testOccupy_MultipleTimes() {
        spot.occupy();

        assertThrows(IllegalStateException.class, () -> spot.occupy());
        assertThrows(IllegalStateException.class, () -> spot.occupy());
    }

    @Test
    @DisplayName("occupy after release succeeds")
    void testOccupy_AfterRelease() {
        spot.occupy();
        assertEquals(SpotState.OCCUPIED, spot.getState());

        spot.release();
        assertEquals(SpotState.FREE, spot.getState());

        spot.occupy();
        assertEquals(SpotState.OCCUPIED, spot.getState());
    }

    @Test
    @DisplayName("occupy preserves spot identity")
    void testOccupy_PreservesIdentity() {
        String originalId = spot.getSpotId();
        ParkingZone originalZone = spot.getParkingZone();

        spot.occupy();

        assertEquals(originalId, spot.getSpotId());
        assertEquals(originalZone, spot.getParkingZone());
    }


    @Test
    @DisplayName("occupy transition from RESERVED in VIP zone")
    void testOccupy_ReservedToOccupiedVIPZone() {
        when(mockZone.getZoneType()).thenReturn(ZoneType.VIP);
        ParkingSpot vipSpot = new ParkingSpot("vip-spot-2", mockZone);

        vipSpot.reserve();
        vipSpot.occupy();

        assertEquals(SpotState.OCCUPIED, vipSpot.getState());
    }
}