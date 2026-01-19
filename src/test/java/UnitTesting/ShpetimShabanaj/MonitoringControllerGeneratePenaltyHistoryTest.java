package UnitTesting.ShpetimShabanaj;

import Controller.MonitoringController;
import Dto.Monitoring.PenaltySummaryResponseDto;
import Model.PenaltyHistory;
import Model.PenaltySummaryReport;
import Repository.ParkingZoneRepository;
import Repository.PenaltyHistoryRepository;
import Service.MonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonitoringControllerGeneratePenaltyHistoryTest {

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private PenaltyHistoryRepository penaltyHistoryRepository;

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @InjectMocks
    private MonitoringController controller;

    @Mock
    private PenaltySummaryReport penaltySummaryReport;

    // TC-01
    @Test
    @DisplayName("TC-01: Should generate penalty summary using histories from repository")
    void testGeneratePenaltySummaryWithHistories() {
        PenaltyHistory history1 = mock(PenaltyHistory.class);
        PenaltyHistory history2 = mock(PenaltyHistory.class);
        List<PenaltyHistory> histories = List.of(history1, history2);

        BigDecimal totalOverstay = BigDecimal.valueOf(3);
        BigDecimal totalLostTicket = BigDecimal.valueOf(2);
        BigDecimal totalMisuse = BigDecimal.valueOf(1);
        int blacklistCandidates = 4;

        when(penaltyHistoryRepository.findAll()).thenReturn(histories);

        when(penaltySummaryReport.getTotalOverstay()).thenReturn(totalOverstay);
        when(penaltySummaryReport.getTotalLostTicket()).thenReturn(totalLostTicket);
        when(penaltySummaryReport.getTotalMisuse()).thenReturn(totalMisuse);
        when(penaltySummaryReport.getBlacklistCandidatesCount()).thenReturn(blacklistCandidates);

        when(monitoringService.generatePenaltySummary(histories))
                .thenReturn(penaltySummaryReport);

        PenaltySummaryResponseDto response = controller.generatePenaltySummary();

        assertAll("Verify repo call, service call, and response mapping",
                () -> verify(penaltyHistoryRepository, times(1)).findAll(),
                () -> verify(monitoringService, times(1)).generatePenaltySummary(histories),
                () -> assertEquals(totalOverstay, response.totalOverstay()),
                () -> assertEquals(totalLostTicket, response.totalLostTicket()),
                () -> assertEquals(totalMisuse, response.totalMisuse()),
                () -> assertEquals(blacklistCandidates, response.blacklistCandidatesCount())
        );
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should generate penalty summary correctly when there are no penalty histories")
    void testGeneratePenaltySummaryWithEmptyHistories() {
        List<PenaltyHistory> emptyHistories = Collections.emptyList();

        BigDecimal totalOverstay = BigDecimal.ZERO;
        BigDecimal totalLostTicket = BigDecimal.ZERO;
        BigDecimal totalMisuse = BigDecimal.ZERO;
        int blacklistCandidates = 0;

        when(penaltyHistoryRepository.findAll()).thenReturn(emptyHistories);

        when(penaltySummaryReport.getTotalOverstay()).thenReturn(totalOverstay);
        when(penaltySummaryReport.getTotalLostTicket()).thenReturn(totalLostTicket);
        when(penaltySummaryReport.getTotalMisuse()).thenReturn(totalMisuse);
        when(penaltySummaryReport.getBlacklistCandidatesCount()).thenReturn(blacklistCandidates);

        when(monitoringService.generatePenaltySummary(emptyHistories))
                .thenReturn(penaltySummaryReport);

        PenaltySummaryResponseDto response = controller.generatePenaltySummary();

        assertAll("Verify summary is generated from empty histories list",
                () -> verify(penaltyHistoryRepository, times(1)).findAll(),
                () -> verify(monitoringService, times(1)).generatePenaltySummary(emptyHistories),
                () -> assertEquals(BigDecimal.ZERO, response.totalOverstay()),
                () -> assertEquals(BigDecimal.ZERO, response.totalLostTicket()),
                () -> assertEquals(BigDecimal.ZERO, response.totalMisuse()),
                () -> assertEquals(0, response.blacklistCandidatesCount())
        );
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should throw NullPointerException when repository returns null instead of list")
    void testGeneratePenaltySummaryRepoReturnsNull() {
        when(penaltyHistoryRepository.findAll()).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> controller.generatePenaltySummary());
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should throw NullPointerException when service returns null report")
    void testGeneratePenaltySummaryServiceReturnsNull() {
        List<PenaltyHistory> histories = List.of(mock(PenaltyHistory.class));

        when(penaltyHistoryRepository.findAll()).thenReturn(histories);
        when(monitoringService.generatePenaltySummary(histories)).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> controller.generatePenaltySummary());

        verify(penaltyHistoryRepository, times(1)).findAll();
        verify(monitoringService, times(1)).generatePenaltySummary(histories);
    }
}
