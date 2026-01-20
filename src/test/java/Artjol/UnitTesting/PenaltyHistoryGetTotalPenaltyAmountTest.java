package Artjol.UnitTesting;

import Model.PenaltyHistory;
import Model.Penalty;
import Enum.PenaltyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Unit Tests for M-27: PenaltyHistory.getTotalPenaltyAmount

class PenaltyHistoryGetTotalPenaltyAmountTest {

    private PenaltyHistory history;

    @Mock
    private Penalty mockPenalty1;

    @Mock
    private Penalty mockPenalty2;

    @Mock
    private Penalty mockPenalty3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        history = new PenaltyHistory();
    }

    @Test
    @DisplayName("getTotalPenaltyAmount returns zero for empty history")
    void testGetTotalPenaltyAmount_EmptyHistory() {
        BigDecimal total = history.getTotalPenaltyAmount();

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    @DisplayName("getTotalPenaltyAmount returns correct sum for single penalty")
    void testGetTotalPenaltyAmount_SinglePenalty() {
        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        history.addPenalty(penalty);

        BigDecimal total = history.getTotalPenaltyAmount();
        assertEquals(new BigDecimal("50.00"), total);
    }

    @Test
    @DisplayName("getTotalPenaltyAmount returns correct sum for multiple penalties")
    void testGetTotalPenaltyAmount_MultiplePenalties() {
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
        Penalty penalty3 = new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("20.00"),
                LocalDateTime.now()
        );

        history.addPenalty(penalty1);
        history.addPenalty(penalty2);
        history.addPenalty(penalty3);

        BigDecimal total = history.getTotalPenaltyAmount();
        assertEquals(new BigDecimal("100.00"), total);
    }



    @Test
    @DisplayName("getTotalPenaltyAmount with mixed zero and non-zero amounts")
    void testGetTotalPenaltyAmount_MixedAmounts() {
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.LOST_TICKET,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
        Penalty penalty3 = new Penalty(
                PenaltyType.MISUSE,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        history.addPenalty(penalty1);
        history.addPenalty(penalty2);
        history.addPenalty(penalty3);

        BigDecimal total = history.getTotalPenaltyAmount();
        assertEquals(new BigDecimal("150.00"), total);
    }

    @Test
    @DisplayName("getTotalPenaltyAmount with decimal amounts")
    void testGetTotalPenaltyAmount_DecimalAmounts() {
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("25.99"),
                LocalDateTime.now()
        );
        Penalty penalty2 = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("15.50"),
                LocalDateTime.now()
        );

        history.addPenalty(penalty1);
        history.addPenalty(penalty2);

        BigDecimal total = history.getTotalPenaltyAmount();
        assertEquals(new BigDecimal("41.49"), total);
    }



    @Test
    @DisplayName("getTotalPenaltyAmount updates after adding new penalty")
    void testGetTotalPenaltyAmount_UpdatesAfterAddition() {
        Penalty penalty1 = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        history.addPenalty(penalty1);
        assertEquals(new BigDecimal("50.00"), history.getTotalPenaltyAmount());

        Penalty penalty2 = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("30.00"),
                LocalDateTime.now()
        );

        history.addPenalty(penalty2);
        assertEquals(new BigDecimal("80.00"), history.getTotalPenaltyAmount());
    }

}
