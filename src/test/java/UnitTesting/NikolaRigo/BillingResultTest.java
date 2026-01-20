package UnitTesting.NikolaRigo;

import Model.BillingResult;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class BillingResultTest {

    @Test
    void constructor_WithValidParameters_ShouldCreateInstance() {
        // Arrange
        BigDecimal basePrice = new BigDecimal("100.00");
        BigDecimal discountsTotal = new BigDecimal("10.00");
        BigDecimal penaltiesTotal = new BigDecimal("5.00");
        BigDecimal netPrice = new BigDecimal("95.00");
        BigDecimal taxAmount = new BigDecimal("9.50");
        BigDecimal finalPrice = new BigDecimal("104.50");

        // Act
        BillingResult result = new BillingResult(
                basePrice,
                discountsTotal,
                penaltiesTotal,
                netPrice,
                taxAmount,
                finalPrice
        );

        // Assert
        assertNotNull(result);
        assertEquals(basePrice, result.getBasePrice());
        assertEquals(discountsTotal, result.getDiscountsTotal());
        assertEquals(penaltiesTotal, result.getPenaltiesTotal());
        assertEquals(netPrice, result.getNetPrice());
        assertEquals(taxAmount, result.getTaxAmount());
        assertEquals(finalPrice, result.getFinalPrice());
    }

    @Test
    void constructor_WithNullBasePrice_ShouldThrowNullPointerException() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class, () ->
                new BillingResult(
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void constructor_WithNullDiscountsTotal_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new BillingResult(
                        BigDecimal.ZERO,
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void constructor_WithNullPenaltiesTotal_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new BillingResult(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void constructor_WithNullNetPrice_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new BillingResult(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void constructor_WithNullTaxAmount_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new BillingResult(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void constructor_WithNullFinalPrice_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new BillingResult(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null
                )
        );
    }

    @Test
    void constructor_WithZeroValues_ShouldCreateInstance() {
        // Act
        BillingResult result = new BillingResult(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getFinalPrice());
    }

    @Test
    void constructor_WithNegativeValues_ShouldCreateInstance() {
        // Arrange
        BigDecimal negativeValue = new BigDecimal("-50.00");

        // Act
        BillingResult result = new BillingResult(
                negativeValue,
                negativeValue,
                negativeValue,
                negativeValue,
                negativeValue,
                negativeValue
        );

        // Assert
        assertNotNull(result);
        assertEquals(negativeValue, result.getBasePrice());
    }
}
