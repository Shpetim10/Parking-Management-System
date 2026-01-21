package UnitTesting.NikolaRigo;

import Model.ParkingZone;
import org.junit.jupiter.api.Test;
import Enum.ZoneType;
import static org.junit.jupiter.api.Assertions.*;

class ParkingZoneConstructorTest {

    @Test
    void constructor_WithValidParameters_ShouldCreateInstance() {
        // Arrange
        String zoneId = "ZONE-A";
        ZoneType zoneType = ZoneType.STANDARD;
        double maxOccupancyThreshold = 0.8;

        // Act
        ParkingZone zone = new ParkingZone(zoneId, zoneType, maxOccupancyThreshold);

        // Assert
        assertNotNull(zone);
        assertEquals(zoneId, zone.getZoneId());
        assertEquals(zoneType, zone.getZoneType());
        assertEquals(maxOccupancyThreshold, zone.getMaxOccupancyThreshold());
        assertEquals(0, zone.getTotalSpots());
    }

    @Test
    void constructor_WithNullZoneId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ParkingZone(null, ZoneType.STANDARD, 0.8)
        );

        assertEquals("Zone ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void constructor_WithEmptyZoneId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ParkingZone("", ZoneType.STANDARD, 0.8)
        );

        assertEquals("Zone ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void constructor_WithBlankZoneId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ParkingZone("   ", ZoneType.STANDARD, 0.8)
        );

        assertEquals("Zone ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void constructor_WithNullZoneType_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ParkingZone("ZONE-A", null, 0.8)
        );

        assertEquals("Zone type cannot be null", exception.getMessage());
    }

    @Test
    void constructor_WithNegativeOccupancyThreshold_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ParkingZone("ZONE-A", ZoneType.STANDARD, -0.1)
        );

        assertEquals("Occupancy threshold must be between 0 and 1", exception.getMessage());
    }

    @Test
    void constructor_WithOccupancyThresholdAboveOne_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ParkingZone("ZONE-A", ZoneType.STANDARD, 1.1)
        );

        assertEquals("Occupancy threshold must be between 0 and 1", exception.getMessage());
    }

    @Test
    void constructor_WithMinimumOccupancyThreshold_ShouldCreateInstance() {
        // Arrange
        double threshold = 0.0;

        // Act
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, threshold);

        // Assert
        assertEquals(0.0, zone.getMaxOccupancyThreshold());
    }

    @Test
    void constructor_WithMaximumOccupancyThreshold_ShouldCreateInstance() {
        // Arrange
        double threshold = 1.0;

        // Act
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, threshold);

        // Assert
        assertEquals(1.0, zone.getMaxOccupancyThreshold());
    }

    @Test
    void constructor_WithMidRangeOccupancyThreshold_ShouldCreateInstance() {
        // Arrange
        double threshold = 0.5;

        // Act
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, threshold);

        // Assert
        assertEquals(0.5, zone.getMaxOccupancyThreshold());
    }

    @Test
    void constructor_WithDifferentZoneTypes_ShouldCreateInstances() {
        // Arrange
        ZoneType[] zoneTypes = {ZoneType.STANDARD, ZoneType.VIP}; // Add other types if they exist

        // Act & Assert
        for (ZoneType zoneType : zoneTypes) {
            ParkingZone zone = new ParkingZone("ZONE-" + zoneType, zoneType, 0.8);
            assertEquals(zoneType, zone.getZoneType());
        }
    }

    @Test
    void constructor_ShouldInitializeWithEmptySpotsList() {
        // Act
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);

        // Assert
        assertNotNull(zone.getSpots());
        assertEquals(0, zone.getTotalSpots());
        assertEquals(0, zone.getFreeSpotsCount());
        assertTrue(zone.getSpots().isEmpty());
    }

    @Test
    void constructor_WithVerySmallPositiveThreshold_ShouldCreateInstance() {
        // Arrange
        double threshold = 0.01;

        // Act
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, threshold);

        // Assert
        assertEquals(0.01, zone.getMaxOccupancyThreshold());
    }

    @Test
    void constructor_WithVeryCloseToOneThreshold_ShouldCreateInstance() {
        // Arrange
        double threshold = 0.99;

        // Act
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, threshold);

        // Assert
        assertEquals(0.99, zone.getMaxOccupancyThreshold());
    }

    @Test
    void constructor_WithThresholdJustAboveOne_ShouldThrowException() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new ParkingZone("ZONE-A", ZoneType.STANDARD, 1.0001)
        );
    }

    @Test
    void constructor_WithThresholdJustBelowZero_ShouldThrowException() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new ParkingZone("ZONE-A", ZoneType.STANDARD, -0.0001)
        );
    }

    @Test
    void constructor_WithDifferentZoneIds_ShouldCreateInstances() {
        // Arrange
        String[] zoneIds = {"ZONE-A", "ZONE-001", "Z1", "parking-zone-premium"};

        // Act & Assert
        for (String zoneId : zoneIds) {
            ParkingZone zone = new ParkingZone(zoneId, ZoneType.STANDARD, 0.8);
            assertEquals(zoneId, zone.getZoneId());
        }
    }

    @Test
    void constructor_ShouldNotReturnNullForGetSpots() {
        // Act
        ParkingZone zone = new ParkingZone("ZONE-A", ZoneType.STANDARD, 0.8);

        // Assert
        assertNotNull(zone.getSpots());
    }
}