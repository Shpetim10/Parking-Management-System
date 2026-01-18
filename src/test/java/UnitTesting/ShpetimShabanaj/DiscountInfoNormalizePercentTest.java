package UnitTesting.ShpetimShabanaj;

import Model.DiscountInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountInfoNormalizePercentTest {
    private final String FIELD_NAME = "discount";
    // TC-01
    @Test
    @DisplayName("TC-01: Should return BigDecimal.ZERO when value is null")
    void testNormalizePercentWithValueNull() {
        BigDecimal result = DiscountInfo.normalizePercent(null, FIELD_NAME);

        assertAll("Null Check",
                () -> assertNotNull(result),
                () -> assertEquals(BigDecimal.ZERO, result)
        );
    }

    // TC-02, TC-03, TC-06
    @ParameterizedTest
    @ValueSource(strings = {"0", "0.25", "1.0"})
    @DisplayName("TC-02, 03, 06: Should return input value for valid percentages [0.0 - 1.0]")
    void testNormalizePercentWithValidValues(String valueStr) {
        BigDecimal value = new BigDecimal(valueStr);
        BigDecimal result = DiscountInfo.normalizePercent(value, FIELD_NAME);

        assertEquals(value, result, "The value should remain unchanged within valid range.");
    }

    // TC-04
    @ParameterizedTest
    @ValueSource(strings = {"-0.10", "-1.0", "-0.01"})
    @DisplayName("TC-04: Should throw IllegalArgumentException for negative values")
    void testNormalizePercentWithValueNegative(String negativeValueStr) {
        BigDecimal value = new BigDecimal(negativeValueStr);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> DiscountInfo.normalizePercent(value, FIELD_NAME)
        );

        assertEquals(FIELD_NAME + " must not be negative", ex.getMessage());
    }

    // TC-05
    @ParameterizedTest
    @ValueSource(strings = {"1.01", "1.10", "2.0"})
    @DisplayName("TC-05: Should throw IllegalArgumentException for values greater than 1.0")
    void testNormalizePercentWithValueGreaterThanOne(String invalidValueStr) {
        BigDecimal value = new BigDecimal(invalidValueStr);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> DiscountInfo.normalizePercent(value, FIELD_NAME)
        );

        assertEquals(FIELD_NAME + " must be <= 1.0", ex.getMessage());
    }
}
