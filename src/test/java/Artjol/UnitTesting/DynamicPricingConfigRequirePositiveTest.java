package Artjol.UnitTesting;

import Model.DynamicPricingConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

// Unit Tests for M-7: DynamicPricingConfig.requirePositive

class DynamicPricingConfigRequirePositiveTest {

    @Test
    @DisplayName("requirePositive with valid positive value")
    void testRequirePositive_ValidPositive() {
        DynamicPricingConfig config = new DynamicPricingConfig(1.5, 0.8, 1.3);

        assertEquals(1.5, config.getPeakHourMultiplier());
        assertEquals(0.8, config.getHighOccupancyThreshold());
        assertEquals(1.3, config.getHighOccupancyMultiplier());
    }

    @Test
    @DisplayName("requirePositive throws exception for zero value")
    void testRequirePositive_ZeroValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(0.0, 0.5, 1.2);
        });

        assertTrue(exception.getMessage().contains("must be a finite value > 0.0"));
    }

    @Test
    @DisplayName("requirePositive throws exception for negative value")
    void testRequirePositive_NegativeValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(-1.5, 0.5, 1.2);
        });

        assertTrue(exception.getMessage().contains("must be a finite value > 0.0"));
    }

    @Test
    @DisplayName("requirePositive throws exception for NaN")
    void testRequirePositive_NaN() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(Double.NaN, 0.5, 1.2);
        });

        assertTrue(exception.getMessage().contains("must be a finite value > 0.0"));
    }

    @Test
    @DisplayName("requirePositive throws exception for positive infinity")
    void testRequirePositive_PositiveInfinity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(Double.POSITIVE_INFINITY, 0.5, 1.2);
        });

        assertTrue(exception.getMessage().contains("must be a finite value > 0.0"));
    }

    @Test
    @DisplayName("requirePositive throws exception for negative infinity")
    void testRequirePositive_NegativeInfinity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(Double.NEGATIVE_INFINITY, 0.5, 1.2);
        });

        assertTrue(exception.getMessage().contains("must be a finite value > 0.0"));
    }

    @Test
    @DisplayName("requirePositive with very small positive value")
    void testRequirePositive_VerySmallPositive() {
        DynamicPricingConfig config = new DynamicPricingConfig(0.0001, 0.5, 0.0001);

        assertEquals(0.0001, config.getPeakHourMultiplier());
        assertEquals(0.0001, config.getHighOccupancyMultiplier());
    }

    @Test
    @DisplayName("requirePositive with very large positive value")
    void testRequirePositive_VeryLargePositive() {
        DynamicPricingConfig config = new DynamicPricingConfig(1000.0, 0.5, 2000.0);

        assertEquals(1000.0, config.getPeakHourMultiplier());
        assertEquals(2000.0, config.getHighOccupancyMultiplier());
    }

    @Test
    @DisplayName("requirePositive for occupancyMultiplier with zero")
    void testRequirePositive_OccupancyMultiplierZero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(1.5, 0.5, 0.0);
        });

        assertTrue(exception.getMessage().contains("must be a finite value > 0.0"));
    }

    @Test
    @DisplayName("requirePositive for both multipliers positive")
    void testRequirePositive_BothMultipliersPositive() {
        DynamicPricingConfig config = new DynamicPricingConfig(2.0, 0.75, 1.8);

        assertEquals(2.0, config.getPeakHourMultiplier());
        assertEquals(0.75, config.getHighOccupancyThreshold());
        assertEquals(1.8, config.getHighOccupancyMultiplier());
    }
}