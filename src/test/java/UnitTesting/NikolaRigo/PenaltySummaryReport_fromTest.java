package UnitTesting.NikolaRigo;

import Model.Penalty;
import Model.PenaltyHistory;
import Model.PenaltySummaryReport;
import org.junit.jupiter.api.Test;
import Enum.PenaltyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PenaltySummaryReport_fromTest {

    @Test
    void from_WithEmptyList_ShouldReturnReportWithZeroTotals() {
        // Arrange
        List<PenaltyHistory> histories = Collections.emptyList();

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertNotNull(report);
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalOverstay()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalLostTicket()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalMisuse()));
        assertEquals(0, report.getBlacklistCandidatesCount());
    }

    @Test
    void from_WithEmptyPenaltyHistories_ShouldReturnReportWithZeroTotals() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        histories.add(new PenaltyHistory());
        histories.add(new PenaltyHistory());

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalOverstay()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalLostTicket()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalMisuse()));
    }

    @Test
    void from_WithSingleOverstayPenalty_ShouldCalculateCorrectTotal() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();
        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        history.addPenalty(penalty);
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("50.00").compareTo(report.getTotalOverstay()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalLostTicket()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalMisuse()));
    }

    @Test
    void from_WithSingleLostTicketPenalty_ShouldCalculateCorrectTotal() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();
        Penalty penalty = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );
        history.addPenalty(penalty);
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalOverstay()));
        assertEquals(0, new BigDecimal("100.00").compareTo(report.getTotalLostTicket()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalMisuse()));
    }

    @Test
    void from_WithSingleMisusePenalty_ShouldCalculateCorrectTotal() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();
        Penalty penalty = new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("75.00"),
                LocalDateTime.now()
        );
        history.addPenalty(penalty);
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalOverstay()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalLostTicket()));
        assertEquals(0, new BigDecimal("75.00").compareTo(report.getTotalMisuse()));
    }

    @Test
    void from_WithMultiplePenaltiesOfSameType_ShouldSumCorrectly() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();

        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("30.00"),
                LocalDateTime.now()
        );
        Penalty penalty3 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("20.00"),
                LocalDateTime.now()
        );

        history.addPenalty(penalty1);
        history.addPenalty(penalty2);
        history.addPenalty(penalty3);
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("100.00").compareTo(report.getTotalOverstay()));
    }

    @Test
    void from_WithMixedPenaltyTypes_ShouldCalculateAllTotalsCorrectly() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();

        Penalty overstay = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        Penalty lostTicket = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );
        Penalty misuse = new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("75.00"),
                LocalDateTime.now()
        );

        history.addPenalty(overstay);
        history.addPenalty(lostTicket);
        history.addPenalty(misuse);
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("50.00").compareTo(report.getTotalOverstay()));
        assertEquals(0, new BigDecimal("100.00").compareTo(report.getTotalLostTicket()));
        assertEquals(0, new BigDecimal("75.00").compareTo(report.getTotalMisuse()));
    }

    @Test
    void from_WithMultiplePenaltyHistories_ShouldAggregateAllPenalties() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();

        PenaltyHistory history1 = new PenaltyHistory();
        history1.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        ));
        history1.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        ));

        PenaltyHistory history2 = new PenaltyHistory();
        history2.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("30.00"),
                LocalDateTime.now()
        ));
        history2.addPenalty(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("75.00"),
                LocalDateTime.now()
        ));

        histories.add(history1);
        histories.add(history2);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("80.00").compareTo(report.getTotalOverstay()));
        assertEquals(0, new BigDecimal("100.00").compareTo(report.getTotalLostTicket()));
        assertEquals(0, new BigDecimal("75.00").compareTo(report.getTotalMisuse()));
    }

    @Test
    void from_WithZeroAmountPenalties_ShouldHandleCorrectly() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();

        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
        history.addPenalty(penalty);
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalOverstay()));
    }

    @Test
    void from_WithDecimalAmounts_ShouldCalculateCorrectly() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();

        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("15.75"),
                LocalDateTime.now()
        ));
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("23.50"),
                LocalDateTime.now()
        ));
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("39.25").compareTo(report.getTotalOverstay()));
    }

    @Test
    void from_WithLargeNumberOfPenalties_ShouldCalculateCorrectly() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();

        for (int i = 0; i < 100; i++) {
            history.addPenalty(new Penalty(
                    PenaltyType.OVERSTAY,
                    new BigDecimal("10.00"),
                    LocalDateTime.now()
            ));
        }
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("1000.00").compareTo(report.getTotalOverstay()));
    }

    @Test
    void from_WithMixOfHistoriesAndTypes_ShouldAggregateCorrectly() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();

        PenaltyHistory history1 = new PenaltyHistory();
        history1.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("25.00"),
                LocalDateTime.now()
        ));
        history1.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("25.00"),
                LocalDateTime.now()
        ));

        PenaltyHistory history2 = new PenaltyHistory();
        history2.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        ));
        history2.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        ));

        PenaltyHistory history3 = new PenaltyHistory();
        history3.addPenalty(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("30.00"),
                LocalDateTime.now()
        ));
        history3.addPenalty(new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("45.00"),
                LocalDateTime.now()
        ));

        histories.add(history1);
        histories.add(history2);
        histories.add(history3);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("50.00").compareTo(report.getTotalOverstay()));
        assertEquals(0, new BigDecimal("100.00").compareTo(report.getTotalLostTicket()));
        assertEquals(0, new BigDecimal("75.00").compareTo(report.getTotalMisuse()));
    }

    @Test
    void from_ShouldAlwaysSetBlacklistCandidatesCountToZero() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        ));
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, report.getBlacklistCandidatesCount());
    }

    @Test
    void from_WithMixedEmptyAndNonEmptyHistories_ShouldCountOnlyNonEmpty() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();

        PenaltyHistory emptyHistory1 = new PenaltyHistory();
        PenaltyHistory nonEmptyHistory = new PenaltyHistory();
        nonEmptyHistory.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        ));
        PenaltyHistory emptyHistory2 = new PenaltyHistory();

        histories.add(emptyHistory1);
        histories.add(nonEmptyHistory);
        histories.add(emptyHistory2);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("50.00").compareTo(report.getTotalOverstay()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalLostTicket()));
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalMisuse()));
    }

    @Test
    void from_WithLargeAmounts_ShouldCalculateCorrectly() {
        // Arrange
        List<PenaltyHistory> histories = new ArrayList<>();
        PenaltyHistory history = new PenaltyHistory();

        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("9999.99"),
                LocalDateTime.now()
        ));
        history.addPenalty(new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("5000.50"),
                LocalDateTime.now()
        ));
        histories.add(history);

        // Act
        PenaltySummaryReport report = PenaltySummaryReport.from(histories);

        // Assert
        assertEquals(0, new BigDecimal("9999.99").compareTo(report.getTotalOverstay()));
        assertEquals(0, new BigDecimal("5000.50").compareTo(report.getTotalLostTicket()));
    }
}