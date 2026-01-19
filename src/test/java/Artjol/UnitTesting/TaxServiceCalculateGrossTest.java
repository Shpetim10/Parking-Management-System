package Artjol.UnitTesting;

import Service.TaxService;
import Service.impl.DefaultTaxService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

// Unit Tests for M-90: TaxService.calculateGross

class TaxServiceCalculateGrossTest {

    private TaxService taxService;

    @BeforeEach
    void setUp() {
        taxService = new DefaultTaxService();
    }

    @Test
    @DisplayName("calculates gross with zero tax rate")
    void testCalculateGross_ZeroTaxRate() {
        BigDecimal net = new BigDecimal("100.00");
        BigDecimal taxRate = BigDecimal.ZERO;

        BigDecimal gross = taxService.calculateGross(net, taxRate);

        assertEquals(new BigDecimal("100.00"), gross);
    }

    @Test
    @DisplayName("calculates gross with 20% tax rate")
    void testCalculateGross_TwentyPercentTax() {
        BigDecimal net = new BigDecimal("100.00");
        BigDecimal taxRate = new BigDecimal("0.20");

        BigDecimal gross = taxService.calculateGross(net, taxRate);

        assertEquals(new BigDecimal("120.00"), gross);
    }

    @Test
    @DisplayName("calculates gross with 10% tax rate")
    void testCalculateGross_TenPercentTax() {
        BigDecimal net = new BigDecimal("50.00");
        BigDecimal taxRate = new BigDecimal("0.10");

        BigDecimal gross = taxService.calculateGross(net, taxRate);

        assertEquals(new BigDecimal("55.00"), gross);
    }

    @Test
    @DisplayName("calculates gross with zero net amount")
    void testCalculateGross_ZeroNetAmount() {
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal taxRate = new BigDecimal("0.15");

        BigDecimal gross = taxService.calculateGross(net, taxRate);

        assertEquals(new BigDecimal("0.00"), gross);
    }

    @Test
    @DisplayName("throws exception for negative net amount")
    void testCalculateGross_NegativeNetAmount() {
        BigDecimal net = new BigDecimal("-50.00");
        BigDecimal taxRate = new BigDecimal("0.20");

        assertThrows(IllegalArgumentException.class, () -> {
            taxService.calculateGross(net, taxRate);
        });
    }

    @Test
    @DisplayName("throws exception for negative tax rate")
    void testCalculateGross_NegativeTaxRate() {
        BigDecimal net = new BigDecimal("100.00");
        BigDecimal taxRate = new BigDecimal("-0.10");

        assertThrows(IllegalArgumentException.class, () -> {
            taxService.calculateGross(net, taxRate);
        });
    }

    @Test
    @DisplayName("throws exception for tax rate greater than 1")
    void testCalculateGross_TaxRateGreaterThanOne() {
        BigDecimal net = new BigDecimal("100.00");
        BigDecimal taxRate = new BigDecimal("1.5");

        assertThrows(IllegalArgumentException.class, () -> {
            taxService.calculateGross(net, taxRate);
        });
    }

    @Test
    @DisplayName("throws exception for null net amount")
    void testCalculateGross_NullNetAmount() {
        assertThrows(NullPointerException.class, () -> {
            taxService.calculateGross(null, new BigDecimal("0.20"));
        });
    }

    @Test
    @DisplayName("throws exception for null tax rate")
    void testCalculateGross_NullTaxRate() {
        assertThrows(NullPointerException.class, () -> {
            taxService.calculateGross(new BigDecimal("100.00"), null);
        });
    }

}