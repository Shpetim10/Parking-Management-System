package UnitTesting.ShpetimShabanaj;

import Model.DynamicPricingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicPricingConfigConstructorTest {
    // TC-01
    @Test
    @DisplayName("Should create config when all values are valid")
    void testConstructorWithValidValues() {
        double peakMult = 1.5;
        double threshold = 0.8;
        double occMult = 2.0;

        DynamicPricingConfig config = new DynamicPricingConfig(peakMult, threshold, occMult);

        assertAll("Verify all fields are correctly assigned",
                () -> assertEquals(peakMult, config.getPeakHourMultiplier()),
                () -> assertEquals(threshold, config.getHighOccupancyThreshold()),
                () -> assertEquals(occMult, config.getHighOccupancyMultiplier())
        );
    }

    // TC-02
    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -5.5})
    @DisplayName("Should throw exception when peakHourMultiplier is not positive")
    void testPeakHourMultiplierMustBePositive(double invalidValue) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(invalidValue, 0.5, 2.0);
        });
        assertTrue(ex.getMessage().contains("peakHourMultiplier"));
    }

    // TC-03
    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    @DisplayName("Should throw exception when highOccupancyMultiplier is not positive")
    void testHighOccupancyMultiplierMustBePositive(double invalidValue) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(1.5, 0.5, invalidValue);
        });
        assertTrue(ex.getMessage().contains("highOccupancyMultiplier"));
    }

    // TC-04 & TC-05
    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, 0.5})
    @DisplayName("Should accept threshold values exactly at boundaries (0.0 and 1.0)")
    void testThresholdBoundaries(double boundaryValue) {
        assertDoesNotThrow(() -> {
            new DynamicPricingConfig(1.5, boundaryValue, 2.0);
        });
    }

    // TC-06 & TC-07
    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1, 5.0})
    @DisplayName("Should throw exception when threshold is out of [0.0, 1.0] range")
    void testThresholdOutOfRange(double outOfRangeValue) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new DynamicPricingConfig(1.5, outOfRangeValue, 2.0);
        });
        assertEquals("highOccupancyThreshold must be between 0.0 and 1.0 inclusive", ex.getMessage());
    }
}
