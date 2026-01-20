package Artjol.UnitTesting;

import Controller.PenaltyController;
import Dto.Penalty.PenaltyCalculationRequestDto;
import Dto.Penalty.PenaltyCalculationResponseDto;
import Repository.PenaltyHistoryRepository;
import Service.MonitoringService;
import Service.PenaltyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

// Unit Tests for M-117: PenaltyController.calculatePenalty

class PenaltyControllerCalculatePenaltyTest {

    private PenaltyController controller;

    @Mock
    private PenaltyService mockPenaltyService;

    @Mock
    private MonitoringService mockMonitoringService;

    @Mock
    private PenaltyHistoryRepository mockPenaltyRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PenaltyController(
                mockPenaltyService,
                mockMonitoringService,
                mockPenaltyRepo
        );
    }

    @Test
    @DisplayName("calculates penalty successfully")
    void testCalculatePenalty_Success() {
        PenaltyCalculationRequestDto dto = new PenaltyCalculationRequestDto(
                true, 2.0, false, false,
                BigDecimal.TEN, BigDecimal.valueOf(100),
                BigDecimal.valueOf(50), BigDecimal.valueOf(75)
        );

        when(mockPenaltyService.calculatePenalty(
                anyBoolean(), anyDouble(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any()
        )).thenReturn(new BigDecimal("20.00"));

        PenaltyCalculationResponseDto response = controller.calculatePenalty(dto);

        assertNotNull(response);
        assertEquals(new BigDecimal("20.00"), response.totalPenalty());
        verify(mockPenaltyService).calculatePenalty(
                true, 2.0, false, false,
                BigDecimal.TEN, BigDecimal.valueOf(100),
                BigDecimal.valueOf(50), BigDecimal.valueOf(75)
        );
    }

    @Test
    @DisplayName("throws exception for null dto")
    void testCalculatePenalty_NullDto() {
        assertThrows(NullPointerException.class, () -> {
            controller.calculatePenalty(null);
        });
    }


    @Test
    @DisplayName("invokes penalty service with correct parameters")
    void testCalculatePenalty_CorrectParameters() {
        PenaltyCalculationRequestDto dto = new PenaltyCalculationRequestDto(
                true, 3.5, true, true,
                BigDecimal.valueOf(15), BigDecimal.valueOf(200),
                BigDecimal.valueOf(60), BigDecimal.valueOf(80)
        );

        when(mockPenaltyService.calculatePenalty(
                anyBoolean(), anyDouble(), anyBoolean(), anyBoolean(),
                any(), any(), any(), any()
        )).thenReturn(new BigDecimal("100.00"));

        controller.calculatePenalty(dto);

        verify(mockPenaltyService).calculatePenalty(
                true, 3.5, true, true,
                BigDecimal.valueOf(15), BigDecimal.valueOf(200),
                BigDecimal.valueOf(60), BigDecimal.valueOf(80)
        );
    }

}