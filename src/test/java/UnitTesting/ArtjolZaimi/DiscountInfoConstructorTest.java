package UnitTesting.ArtjolZaimi;
import Model.DiscountInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;


 //Unit Tests for M-3: DiscountInfo.constructor

class DiscountInfoConstructorTest {

    @Test
    @DisplayName("Constructor with valid percentage discounts")
    void testConstructor_ValidPercentages() {
        BigDecimal subDiscount = new BigDecimal("0.15");
        BigDecimal promoDiscount = new BigDecimal("0.10");
        BigDecimal promoFixed = new BigDecimal("5.00");

        DiscountInfo info = new DiscountInfo(subDiscount, promoDiscount, promoFixed, true, 2);

        assertEquals(subDiscount, info.getSubscriptionDiscountPercent());
        assertEquals(promoDiscount, info.getPromoDiscountPercent());
        assertEquals(promoFixed, info.getPromoDiscountFixed());
        assertTrue(info.isSubscriptionHasFreeHours());
        assertEquals(2, info.getFreeHoursPerDay());
    }

    @Test
    @DisplayName("Constructor with null values defaults to zero")
    void testConstructor_NullDefaultsToZero() {
        DiscountInfo info = new DiscountInfo(null, null, null, false, 0);

        assertEquals(BigDecimal.ZERO, info.getSubscriptionDiscountPercent());
        assertEquals(BigDecimal.ZERO, info.getPromoDiscountPercent());
        assertEquals(BigDecimal.ZERO, info.getPromoDiscountFixed());
        assertFalse(info.isSubscriptionHasFreeHours());
        assertEquals(0, info.getFreeHoursPerDay());
    }

    @Test
    @DisplayName("Constructor throws exception for negative subscription discount")
    void testConstructor_NegativeSubscriptionDiscount() {
        BigDecimal negativeDiscount = new BigDecimal("-0.10");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DiscountInfo(negativeDiscount, BigDecimal.ZERO, BigDecimal.ZERO, false, 0);
        });

        assertTrue(exception.getMessage().contains("must not be negative"));
    }

    @Test
    @DisplayName("Constructor throws exception for discount > 1.0")
    void testConstructor_DiscountGreaterThanOne() {
        BigDecimal excessiveDiscount = new BigDecimal("1.5");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DiscountInfo(excessiveDiscount, BigDecimal.ZERO, BigDecimal.ZERO, false, 0);
        });

        assertTrue(exception.getMessage().contains("must be <= 1.0"));
    }

    @Test
    @DisplayName("Constructor throws exception for negative free hours")
    void testConstructor_NegativeFreeHours() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, -1);
        });

        assertTrue(exception.getMessage().contains("freeHoursPerDay must not be negative"));
    }

    @Test
    @DisplayName("Constructor throws exception for free hours without flag")
    void testConstructor_FreeHoursWithoutFlag() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 5);
        });

        assertTrue(exception.getMessage().contains("freeHoursPerDay > 0 is not allowed"));
    }

    @Test
    @DisplayName("Constructor with boundary value discount = 1.0")
    void testConstructor_BoundaryDiscount() {
        BigDecimal maxDiscount = BigDecimal.ONE;

        DiscountInfo info = new DiscountInfo(maxDiscount, maxDiscount, BigDecimal.ZERO, false, 0);

        assertEquals(maxDiscount, info.getSubscriptionDiscountPercent());
        assertEquals(maxDiscount, info.getPromoDiscountPercent());
    }

    @Test
    @DisplayName("Constructor throws exception for negative fixed discount")
    void testConstructor_NegativeFixedDiscount() {
        BigDecimal negativeFixed = new BigDecimal("-10.00");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, negativeFixed, false, 0);
        });

        assertTrue(exception.getMessage().contains("must not be negative"));
    }


    @Test
    @DisplayName("Constructor with maximum valid values")
    void testConstructor_MaximumValidValues() {
        BigDecimal maxDiscount = BigDecimal.ONE;
        BigDecimal largeFixed = new BigDecimal("1000.00");

        DiscountInfo info = new DiscountInfo(maxDiscount, maxDiscount, largeFixed, true, 24);

        assertEquals(maxDiscount, info.getSubscriptionDiscountPercent());
        assertEquals(maxDiscount, info.getPromoDiscountPercent());
        assertEquals(largeFixed, info.getPromoDiscountFixed());
        assertTrue(info.isSubscriptionHasFreeHours());
        assertEquals(24, info.getFreeHoursPerDay());
    }
}