package UnitTesting.NikolaRigo;

import Model.Penalty;
import Model.PenaltyHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import Enum.PenaltyType;

class PenaltyHistory_addPenaltyTest {

    private PenaltyHistory penaltyHistory;

    @BeforeEach
    void setUp() {
        penaltyHistory = new PenaltyHistory();
    }

    @Test
    void addPenalty_WithValidPenalty_ShouldAddPenaltyToList() {
        // Arrange
        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(penalty);

        // Assert
        assertEquals(1, penaltyHistory.getPenaltyCount());
        assertTrue(penaltyHistory.getPenalties().contains(penalty));
    }

    @Test
    void addPenalty_WithNullPenalty_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> penaltyHistory.addPenalty(null)
        );

        assertEquals("Penalty cannot be null", exception.getMessage());
    }

    @Test
    void addPenalty_WithNullPenalty_ShouldNotChangeList() {
        // Arrange
        int initialCount = penaltyHistory.getPenaltyCount();

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> penaltyHistory.addPenalty(null)
        );

        assertEquals(initialCount, penaltyHistory.getPenaltyCount());
    }

    @Test
    void addPenalty_MultiplePenalties_ShouldAddAllToList() {
        // Arrange
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("25.00"),
                LocalDateTime.now()
        );
        Penalty penalty3 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("75.00"),
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(penalty1);
        penaltyHistory.addPenalty(penalty2);
        penaltyHistory.addPenalty(penalty3);

        // Assert
        assertEquals(3, penaltyHistory.getPenaltyCount());
        assertTrue(penaltyHistory.getPenalties().contains(penalty1));
        assertTrue(penaltyHistory.getPenalties().contains(penalty2));
        assertTrue(penaltyHistory.getPenalties().contains(penalty3));
    }

    @Test
    void addPenalty_ShouldMaintainOrderOfAddition() {
        // Arrange
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("25.00"),
                LocalDateTime.now()
        );
        Penalty penalty3 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("75.00"),
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(penalty1);
        penaltyHistory.addPenalty(penalty2);
        penaltyHistory.addPenalty(penalty3);

        // Assert
        List<Penalty> penalties = penaltyHistory.getPenalties();
        assertEquals(penalty1, penalties.get(0));
        assertEquals(penalty2, penalties.get(1));
        assertEquals(penalty3, penalties.get(2));
    }

    @Test
    void addPenalty_ShouldChangeIsEmptyFromTrueToFalse() {
        // Arrange
        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        assertTrue(penaltyHistory.isEmpty());

        // Act
        penaltyHistory.addPenalty(penalty);

        // Assert
        assertFalse(penaltyHistory.isEmpty());
    }

    @Test
    void addPenalty_ShouldIncrementPenaltyCount() {
        // Arrange
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("25.00"),
                LocalDateTime.now()
        );

        // Act & Assert
        assertEquals(0, penaltyHistory.getPenaltyCount());

        penaltyHistory.addPenalty(penalty1);
        assertEquals(1, penaltyHistory.getPenaltyCount());

        penaltyHistory.addPenalty(penalty2);
        assertEquals(2, penaltyHistory.getPenaltyCount());
    }

    @Test
    void addPenalty_ShouldUpdateTotalPenaltyAmount() {
        // Arrange
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("30.00"),
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(penalty1);
        penaltyHistory.addPenalty(penalty2);

        // Assert
        BigDecimal expectedTotal = new BigDecimal("80.00");
        assertEquals(0, expectedTotal.compareTo(penaltyHistory.getTotalPenaltyAmount()));
    }

    @Test
    void addPenalty_SamePenaltyTwice_ShouldAddBothInstances() {
        // Arrange
        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(penalty);
        penaltyHistory.addPenalty(penalty);

        // Assert
        assertEquals(2, penaltyHistory.getPenaltyCount());
    }

    @Test
    void addPenalty_WithZeroAmountPenalty_ShouldAddSuccessfully() {
        // Arrange
        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(penalty);

        // Assert
        assertEquals(1, penaltyHistory.getPenaltyCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(penaltyHistory.getTotalPenaltyAmount()));
    }

    @Test
    void addPenalty_WithLargeAmountPenalty_ShouldAddSuccessfully() {
        // Arrange
        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("10000.00"),
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(penalty);

        // Assert
        assertEquals(1, penaltyHistory.getPenaltyCount());
        BigDecimal expectedAmount = new BigDecimal("10000.00");
        assertEquals(0, expectedAmount.compareTo(penaltyHistory.getTotalPenaltyAmount()));
    }

    @Test
    void addPenalty_ManyPenalties_ShouldHandleLargeList() {
        // Arrange & Act
        for (int i = 0; i < 100; i++) {
            Penalty penalty = new Penalty(
                    PenaltyType.OVERSTAY,
                    new BigDecimal("10.00"),
                    LocalDateTime.now()
            );
            penaltyHistory.addPenalty(penalty);
        }

        // Assert
        assertEquals(100, penaltyHistory.getPenaltyCount());
        assertFalse(penaltyHistory.isEmpty());
        BigDecimal expectedTotal = new BigDecimal("1000.00");
        assertEquals(0, expectedTotal.compareTo(penaltyHistory.getTotalPenaltyAmount()));
    }

    @Test
    void addPenalty_AfterMultipleNullAttempts_ShouldStillAcceptValidPenalty() {
        // Arrange
        Penalty validPenalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        // Act
        assertThrows(IllegalArgumentException.class, () -> penaltyHistory.addPenalty(null));
        assertThrows(IllegalArgumentException.class, () -> penaltyHistory.addPenalty(null));
        penaltyHistory.addPenalty(validPenalty);

        // Assert
        assertEquals(1, penaltyHistory.getPenaltyCount());
        assertTrue(penaltyHistory.getPenalties().contains(validPenalty));
    }

    @Test
    void addPenalty_WithDifferentPenaltyTypes_ShouldAddAll() {
        // Arrange
        Penalty overstayPenalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );
        Penalty latePaymentPenalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("25.00"),
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(overstayPenalty);
        penaltyHistory.addPenalty(latePaymentPenalty);

        // Assert
        assertEquals(2, penaltyHistory.getPenaltyCount());
        BigDecimal expectedTotal = new BigDecimal("75.00");
        assertEquals(0, expectedTotal.compareTo(penaltyHistory.getTotalPenaltyAmount()));
    }

    @Test
    void addPenalty_WithDifferentTimestamps_ShouldAddAll() {
        // Arrange
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("25.00"),
                LocalDateTime.of(2025, 1, 15, 14, 30)
        );

        // Act
        penaltyHistory.addPenalty(penalty1);
        penaltyHistory.addPenalty(penalty2);

        // Assert
        assertEquals(2, penaltyHistory.getPenaltyCount());
    }

    @Test
    void addPenalty_WithDecimalAmounts_ShouldCalculateTotalCorrectly() {
        // Arrange
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("15.75"),
                LocalDateTime.now()
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("23.50"),
                LocalDateTime.now()
        );

        // Act
        penaltyHistory.addPenalty(penalty1);
        penaltyHistory.addPenalty(penalty2);

        // Assert
        BigDecimal expectedTotal = new BigDecimal("39.25");
        assertEquals(0, expectedTotal.compareTo(penaltyHistory.getTotalPenaltyAmount()));
    }
}
