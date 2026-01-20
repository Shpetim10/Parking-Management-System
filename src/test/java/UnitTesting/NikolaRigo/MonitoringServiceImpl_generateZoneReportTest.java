package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import Service.impl.MonitoringServiceImpl;
import Model.ZoneOccupancyReport;
import Enum.ZoneType;
import static org.junit.jupiter.api.Assertions.*;

class MonitoringServiceImpl_generateZoneReportTest {

    private MonitoringServiceImpl monitoringService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        monitoringService = new MonitoringServiceImpl();
    }

    @Test
    void generateZoneReport_WithValidParameters_ShouldCreateReportWithCorrectValues() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        double averageOccupancy = 0.75;
        int totalReservations = 100;
        int noShowReservations = 10;

        // Act
        ZoneOccupancyReport report = monitoringService.generateZoneReport(
                zoneType,
                averageOccupancy,
                totalReservations,
                noShowReservations
        );

        // Assert
        assertNotNull(report);
        assertEquals(ZoneType.STANDARD, report.getZoneType());
        assertEquals(0.75, report.getAverageOccupancy(), 0.0001);
        assertEquals(100, report.getTotalReservations());
        assertEquals(10, report.getNoShowReservations());
    }

    @Test
    void generateZoneReport_WithPremiumZoneType_ShouldCreatePremiumZoneReport() {
        // Arrange
        ZoneType zoneType = ZoneType.VIP;
        double averageOccupancy = 0.90;
        int totalReservations = 200;
        int noShowReservations = 5;

        // Act
        ZoneOccupancyReport report = monitoringService.generateZoneReport(
                zoneType,
                averageOccupancy,
                totalReservations,
                noShowReservations
        );

        // Assert
        assertNotNull(report);
        assertEquals(ZoneType.VIP, report.getZoneType());
        assertEquals(0.90, report.getAverageOccupancy(), 0.0001);
        assertEquals(200, report.getTotalReservations());
        assertEquals(5, report.getNoShowReservations());
    }

    @Test
    void generateZoneReport_WithZeroOccupancyAndReservations_ShouldCreateReportWithZeros() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        double averageOccupancy = 0.0;
        int totalReservations = 0;
        int noShowReservations = 0;

        // Act
        ZoneOccupancyReport report = monitoringService.generateZoneReport(
                zoneType,
                averageOccupancy,
                totalReservations,
                noShowReservations
        );

        // Assert
        assertNotNull(report);
        assertEquals(ZoneType.STANDARD, report.getZoneType());
        assertEquals(0.0, report.getAverageOccupancy(), 0.0001);
        assertEquals(0, report.getTotalReservations());
        assertEquals(0, report.getNoShowReservations());
    }

    @Test
    void generateZoneReport_WithPreciseOccupancyValue_ShouldPreservePrecision() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        double averageOccupancy = 0.12345;
        int totalReservations = 150;
        int noShowReservations = 15;

        // Act
        ZoneOccupancyReport report = monitoringService.generateZoneReport(
                zoneType,
                averageOccupancy,
                totalReservations,
                noShowReservations
        );

        // Assert
        assertNotNull(report);
        assertEquals(0.12345, report.getAverageOccupancy(), 0.00001);
        assertEquals(150, report.getTotalReservations());
        assertEquals(15, report.getNoShowReservations());
    }

    @Test
    void generateZoneReport_CalledMultipleTimes_ShouldCreateIndependentReports() {
        // Arrange
        ZoneType zoneType1 = ZoneType.STANDARD;
        ZoneType zoneType2 = ZoneType.VIP;

        // Act
        ZoneOccupancyReport report1 = monitoringService.generateZoneReport(
                zoneType1, 0.70, 100, 10
        );
        ZoneOccupancyReport report2 = monitoringService.generateZoneReport(
                zoneType2, 0.80, 200, 20
        );

        // Assert
        assertNotNull(report1);
        assertNotNull(report2);
        assertNotSame(report1, report2);
        assertEquals(ZoneType.STANDARD, report1.getZoneType());
        assertEquals(ZoneType.VIP, report2.getZoneType());
        assertEquals(0.70, report1.getAverageOccupancy(), 0.0001);
        assertEquals(0.80, report2.getAverageOccupancy(), 0.0001);
    }

    @Test
    void generateZoneReport_WithDifferentZoneTypes_ShouldCreateCorrectReportForEach() {
        // Arrange & Act
        ZoneOccupancyReport standardReport = monitoringService.generateZoneReport(
                ZoneType.STANDARD, 0.65, 80, 8
        );
        ZoneOccupancyReport premiumReport = monitoringService.generateZoneReport(
                ZoneType.VIP, 0.90, 120, 12
        );

        // Assert
        assertEquals(ZoneType.STANDARD, standardReport.getZoneType());
        assertEquals(ZoneType.VIP, premiumReport.getZoneType());
        assertEquals(0.65, standardReport.getAverageOccupancy(), 0.0001);
        assertEquals(0.90, premiumReport.getAverageOccupancy(), 0.0001);
        assertEquals(80, standardReport.getTotalReservations());
        assertEquals(120, premiumReport.getTotalReservations());
    }

    @Test
    void generateZoneReport_WithMidRangeValues_ShouldCreateReportCorrectly() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        double averageOccupancy = 0.50;
        int totalReservations = 250;
        int noShowReservations = 25;

        // Act
        ZoneOccupancyReport report = monitoringService.generateZoneReport(
                zoneType,
                averageOccupancy,
                totalReservations,
                noShowReservations
        );

        // Assert
        assertNotNull(report);
        assertEquals(ZoneType.STANDARD, report.getZoneType());
        assertEquals(0.50, report.getAverageOccupancy(), 0.0001);
        assertEquals(250, report.getTotalReservations());
        assertEquals(25, report.getNoShowReservations());
    }
}
