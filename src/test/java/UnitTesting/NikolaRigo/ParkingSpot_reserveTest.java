package UnitTesting.NikolaRigo;

import Model.ParkingSpot;
import Model.ParkingZone;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import Enum.ZoneType;
import Enum.SpotState;

class ParkingSpot_reserveTest {

    @Test
    void reserve_WhenSpotIsFree_ShouldSetStateToReserved() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("SPOT-001", zone);

        // Act
        spot.reserve();

        // Assert
        assertEquals(SpotState.RESERVED, spot.getState());
    }

    @Test
    void reserve_WhenSpotIsAlreadyReserved_ShouldThrowIllegalStateException() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("SPOT-001", zone);
        spot.reserve(); // First reservation succeeds

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> spot.reserve()
        );

        assertEquals("Spot can only be reserved if it is FREE", exception.getMessage());
    }

    @Test
    void reserve_WhenSpotIsOccupied_ShouldThrowIllegalStateException() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("SPOT-001", zone);
        spot.occupy(); // Assuming this method exists

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> spot.reserve()
        );

        assertEquals("Spot can only be reserved if it is FREE", exception.getMessage());
    }

    @Test
    void reserve_WhenSpotIsFree_ShouldOnlyChangeState() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        String spotId = "SPOT-001";
        ParkingSpot spot = new ParkingSpot(spotId, zone);

        // Act
        spot.reserve();

        // Assert
        assertEquals(SpotState.RESERVED, spot.getState());
        assertEquals(spotId, spot.getSpotId());
        assertEquals(zone, spot.getParkingZone());
    }

    @Test
    void reserve_WhenSpotIsFree_ShouldTransitionCorrectly() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("SPOT-001", zone);
        SpotState initialState = spot.getState();

        // Act
        spot.reserve();

        // Assert
        assertEquals(SpotState.FREE, initialState);
        assertEquals(SpotState.RESERVED, spot.getState());
    }

    @Test
    void reserve_FromNonFreeStates_ShouldThrowIllegalStateException() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);

        // Test RESERVED state
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        spot1.reserve();

        assertThrows(
                IllegalStateException.class,
                () -> spot1.reserve(),
                "Should throw exception when state is RESERVED"
        );

        // Test OCCUPIED state (if you have an occupy() method)
        ParkingZone zone2 = new ParkingZone("ZONE-B", ZoneType.VIP, 0.9);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone2);
        spot2.occupy(); // Assuming this method exists

        assertThrows(
                IllegalStateException.class,
                () -> spot2.reserve(),
                "Should throw exception when state is OCCUPIED"
        );
    }

    @Test
    void reserve_MultipleSpots_ShouldReserveIndependently() {
        // Arrange
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone);

        // Act
        spot1.reserve();

        // Assert
        assertEquals(SpotState.RESERVED, spot1.getState());
        assertEquals(SpotState.FREE, spot2.getState());
    }

    @Test
    void reserve_InDifferentZones_ShouldWork() {
        // Arrange
        ParkingZone zone1 = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);
        ParkingZone zone2 = new ParkingZone("ZONE-B", ZoneType.VIP, 0.9);
        ParkingSpot spot1 = new ParkingSpot("SPOT-001", zone1);
        ParkingSpot spot2 = new ParkingSpot("SPOT-002", zone2);

        // Act
        spot1.reserve();
        spot2.reserve();

        // Assert
        assertEquals(SpotState.RESERVED, spot1.getState());
        assertEquals(SpotState.RESERVED, spot2.getState());
    }

    @Test
    void reserve_WithDifferentZoneTypes_ShouldWork() {
        // Test with various zone types
        ZoneType[] zoneTypes = {ZoneType.STANDARD, ZoneType.VIP}; // Add other types if they exist

        for (ZoneType zoneType : zoneTypes) {
            // Arrange
            ParkingZone zone = new ParkingZone("ZONE-" + zoneType, zoneType, 0.8);
            ParkingSpot spot = new ParkingSpot("SPOT-001", zone);

            // Act
            spot.reserve();

            // Assert
            assertEquals(SpotState.RESERVED, spot.getState());
        }
    }
}
