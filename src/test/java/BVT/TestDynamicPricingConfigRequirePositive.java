package BVT;

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

    // BVT-2: valid typical positive value
    @Test
    void testAcceptsTypicalPositiveValue() {
        assertDoesNotThrow(() ->
                new DynamicPricingConfig(1.5, 0.5, 1.0)
        );
    }

    // BVT-3: valid large positive value
    @Test
    void testAcceptsLargePositiveValue() {
        assertDoesNotThrow(() ->
                new DynamicPricingConfig(100.0, 0.5, 1.0)
        );
    }

    // BVT-4: invalid boundary = 0
    @Test
    void testRejectsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(0.0, 0.5, 1.0)
        );
    }

    // BVT-5: invalid boundary < 0
    @Test
    void testRejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(-0.1, 0.5, 1.0)
        );
    }

    // BVT-6: invalid special value (NaN)
    @Test
    void testRejectsNaN() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(Double.NaN, 0.5, 1.0)
        );
    }

    // BVT-7: invalid special value (positive infinity)
    @Test
    void testRejectsInfinity() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(Double.POSITIVE_INFINITY, 0.5, 1.0)
        );
    }

    // BVT-8: invalid special value (negative infinity)
    @Test
    void testRejectsNegativeInfinity() {
        assertThrows(IllegalArgumentException.class, () ->
                new DynamicPricingConfig(Double.NEGATIVE_INFINITY, 0.5, 1.0)
        );
    }
}