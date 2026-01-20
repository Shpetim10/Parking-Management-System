package UnitTesting.NikolaRigo;

import Model.ZoneOccupancyReport;
import org.junit.jupiter.api.Test;
import Enum.ZoneType;
import static org.junit.jupiter.api.Assertions.*;

class ZoneOccupancyReportConstructorTest {

    @Test
    void constructor_WithValidParameters_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        double averageOccupancy = 0.75;
        int totalReservations = 100;
        int noShowReservations = 10;

        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                zoneType,
                averageOccupancy,
                totalReservations,
                noShowReservations
        );

        // Assert
        assertNotNull(report);
        assertEquals(zoneType, report.getZoneType());
        assertEquals(0.75, report.getAverageOccupancy(), 0.0001);
        assertEquals(100, report.getTotalReservations());
        assertEquals(10, report.getNoShowReservations());
    }

    @Test
    void constructor_WithNullZoneType_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ZoneOccupancyReport(null, 0.75, 100, 10)
        );

        assertEquals("Zone type cannot be null", exception.getMessage());
    }

    @Test
    void constructor_WithNegativeAverageOccupancy_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ZoneOccupancyReport(ZoneType.STANDARD, -0.1, 100, 10)
        );

        assertEquals("Average occupancy must be between 0 and 1", exception.getMessage());
    }

    @Test
    void constructor_WithAverageOccupancyAboveOne_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ZoneOccupancyReport(ZoneType.STANDARD, 1.1, 100, 10)
        );

        assertEquals("Average occupancy must be between 0 and 1", exception.getMessage());
    }

    @Test
    void constructor_WithMinimumAverageOccupancy_ShouldCreateInstance() {
        // Arrange
        double occupancy = 0.0;

        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                occupancy,
                100,
                10
        );

        // Assert
        assertEquals(0.0, report.getAverageOccupancy(), 0.0001);
    }

    @Test
    void constructor_WithMaximumAverageOccupancy_ShouldCreateInstance() {
        // Arrange
        double occupancy = 1.0;

        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                occupancy,
                100,
                10
        );

        // Assert
        assertEquals(1.0, report.getAverageOccupancy(), 0.0001);
    }

    @Test
    void constructor_WithMidRangeAverageOccupancy_ShouldCreateInstance() {
        // Arrange
        double occupancy = 0.5;

        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                occupancy,
                100,
                10
        );

        // Assert
        assertEquals(0.5, report.getAverageOccupancy(), 0.0001);
    }

    @Test
    void constructor_WithZeroTotalReservations_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                0.5,
                0,
                0
        );

        // Assert
        assertEquals(0, report.getTotalReservations());
    }

    @Test
    void constructor_WithZeroNoShowReservations_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                0.5,
                100,
                0
        );

        // Assert
        assertEquals(0, report.getNoShowReservations());
    }

    @Test
    void constructor_WithNegativeTotalReservations_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                0.5,
                -10,
                0
        );

        // Assert
        assertEquals(-10, report.getTotalReservations());
    }

    @Test
    void constructor_WithNegativeNoShowReservations_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                0.5,
                100,
                -5
        );

        // Assert
        assertEquals(-5, report.getNoShowReservations());
    }

    @Test
    void constructor_WithNoShowGreaterThanTotal_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                0.5,
                100,
                150
        );

        // Assert
        assertEquals(100, report.getTotalReservations());
        assertEquals(150, report.getNoShowReservations());
    }

    @Test
    void constructor_WithLargeReservationNumbers_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                0.85,
                100000,
                5000
        );

        // Assert
        assertEquals(100000, report.getTotalReservations());
        assertEquals(5000, report.getNoShowReservations());
    }

    @Test
    void constructor_WithDifferentZoneTypes_ShouldCreateInstances() {
        // Arrange
        ZoneType[] zoneTypes = {ZoneType.STANDARD, ZoneType.VIP}; // Add other types if they exist

        // Act & Assert
        for (ZoneType zoneType : zoneTypes) {
            ZoneOccupancyReport report = new ZoneOccupancyReport(
                    zoneType,
                    0.75,
                    100,
                    10
            );
            assertEquals(zoneType, report.getZoneType());
        }
    }

    @Test
    void constructor_WithVerySmallPositiveOccupancy_ShouldCreateInstance() {
        // Arrange
        double occupancy = 0.001;

        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                occupancy,
                100,
                10
        );

        // Assert
        assertEquals(0.001, report.getAverageOccupancy(), 0.0001);
    }

    @Test
    void constructor_WithVeryCloseToOneOccupancy_ShouldCreateInstance() {
        // Arrange
        double occupancy = 0.999;

        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                occupancy,
                100,
                10
        );

        // Assert
        assertEquals(0.999, report.getAverageOccupancy(), 0.0001);
    }

    @Test
    void constructor_WithOccupancyJustAboveOne_ShouldThrowException() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new ZoneOccupancyReport(ZoneType.STANDARD, 1.0001, 100, 10)
        );
    }

    @Test
    void constructor_WithOccupancyJustBelowZero_ShouldThrowException() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new ZoneOccupancyReport(ZoneType.STANDARD, -0.0001, 100, 10)
        );
    }

    @Test
    void constructor_WithAllZeroValues_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                0.0,
                0,
                0
        );

        // Assert
        assertNotNull(report);
        assertEquals(0.0, report.getAverageOccupancy(), 0.0001);
        assertEquals(0, report.getTotalReservations());
        assertEquals(0, report.getNoShowReservations());
    }

    @Test
    void constructor_WithMaximumOccupancyAndNoReservations_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                1.0,
                0,
                0
        );

        // Assert
        assertEquals(1.0, report.getAverageOccupancy(), 0.0001);
        assertEquals(0, report.getTotalReservations());
    }

    @Test
    void constructor_WithPreciseDecimalOccupancy_ShouldPreserveValue() {
        // Arrange
        double occupancy = 0.12345;

        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                occupancy,
                100,
                10
        );

        // Assert
        assertEquals(0.12345, report.getAverageOccupancy(), 0.00001);
    }

    @Test
    void constructor_ForPremiumZone_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.VIP,
                0.90,
                200,
                5
        );

        // Assert
        assertEquals(ZoneType.VIP, report.getZoneType());
        assertEquals(0.90, report.getAverageOccupancy(), 0.0001);
        assertEquals(200, report.getTotalReservations());
        assertEquals(5, report.getNoShowReservations());
    }

    @Test
    void constructor_WithEqualTotalAndNoShowReservations_ShouldCreateInstance() {
        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                0.0,
                50,
                50
        );

        // Assert
        assertEquals(50, report.getTotalReservations());
        assertEquals(50, report.getNoShowReservations());
    }

    @Test
    void constructor_WithVeryLargeOccupancyPrecision_ShouldHandleCorrectly() {
        // Arrange
        double occupancy = 0.123456789;

        // Act
        ZoneOccupancyReport report = new ZoneOccupancyReport(
                ZoneType.STANDARD,
                occupancy,
                100,
                10
        );

        // Assert
        assertEquals(occupancy, report.getAverageOccupancy(), 0.0000001);
    }
}
