package UnitTesting.NikolaRigo;

import Model.DiscountInfo;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class normalizeNonNegativeAmountTest {

    @Test
    void normalizeNonNegativeAmount_WithPositiveValue_ShouldReturnSameValue() {
        // Arrange
        BigDecimal value = new BigDecimal("100.50");
        String fieldName = "testField";

        // Act
        BigDecimal result = DiscountInfo.normalizeNonNegativeAmount(value, fieldName);

        // Assert
        assertEquals(value, result);
    }

    @Test
    void normalizeNonNegativeAmount_WithZero_ShouldReturnZero() {
        // Arrange
        BigDecimal value = BigDecimal.ZERO;
        String fieldName = "testField";

        // Act
        BigDecimal result = DiscountInfo.normalizeNonNegativeAmount(value, fieldName);

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void normalizeNonNegativeAmount_WithNullValue_ShouldReturnZero() {
        // Arrange
        String fieldName = "testField";

        // Act
        BigDecimal result = DiscountInfo.normalizeNonNegativeAmount(null, fieldName);

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void normalizeNonNegativeAmount_WithNegativeValue_ShouldThrowIllegalArgumentException() {
        // Arrange
        BigDecimal value = new BigDecimal("-50.00");
        String fieldName = "amount";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DiscountInfo.normalizeNonNegativeAmount(value, fieldName)
        );

        assertEquals("amount must not be negative", exception.getMessage());
    }

    @Test
    void normalizeNonNegativeAmount_WithNegativeValueAndDifferentFieldName_ShouldIncludeFieldNameInMessage() {
        // Arrange
        BigDecimal value = new BigDecimal("-1");
        String fieldName = "discount";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DiscountInfo.normalizeNonNegativeAmount(value, fieldName)
        );

        assertEquals("discount must not be negative", exception.getMessage());
    }

    @Test
    void normalizeNonNegativeAmount_WithVerySmallPositiveValue_ShouldReturnValue() {
        // Arrange
        BigDecimal value = new BigDecimal("0.01");
        String fieldName = "testField";

        // Act
        BigDecimal result = DiscountInfo.normalizeNonNegativeAmount(value, fieldName);

        // Assert
        assertEquals(value, result);
    }

    @Test
    void normalizeNonNegativeAmount_WithLargeValue_ShouldReturnValue() {
        // Arrange
        BigDecimal value = new BigDecimal("999999999.99");
        String fieldName = "testField";

        // Act
        BigDecimal result = DiscountInfo.normalizeNonNegativeAmount(value, fieldName);

        // Assert
        assertEquals(value, result);
    }

    @Test
    void normalizeNonNegativeAmount_WithVerySmallNegativeValue_ShouldThrowException() {
        // Arrange
        BigDecimal value = new BigDecimal("-0.01");
        String fieldName = "testField";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> DiscountInfo.normalizeNonNegativeAmount(value, fieldName)
        );
    }

    @Test
    void normalizeNonNegativeAmount_WithMinusZero_ShouldReturnSameInstance() {
        // Arrange
        BigDecimal value = new BigDecimal("-0.00");
        String fieldName = "testField";

        // Act
        BigDecimal result = DiscountInfo.normalizeNonNegativeAmount(value, fieldName);

        // Assert
        assertSame(value, result);  // Same object reference
    }

    @Test
    void normalizeNonNegativeAmount_WithDecimalPrecision_ShouldPreserveValue() {
        // Arrange
        BigDecimal value = new BigDecimal("123.456789");
        String fieldName = "testField";

        // Act
        BigDecimal result = DiscountInfo.normalizeNonNegativeAmount(value, fieldName);

        // Assert
        assertEquals(value, result);
        assertEquals("123.456789", result.toPlainString());
    }
}
