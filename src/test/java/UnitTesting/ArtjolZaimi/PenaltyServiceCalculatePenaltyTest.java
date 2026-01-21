package UnitTesting.ArtjolZaimi;

import Service.PenaltyService;
import Service.impl.PenaltyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

// Unit Tests for M-97: PenaltyService.calculatePenalty

class PenaltyServiceCalculatePenaltyTest {

    private PenaltyService service;

    @BeforeEach
    void setUp() {
        service = new PenaltyServiceImpl();
    }

    @Test
    @DisplayName("calculates overstay penalty")
    void testCalculatePenalty_Overstay() {
        BigDecimal result = service.calculatePenalty(
                true, 2.0, false, false,
                BigDecimal.TEN, BigDecimal.valueOf(100),
                BigDecimal.valueOf(50), BigDecimal.valueOf(75)
        );

        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    @DisplayName("calculates lost ticket penalty")
    void testCalculatePenalty_LostTicket() {
        BigDecimal result = service.calculatePenalty(
                false, 0.0, true, false,
                BigDecimal.TEN, BigDecimal.valueOf(100),
                BigDecimal.valueOf(50), BigDecimal.valueOf(75)
        );

        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    @DisplayName("calculates zone misuse penalty")
    void testCalculatePenalty_ZoneMisuse() {
        BigDecimal result = service.calculatePenalty(
                false, 0.0, false, true,
                BigDecimal.TEN, BigDecimal.valueOf(100),
                BigDecimal.valueOf(50), BigDecimal.valueOf(75)
        );

        assertEquals(new BigDecimal("75.00"), result);
    }

    @Test
    @DisplayName("throws exception for null rates")
    void testCalculatePenalty_NullRates() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.calculatePenalty(
                    true, 2.0, false, false,
                    null, BigDecimal.valueOf(100),
                    BigDecimal.valueOf(50), BigDecimal.valueOf(75)
            );
        });
    }
}