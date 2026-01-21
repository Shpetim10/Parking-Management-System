package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import Service.impl.MonitoringServiceImpl;
import Model.PenaltyHistory;
import Model.PenaltySummaryReport;
import Model.Penalty;
import Enum.PenaltyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonitoringServiceImpl_generatePenaltySummaryTest {

    private MonitoringServiceImpl monitoringService;

    @Mock
    private PenaltyHistory mockHistory1;

    @Mock
    private PenaltyHistory mockHistory2;

    @Mock
    private PenaltyHistory mockHistory3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        monitoringService = new MonitoringServiceImpl();
    }

    @Test
    void generatePenaltySummary_WithEmptyList_ShouldReturnReportWithZeroTotals() {
        // Arrange
        List<PenaltyHistory> histories = Collections.emptyList();

        // Act
        PenaltySummaryReport report = monitoringService.generatePenaltySummary(histories);

        // Assert
        assertNotNull(report);
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalOverstay()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalLostTicket()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalMisuse()));
        assertEquals(0, report.getBlacklistCandidatesCount());
    }

    @Test
    void generatePenaltySummary_WithSingleHistoryContainingPenalties_ShouldCalculateCorrectTotals() {
        // Arrange
        List<Penalty> penalties = new ArrayList<>();
        penalties.add(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        ));
        penalties.add(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        ));
        penalties.add(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("75.00"),
                LocalDateTime.now()
        ));

        when(mockHistory1.getPenalties()).thenReturn(penalties);

        List<PenaltyHistory> histories = new ArrayList<>();
        histories.add(mockHistory1);

        // Act
        PenaltySummaryReport report = monitoringService.generatePenaltySummary(histories);

        // Assert
        assertNotNull(report);
        assertEquals(0, new BigDecimal("50.00").compareTo(report.getTotalOverstay()));
        assertEquals(0, new BigDecimal("100.00").compareTo(report.getTotalLostTicket()));
        assertEquals(0, new BigDecimal("75.00").compareTo(report.getTotalMisuse()));
        verify(mockHistory1).getPenalties();
    }

    @Test
    void generatePenaltySummary_WithMultipleHistories_ShouldAggregateAllPenalties() {
        // Arrange
        List<Penalty> penalties1 = new ArrayList<>();
        penalties1.add(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        ));
        penalties1.add(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        ));

        List<Penalty> penalties2 = new ArrayList<>();
        penalties2.add(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("30.00"),
                LocalDateTime.now()
        ));
        penalties2.add(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("75.00"),
                LocalDateTime.now()
        ));

        List<Penalty> penalties3 = new ArrayList<>();
        penalties3.add(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        ));

        when(mockHistory1.getPenalties()).thenReturn(penalties1);
        when(mockHistory2.getPenalties()).thenReturn(penalties2);
        when(mockHistory3.getPenalties()).thenReturn(penalties3);

        List<PenaltyHistory> histories = new ArrayList<>();
        histories.add(mockHistory1);
        histories.add(mockHistory2);
        histories.add(mockHistory3);

        // Act
        PenaltySummaryReport report = monitoringService.generatePenaltySummary(histories);

        // Assert
        assertNotNull(report);
        assertEquals(0, new BigDecimal("80.00").compareTo(report.getTotalOverstay())); // 50 + 30
        assertEquals(0, new BigDecimal("150.00").compareTo(report.getTotalLostTicket())); // 100 + 50
        assertEquals(0, new BigDecimal("75.00").compareTo(report.getTotalMisuse())); // 75
        verify(mockHistory1).getPenalties();
        verify(mockHistory2).getPenalties();
        verify(mockHistory3).getPenalties();
    }

    @Test
    void generatePenaltySummary_WithMixedEmptyAndNonEmptyHistories_ShouldCalculateCorrectly() {
        // Arrange
        List<Penalty> penalties = new ArrayList<>();
        penalties.add(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("40.00"),
                LocalDateTime.now()
        ));
        penalties.add(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("60.00"),
                LocalDateTime.now()
        ));

        when(mockHistory1.getPenalties()).thenReturn(Collections.emptyList());
        when(mockHistory2.getPenalties()).thenReturn(penalties);
        when(mockHistory3.getPenalties()).thenReturn(Collections.emptyList());

        List<PenaltyHistory> histories = new ArrayList<>();
        histories.add(mockHistory1);
        histories.add(mockHistory2);
        histories.add(mockHistory3);

        // Act
        PenaltySummaryReport report = monitoringService.generatePenaltySummary(histories);

        // Assert
        assertNotNull(report);
        assertEquals(0, new BigDecimal("40.00").compareTo(report.getTotalOverstay()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalLostTicket()));
        assertEquals(0, new BigDecimal("60.00").compareTo(report.getTotalMisuse()));
        verify(mockHistory1).getPenalties();
        verify(mockHistory2).getPenalties();
        verify(mockHistory3).getPenalties();
    }

    @Test
    void generatePenaltySummary_WithOnlyOverstayPenalties_ShouldOnlyCalculateOverstayTotal() {
        // Arrange
        List<Penalty> penalties = new ArrayList<>();
        penalties.add(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("25.00"),
                LocalDateTime.now()
        ));
        penalties.add(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("35.00"),
                LocalDateTime.now()
        ));
        penalties.add(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("40.00"),
                LocalDateTime.now()
        ));

        when(mockHistory1.getPenalties()).thenReturn(penalties);

        List<PenaltyHistory> histories = new ArrayList<>();
        histories.add(mockHistory1);

        // Act
        PenaltySummaryReport report = monitoringService.generatePenaltySummary(histories);

        // Assert
        assertNotNull(report);
        assertEquals(0, new BigDecimal("100.00").compareTo(report.getTotalOverstay()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalLostTicket()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalMisuse()));
        verify(mockHistory1).getPenalties();
    }

    @Test
    void generatePenaltySummary_WithLargeAmounts_ShouldCalculateCorrectly() {
        // Arrange
        List<Penalty> penalties = new ArrayList<>();
        penalties.add(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("9999.99"),
                LocalDateTime.now()
        ));
        penalties.add(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("5000.50"),
                LocalDateTime.now()
        ));
        penalties.add(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("3000.25"),
                LocalDateTime.now()
        ));

        when(mockHistory1.getPenalties()).thenReturn(penalties);

        List<PenaltyHistory> histories = new ArrayList<>();
        histories.add(mockHistory1);

        // Act
        PenaltySummaryReport report = monitoringService.generatePenaltySummary(histories);

        // Assert
        assertNotNull(report);
        assertEquals(0, new BigDecimal("9999.99").compareTo(report.getTotalOverstay()));
        assertEquals(0, new BigDecimal("5000.50").compareTo(report.getTotalLostTicket()));
        assertEquals(0, new BigDecimal("3000.25").compareTo(report.getTotalMisuse()));
        verify(mockHistory1).getPenalties();
    }
}
