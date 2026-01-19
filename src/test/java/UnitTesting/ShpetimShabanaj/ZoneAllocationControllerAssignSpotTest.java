package UnitTesting.ShpetimShabanaj;

import Controller.ZoneAllocationController;
import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Enum.SpotState;
import Enum.ZoneType;
import Model.ParkingSpot;
import Model.ParkingZone;
import Model.SpotAssignmentRequest;
import Model.SubscriptionPlan;
import Repository.ParkingZoneRepository;
import Repository.SubscriptionPlanRepository;
import Service.ZoneAllocationService;
import Service.ZoneOccupancyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneAllocationControllerAssignSpotTest {

    @Mock
    private ZoneAllocationService zoneAllocationService;

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @Mock
    private ZoneOccupancyService occupancyService;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @InjectMocks
    private ZoneAllocationController controller;

    @Mock
    private SubscriptionPlan mockPlan;

    @Mock
    private ParkingZone mockZone;

    @Mock
    private ParkingSpot mockSpot;

    private SpotAssignmentRequestDto testDto;
    private LocalDateTime testStartTime;

    @BeforeEach
    void setUp() {
        testStartTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        testDto = new SpotAssignmentRequestDto(
                "U1",
                ZoneType.STANDARD,
                testStartTime
        );
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should assign spot successfully when all conditions are met")
    void testAssignSpotHappyPathSpotAssignedSuccessfully() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(mockZone));
        when(mockZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(mockZone.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(mockZone.getZoneId()).thenReturn("Z1");
        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(0.50);
        when(zoneAllocationService.assignSpot(any(SpotAssignmentRequest.class), eq(mockZone)))
                .thenReturn(mockSpot);
        when(mockSpot.getSpotId()).thenReturn("S1");
        when(mockSpot.getState()).thenReturn(SpotState.FREE);
        when(mockSpot.getParkingZone()).thenReturn(mockZone);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify spot assignment",
                () -> assertNotNull(response),
                () -> assertEquals("S1", response.spotId()),
                () -> assertEquals(ZoneType.STANDARD, response.zoneType()),
                () -> assertEquals(SpotState.FREE, response.state()),
                () -> assertEquals("Z1", response.zoneId()),
                () -> verify(subscriptionPlanRepository, times(1)).getPlanForUser("U1"),
                () -> verify(parkingZoneRepository, times(1)).findAll(),
                () -> verify(occupancyService, times(1)).calculateOccupancyRatioForZone("Z1"),
                () -> verify(zoneAllocationService, times(1)).assignSpot(any(SpotAssignmentRequest.class), eq(mockZone))
        );

        verifyNoMoreInteractions(subscriptionPlanRepository, parkingZoneRepository, occupancyService, zoneAllocationService);
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should throw NullPointerException when dto is null")
    void testAssignSpotNullDtoThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> controller.assignSpot(null));

        verifyNoInteractions(subscriptionPlanRepository, parkingZoneRepository, occupancyService, zoneAllocationService);
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should throw NoSuchElementException when subscription plan not found")
    void testAssignSpotPlanNotFoundThrowsNoSuchElementException() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> controller.assignSpot(testDto));

        verify(subscriptionPlanRepository, times(1)).getPlanForUser("U1");
        verifyNoInteractions(parkingZoneRepository, occupancyService, zoneAllocationService);
        verifyNoMoreInteractions(subscriptionPlanRepository);
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should return null when no matching zone type found")
    void testAssignSpotNoMatchingZoneTypeReturnsNull() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));

        ParkingZone standardZone = mock(ParkingZone.class);
        when(standardZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(parkingZoneRepository.findAll()).thenReturn(List.of(standardZone));

        SpotAssignmentRequestDto evDto = new SpotAssignmentRequestDto(
                "U1", ZoneType.EV, testStartTime
        );

        SpotAssignmentResponseDto response = controller.assignSpot(evDto);

        assertAll("Verify no matching zone type handling",
                () -> assertNull(response),
                () -> verify(subscriptionPlanRepository, times(1)).getPlanForUser("U1"),
                () -> verify(parkingZoneRepository, times(1)).findAll()
        );

        verifyNoInteractions(occupancyService, zoneAllocationService);
        verifyNoMoreInteractions(subscriptionPlanRepository, parkingZoneRepository);
    }

    // TC-05
    @Test
    @DisplayName("TC-05: Should return null when occupancy equals threshold")
    void testAssignSpotOccupancyAtThresholdReturnsNull() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(mockZone));
        when(mockZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(mockZone.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(mockZone.getZoneId()).thenReturn("Z1");
        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(0.80);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify occupancy at threshold handling",
                () -> assertNull(response),
                () -> verify(occupancyService, times(1)).calculateOccupancyRatioForZone("Z1"),
                () -> verify(zoneAllocationService, never()).assignSpot(any(), any())
        );

        verifyNoMoreInteractions(occupancyService);
    }

    // TC-06
    @Test
    @DisplayName("TC-06: Should return null when occupancy above threshold")
    void testAssignSpotOccupancyAboveThresholdReturnsNull() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(mockZone));
        when(mockZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(mockZone.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(mockZone.getZoneId()).thenReturn("Z1");
        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(90.0);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify occupancy above threshold handling",
                () -> assertNull(response),
                () -> verify(occupancyService, times(1)).calculateOccupancyRatioForZone("Z1"),
                () -> verify(zoneAllocationService, never()).assignSpot(any(), any())
        );

        verifyNoMoreInteractions(occupancyService);
    }

    // TC-07
    @Test
    @DisplayName("TC-07: Should return null when zoneAllocationService returns null")
    void testAssignSpotAllocationServiceReturnsNullReturnsNull() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(mockZone));
        when(mockZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(mockZone.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(mockZone.getZoneId()).thenReturn("Z1");
        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(0.50);
        when(zoneAllocationService.assignSpot(any(SpotAssignmentRequest.class), eq(mockZone)))
                .thenReturn(null);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify null spot handling",
                () -> assertNull(response),
                () -> verify(zoneAllocationService, times(1)).assignSpot(any(SpotAssignmentRequest.class), eq(mockZone))
        );
    }

    // TC-08
    @Test
    @DisplayName("TC-08: Should create SpotAssignmentRequest with correct values")
    void testAssignSpotVerifySpotAssignmentRequestConstruction() {
        LocalDateTime specificTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        SpotAssignmentRequestDto dto = new SpotAssignmentRequestDto(
                "U2", ZoneType.VIP, specificTime
        );

        when(subscriptionPlanRepository.getPlanForUser("U2")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(mockZone));
        when(mockZone.getZoneType()).thenReturn(ZoneType.VIP);
        when(mockZone.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(mockZone.getZoneId()).thenReturn("Z1");
        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(0.50);
        when(zoneAllocationService.assignSpot(any(SpotAssignmentRequest.class), eq(mockZone)))
                .thenReturn(mockSpot);
        when(mockSpot.getSpotId()).thenReturn("S1");
        when(mockSpot.getState()).thenReturn(SpotState.FREE);
        when(mockSpot.getParkingZone()).thenReturn(mockZone);

        controller.assignSpot(dto);

        ArgumentCaptor<SpotAssignmentRequest> requestCaptor = ArgumentCaptor.forClass(SpotAssignmentRequest.class);
        verify(zoneAllocationService).assignSpot(requestCaptor.capture(), eq(mockZone));

        SpotAssignmentRequest capturedRequest = requestCaptor.getValue();
        assertAll("Verify request construction",
                () -> assertEquals("U2", capturedRequest.getUserId()),
                () -> assertEquals(ZoneType.VIP, capturedRequest.getRequestedZoneType()),
                () -> assertEquals(mockPlan, capturedRequest.getSubscriptionPlan()),
                () -> assertEquals(specificTime, capturedRequest.getRequestedStartTime())
        );
    }

    // TC-09
    @Test
    @DisplayName("TC-09: Should map response DTO correctly from spot")
    void testAssignSpotResponseDtoMappingMappedCorrectly() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(mockZone));
        when(mockZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(mockZone.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(mockZone.getZoneId()).thenReturn("Z1");
        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(0.50);
        when(zoneAllocationService.assignSpot(any(SpotAssignmentRequest.class), eq(mockZone)))
                .thenReturn(mockSpot);
        when(mockSpot.getSpotId()).thenReturn("S1");
        when(mockSpot.getState()).thenReturn(SpotState.FREE);
        when(mockSpot.getParkingZone()).thenReturn(mockZone);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify response DTO mapping",
                () -> assertEquals("S1", response.spotId()),
                () -> assertEquals(ZoneType.STANDARD, response.zoneType()),
                () -> assertEquals(SpotState.FREE, response.state()),
                () -> assertEquals("Z1", response.zoneId())
        );
    }

    // TC-10
    @Test
    @DisplayName("TC-10: Should process multiple zones until matching type found")
    void testAssignSpotMultipleZonesSkipsNonMatchingTypes() {
        ParkingZone evZone = mock(ParkingZone.class);
        ParkingZone vipZone = mock(ParkingZone.class);
        ParkingZone standardZone = mock(ParkingZone.class);

        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(evZone, vipZone, standardZone));

        when(evZone.getZoneType()).thenReturn(ZoneType.EV);
        when(vipZone.getZoneType()).thenReturn(ZoneType.VIP);
        when(standardZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(standardZone.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(standardZone.getZoneId()).thenReturn("Z3");

        when(occupancyService.calculateOccupancyRatioForZone("Z3")).thenReturn(0.50);
        when(zoneAllocationService.assignSpot(any(SpotAssignmentRequest.class), eq(standardZone)))
                .thenReturn(mockSpot);
        when(mockSpot.getSpotId()).thenReturn("S1");
        when(mockSpot.getState()).thenReturn(SpotState.FREE);
        when(mockSpot.getParkingZone()).thenReturn(standardZone);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify zone selection",
                () -> assertNotNull(response),
                () -> assertEquals("Z3", response.zoneId()),
                () -> verify(occupancyService, times(1)).calculateOccupancyRatioForZone("Z3"),
                () -> verify(occupancyService, never()).calculateOccupancyRatioForZone("Z1"),
                () -> verify(occupancyService, never()).calculateOccupancyRatioForZone("Z2")
        );
    }

    // TC-11
    @Test
    @DisplayName("TC-11: Should use first matching zone with FREE capacity")
    void testAssignSpotFirstMatchingZoneUsed() {
        ParkingZone standardZone1 = mock(ParkingZone.class);
        ParkingZone standardZone2 = mock(ParkingZone.class);

        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(standardZone1, standardZone2));

        when(standardZone1.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(standardZone1.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(standardZone1.getZoneId()).thenReturn("Z1");

        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(0.50);
        when(zoneAllocationService.assignSpot(any(SpotAssignmentRequest.class), eq(standardZone1)))
                .thenReturn(mockSpot);
        when(mockSpot.getSpotId()).thenReturn("S1");
        when(mockSpot.getState()).thenReturn(SpotState.FREE);
        when(mockSpot.getParkingZone()).thenReturn(standardZone1);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify first zone used",
                () -> assertNotNull(response),
                () -> assertEquals("Z1", response.zoneId()),
                () -> verify(occupancyService, times(1)).calculateOccupancyRatioForZone("Z1"),
                () -> verify(zoneAllocationService, times(1)).assignSpot(any(SpotAssignmentRequest.class), eq(standardZone1)),
                () -> verifyNoInteractions(standardZone2)
        );
    }

    // TC-12
    @Test
    @DisplayName("TC-12: Should return null when zone list is empty")
    void testAssignSpotEmptyZoneListReturnsNull() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(Collections.emptyList());

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify empty zone list handling",
                () -> assertNull(response),
                () -> verify(parkingZoneRepository, times(1)).findAll()
        );

        verifyNoInteractions(occupancyService, zoneAllocationService);
    }

    // TC-13
    @Test
    @DisplayName("TC-13: Should allow assignment when occupancy below threshold")
    void testAssignSpotOccupancyBelowThresholdAllowsAssignment() {
        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(mockZone));
        when(mockZone.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(mockZone.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(mockZone.getZoneId()).thenReturn("Z1");
        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(0.70);
        when(zoneAllocationService.assignSpot(any(SpotAssignmentRequest.class), eq(mockZone)))
                .thenReturn(mockSpot);
        when(mockSpot.getSpotId()).thenReturn("S1");
        when(mockSpot.getState()).thenReturn(SpotState.FREE);
        when(mockSpot.getParkingZone()).thenReturn(mockZone);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify below threshold assignment",
                () -> assertNotNull(response),
                () -> verify(zoneAllocationService, times(1)).assignSpot(any(SpotAssignmentRequest.class), eq(mockZone))
        );
    }

    // TC-14
    @Test
    @DisplayName("TC-14: Should call occupancyService for each matching zone")
    void testAssignSpotOccupancyServiceCalledForEachMatchingZone() {
        ParkingZone standardZone1 = mock(ParkingZone.class);
        ParkingZone standardZone2 = mock(ParkingZone.class);

        when(subscriptionPlanRepository.getPlanForUser("U1")).thenReturn(Optional.of(mockPlan));
        when(parkingZoneRepository.findAll()).thenReturn(List.of(standardZone1, standardZone2));

        when(standardZone1.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(standardZone1.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(standardZone1.getZoneId()).thenReturn("Z1");

        when(standardZone2.getZoneType()).thenReturn(ZoneType.STANDARD);
        when(standardZone2.getMaxOccupancyThreshold()).thenReturn(0.80);
        when(standardZone2.getZoneId()).thenReturn("Z2");

        when(occupancyService.calculateOccupancyRatioForZone("Z1")).thenReturn(0.90);
        when(occupancyService.calculateOccupancyRatioForZone("Z2")).thenReturn(0.50);
        when(zoneAllocationService.assignSpot(any(SpotAssignmentRequest.class), eq(standardZone2)))
                .thenReturn(mockSpot);
        when(mockSpot.getSpotId()).thenReturn("S2");
        when(mockSpot.getState()).thenReturn(SpotState.FREE);
        when(mockSpot.getParkingZone()).thenReturn(standardZone2);

        SpotAssignmentResponseDto response = controller.assignSpot(testDto);

        assertAll("Verify occupancy checked for both zones",
                () -> assertNotNull(response),
                () -> assertEquals("Z2", response.zoneId()),
                () -> verify(occupancyService, times(1)).calculateOccupancyRatioForZone("Z1"),
                () -> verify(occupancyService, times(1)).calculateOccupancyRatioForZone("Z2"),
                () -> verify(zoneAllocationService, never()).assignSpot(any(SpotAssignmentRequest.class), eq(standardZone1)),
                () -> verify(zoneAllocationService, times(1)).assignSpot(any(SpotAssignmentRequest.class), eq(standardZone2))
        );
    }
}
