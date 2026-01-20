package UnitTesting.NikolaRigo;

import Model.ParkingSpot;
import Model.ParkingZone;
import org.junit.jupiter.api.Test;
import Enum.ZoneType;
import static org.junit.jupiter.api.Assertions.*;

class ParkingZone_getFirstFreeSpotTest {

    @Test
    void getFirstFreeSpot_WhenZoneHasNoSpots_ShouldReturnNull() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNull(result);
    }

    @Test
    void getFirstFreeSpot_WhenZoneHasOneFreeSpot_ShouldReturnThatSpot() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("SPOT-001", zone);
        zone.addSpot(spot);

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNotNull(result);
        assertEquals(spot, result);
        assertEquals("SPOT-001", result.getSpotId());
    }

    @Test
    void getFirstFreeSpot_WhenAllSpotsAreReserved_ShouldReturnNull() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone);
        zone.addSpot(spot1);
        zone.addSpot(spot2);

        spot1.reserve();
        spot2.reserve();

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNull(result);
    }

    @Test
    void getFirstFreeSpot_WhenAllSpotsAreOccupied_ShouldReturnNull() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone);
        zone.addSpot(spot1);
        zone.addSpot(spot2);

        spot1.occupy(); // Assuming this method exists
        spot2.occupy();

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNull(result);
    }

    @Test
    void getFirstFreeSpot_WhenMultipleFreeSpots_ShouldReturnFirstOne() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone);
        ParkingSpot spot3 = new ParkingSpot("SPOT-003", zone);
        zone.addSpot(spot1);
        zone.addSpot(spot2);
        zone.addSpot(spot3);

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNotNull(result);
        assertEquals(spot1, result);
        assertEquals("SPOT-001", result.getSpotId());
    }

    @Test
    void getFirstFreeSpot_WhenOnlyMiddleSpotIsFree_ShouldReturnThatSpot() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone);
        ParkingSpot spot3 = new ParkingSpot("SPOT-003", zone);
        zone.addSpot(spot1);
        zone.addSpot(spot2);
        zone.addSpot(spot3);

        spot1.reserve();
        spot3.occupy(); // Assuming this method exists

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNotNull(result);
        assertEquals(spot2, result);
        assertEquals("SPOT-002", result.getSpotId());
    }

    @Test
    void getFirstFreeSpot_WhenOnlyLastSpotIsFree_ShouldReturnThatSpot() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone);
        ParkingSpot spot3 = new ParkingSpot("SPOT-003", zone);
        zone.addSpot(spot1);
        zone.addSpot(spot2);
        zone.addSpot(spot3);

        spot1.reserve();
        spot2.reserve();

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNotNull(result);
        assertEquals(spot3, result);
        assertEquals("SPOT-003", result.getSpotId());
    }

    @Test
    void getFirstFreeSpot_CalledMultipleTimes_ShouldReturnSameSpot() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("SPOT-001", zone);
        zone.addSpot(spot);

        // Act
        ParkingSpot result1 = zone.getFirstFreeSpot();
        ParkingSpot result2 = zone.getFirstFreeSpot();
        ParkingSpot result3 = zone.getFirstFreeSpot();

        // Assert
        assertNotNull(result1);
        assertSame(result1, result2);
        assertSame(result1, result3);
    }

    @Test
    void getFirstFreeSpot_AfterReservingSpot_ShouldReturnNextFreeSpot() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone);
        zone.addSpot(spot1);
        zone.addSpot(spot2);

        // Act
        ParkingSpot firstFree = zone.getFirstFreeSpot();
        firstFree.reserve();
        ParkingSpot secondFree = zone.getFirstFreeSpot();

        // Assert
        assertEquals(spot1, firstFree);
        assertEquals(spot2, secondFree);
    }

    @Test
    void getFirstFreeSpot_WithMixedStates_ShouldReturnFirstFreeSpot() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone);
        ParkingSpot spot3 = new ParkingSpot("SPOT-003", zone);
        ParkingSpot spot4 = new ParkingSpot("SPOT-004", zone);
        zone.addSpot(spot1);
        zone.addSpot(spot2);
        zone.addSpot(spot3);
        zone.addSpot(spot4);

        spot1.occupy(); // Assuming this method exists
        spot3.reserve();

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNotNull(result);
        assertEquals(spot2, result);
    }

    @Test
    void getFirstFreeSpot_WithLargeNumberOfSpots_ShouldReturnFirstFree() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);

        for (int i = 1; i <= 100; i++) {
            ParkingSpot spot = new ParkingSpot("SPOT-" + String.format("%03d", i), zone);
            zone.addSpot(spot);
            if (i <= 50) {
                spot.reserve();
            }
        }

        // Act
        ParkingSpot result = zone.getFirstFreeSpot();

        // Assert
        assertNotNull(result);
        assertEquals("SPOT-051", result.getSpotId());
    }

    @Test
    void getFirstFreeSpot_InDifferentZoneTypes_ShouldWork() {
        // Arrange
        ParkingZone standardZone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingZone premiumZone = new ParkingZone("ZONE-B", ZoneType.VIP, 0.9);

        ParkingSpot standardSpot = new ParkingSpot("SPOT-001", standardZone);
        ParkingSpot premiumSpot = new ParkingSpot("SPOT-002", premiumZone);

        standardZone.addSpot(standardSpot);
        premiumZone.addSpot(premiumSpot);

        // Act
        ParkingSpot standardResult = standardZone.getFirstFreeSpot();
        ParkingSpot premiumResult = premiumZone.getFirstFreeSpot();

        // Assert
        assertEquals(standardSpot, standardResult);
        assertEquals(premiumSpot, premiumResult);
    }
}
