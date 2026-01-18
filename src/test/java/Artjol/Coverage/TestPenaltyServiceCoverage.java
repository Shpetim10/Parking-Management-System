package Artjol.Coverage;


import Service.PenaltyService;
import Service.impl.PenaltyServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TestPenaltyServiceCoverage {

    private final PenaltyService service = new PenaltyServiceImpl();

    // MC/DC-1: no violations
    @Test
    void testNoViolationsResultsInZeroPenalty() {
        assertEquals(new BigDecimal("0.00"),
                service.calculatePenalty(
                        false, 0, false, false,
                        BigDecimal.TEN,
                        BigDecimal.valueOf(100),
                        BigDecimal.TEN,
                        BigDecimal.TEN
                ));
    }

    // MC/DC-2: overstay only
    @Test
    void testOverstayOnlyAppliesOverstayPenalty() {
        assertEquals(new BigDecimal("20.00"),
                service.calculatePenalty(
                        true, 2, false, false,
                        BigDecimal.TEN,
                        BigDecimal.valueOf(100),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
    }

    // MC/DC-3: overstay capped
    @Test
    void testOverstayIsCappedAtMaximum() {
        assertEquals(new BigDecimal("50.00"),
                service.calculatePenalty(
                        true, 10, false, false,
                        BigDecimal.TEN,
                        BigDecimal.valueOf(50),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
    }

    // MC/DC-4: lost ticket only
    @Test
    void testLostTicketOnlyAddsLostTicketFee() {
        assertEquals(new BigDecimal("30.00"),
                service.calculatePenalty(
                        false, 0, true, false,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(30),
                        BigDecimal.ZERO
                ));
    }

    // MC/DC-5: zone misuse only
    @Test
    void testZoneMisuseOnlyAddsMisuseFee() {
        assertEquals(new BigDecimal("40.00"),
                service.calculatePenalty(
                        false, 0, false, true,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(100),
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(40)
                ));
    }

    // MC/DC-6: invalid negative extra hours
    @Test
    void testNegativeExtraHoursThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.calculatePenalty(
                        true, -1, false, false,
                        BigDecimal.TEN,
                        BigDecimal.valueOf(100),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
    }
}