package Artjol.UnitTesting;
import Model.ParkingZone;
import Model.ParkingSpot;
import Enum.ZoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

// Unit Tests for M-24: ParkingZone.getFreeSpotsCount

class ParkingZoneGetFreeSpotsCountTest {

    private ParkingZone zone;

    @BeforeEach
    void setUp() {
        zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
    }

    @Test
    @DisplayName("getFreeSpotsCount returns 0 for empty zone")
    void testGetFreeSpotsCount_EmptyZone() {
        assertEquals(0, zone.getFreeSpotsCount());
    }

    @Test
    @DisplayName("getFreeSpotsCount returns correct count with all free spots")
    void testGetFreeSpotsCount_AllFree() {
        ParkingSpot spot1 = new ParkingSpot("spot-1", zone);
        ParkingSpot spot2 = new ParkingSpot("spot-2", zone);
        ParkingSpot spot3 = new ParkingSpot("spot-3", zone);

        zone.addSpot(spot1);
        zone.addSpot(spot2);
        zone.addSpot(spot3);

        assertEquals(3, zone.getFreeSpotsCount());
    }

    @Test
    @DisplayName("getFreeSpotsCount returns 0 when all spots occupied")
    void testGetFreeSpotsCount_AllOccupied() {
        ParkingSpot spot1 = new ParkingSpot("spot-1", zone);
        ParkingSpot spot2 = new ParkingSpot("spot-2", zone);

        zone.addSpot(spot1);
        zone.addSpot(spot2);

        spot1.occupy();
        spot2.occupy();

        assertEquals(0, zone.getFreeSpotsCount());
    }

    @Test
    @DisplayName("getFreeSpotsCount with mixed spot states")
    void testGetFreeSpotsCount_MixedStates() {
        ParkingSpot spot1 = new ParkingSpot("spot-1", zone);
        ParkingSpot spot2 = new ParkingSpot("spot-2", zone);
        ParkingSpot spot3 = new ParkingSpot("spot-3", zone);
        ParkingSpot spot4 = new ParkingSpot("spot-4", zone);

        zone.addSpot(spot1);
        zone.addSpot(spot2);
        zone.addSpot(spot3);
        zone.addSpot(spot4);

        spot1.occupy();
        spot2.reserve();
        // spot3 and spot4 remain FREE

        assertEquals(2, zone.getFreeSpotsCount());
    }


    @Test
    @DisplayName("getFreeSpotsCount updates after occupying spot")
    void testGetFreeSpotsCount_AfterOccupying() {
        ParkingSpot spot1 = new ParkingSpot("spot-1", zone);
        ParkingSpot spot2 = new ParkingSpot("spot-2", zone);

        zone.addSpot(spot1);
        zone.addSpot(spot2);

        assertEquals(2, zone.getFreeSpotsCount());

        spot1.occupy();

        assertEquals(1, zone.getFreeSpotsCount());

        spot2.occupy();

        assertEquals(0, zone.getFreeSpotsCount());
    }

    @Test
    @DisplayName("getFreeSpotsCount updates after releasing spot")
    void testGetFreeSpotsCount_AfterReleasing() {
        ParkingSpot spot1 = new ParkingSpot("spot-1", zone);
        ParkingSpot spot2 = new ParkingSpot("spot-2", zone);

        zone.addSpot(spot1);
        zone.addSpot(spot2);

        spot1.occupy();
        spot2.occupy();

        assertEquals(0, zone.getFreeSpotsCount());

        spot1.release();

        assertEquals(1, zone.getFreeSpotsCount());

        spot2.release();

        assertEquals(2, zone.getFreeSpotsCount());
    }



    @Test
    @DisplayName("getFreeSpotsCount multiple calls return consistent results")
    void testGetFreeSpotsCount_ConsistentResults() {
        ParkingSpot spot1 = new ParkingSpot("spot-1", zone);
        ParkingSpot spot2 = new ParkingSpot("spot-2", zone);

        zone.addSpot(spot1);
        zone.addSpot(spot2);

        assertEquals(2, zone.getFreeSpotsCount());
        assertEquals(2, zone.getFreeSpotsCount());
        assertEquals(2, zone.getFreeSpotsCount());
    }
}