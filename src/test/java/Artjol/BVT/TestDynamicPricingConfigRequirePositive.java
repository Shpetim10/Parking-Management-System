package Artjol.BVT;

import Model.DynamicPricingConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDynamicPricingConfigRequirePositive {

    // BVT-1: valid minimum boundary (> 0)
    @Test
    void testAcceptsSmallestPositiveValue() {
        assertDoesNotThrow(() ->
                new DynamicPricingConfig(0.0000001, 0.5, 1.0)
        );
    }

    // BVT-2: invalid boundary = 0
    @Test
    void testRejectsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(0.0, 0.5, 1.0)
        );
    }

    // BVT-3: invalid boundary < 0
    @Test
    void testRejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(-0.1, 0.5, 1.0)
        );
    }

    // BVT-4: NaN
    @Test
    void testRejectsNaN() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(Double.NaN, 0.5, 1.0)
        );
    }

    // BVT-5: Infinity
    @Test
    void testRejectsInfinity() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(Double.POSITIVE_INFINITY, 0.5, 1.0)
        );
    }

    // BVT-6: Negative High Occupancy
    @Test
    void testRejectsNegativeHighOccupancyMultiplier() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(1.5, 0.5, -1.0)
        );
    }
}