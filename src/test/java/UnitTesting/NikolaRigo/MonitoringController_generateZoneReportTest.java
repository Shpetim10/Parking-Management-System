package UnitTesting.NikolaRigo;

import Controller.MonitoringController;
import Dto.Monitoring.ZoneOccupancyReportResponseDto;
import Dto.Monitoring.ZoneReportRequestDto;
import Model.ParkingZone;
import Model.ZoneOccupancyReport;
import Repository.ParkingZoneRepository;
import Repository.PenaltyHistoryRepository;
import Service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import Enum.ZoneType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private PenaltyHistoryRepository penaltyHistoryRepository;

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    private MonitoringController controller;

    @BeforeEach
    void setUp() {
        // We inject the mocks manually via the constructor to ensure the class is initialized correctly
        controller = new MonitoringController(
                monitoringService,
                penaltyHistoryRepository,
                parkingZoneRepository
        );
    }

    @Test
    @DisplayName("generateZoneReport: Should return correct DTO when zone exists")
    void generateZoneReport_Success() {
        // Arrange
        String zoneId = "zone-123";
        ZoneType zoneType = ZoneType.VIP;

        int totalReservations = 100;
        int noShows = 5;
        int totalTimeSlots = 200;
        double expectedOccupancy = 0.5;

        // FIX: Ensure arguments match the DTO definition: (ID, TimeSlots, Reservations, NoShows)
        ZoneReportRequestDto requestDto = new ZoneReportRequestDto(
                zoneId,
                totalTimeSlots,      // was passed as 3rd arg, move to 2nd
                totalReservations,   // was passed as 2nd arg, move to 3rd
                noShows              // was passed as 3rd arg, move to 4th
        );

        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneType()).thenReturn(zoneType);

        ZoneOccupancyReport mockReport = new ZoneOccupancyReport(zoneType, expectedOccupancy, totalReservations, noShows);

        when(parkingZoneRepository.findById(zoneId)).thenReturn(mockZone);

        // The stub arguments must match the logic derived from the DTO
        when(monitoringService.generateZoneReport(
                eq(zoneType),
                eq(expectedOccupancy),
                eq(totalReservations),
                eq(noShows))
        ).thenReturn(mockReport);

        // Act
        ZoneOccupancyReportResponseDto result = controller.generateZoneReport(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(zoneType, result.zoneType());
        assertEquals(expectedOccupancy, result.averageOccupancy());
    }

    @Test
    @DisplayName("generateZoneReport: Should handle zero time slots observed")
    void generateZoneReport_ZeroTimeSlots() {
        // Arrange
        String zoneId = "zone-ABC";
        ZoneType zoneType = ZoneType.STANDARD;

        // FIX: Pass 0 as the second argument (TimeSlots)
        ZoneReportRequestDto requestDto = new ZoneReportRequestDto(
                zoneId,
                0,   // totalTimeSlotsObserved
                50,  // totalReservations
                2    // noShowReservations
        );

        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneType()).thenReturn(zoneType);

        // Expect occupancy 0.0
        ZoneOccupancyReport mockReport = new ZoneOccupancyReport(zoneType, 0.0, 50, 2);

        when(parkingZoneRepository.findById(zoneId)).thenReturn(mockZone);

        // Stub expects 0.0 occupancy
        when(monitoringService.generateZoneReport(eq(zoneType), eq(0.0), eq(50), eq(2)))
                .thenReturn(mockReport);

        // Act
        ZoneOccupancyReportResponseDto result = controller.generateZoneReport(requestDto);

        // Assert
        assertEquals(0.0, result.averageOccupancy());
    }

    @Test
    @DisplayName("generateZoneReport: Should throw NullPointerException when DTO is null")
    void generateZoneReport_NullDto() {
        // Act & Assert
        Exception exception = assertThrows(NullPointerException.class, () -> {
            controller.generateZoneReport(null);
        });

        assertEquals("dto must not be null", exception.getMessage());

        // Verify no repositories were touched
        verifyNoInteractions(parkingZoneRepository);
        verifyNoInteractions(monitoringService);
    }
}