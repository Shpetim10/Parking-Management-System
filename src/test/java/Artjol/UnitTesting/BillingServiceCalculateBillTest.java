package Artjol.UnitTesting;

import Model.*;
import Enum.*;
import Service.*;
import Service.impl.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Unit Tests for M-77: BillingService.calculateBill

class BillingServiceCalculateBillTest {

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new DefaultBillingService(
                new DefaultDurationCalculator(),
                new DefaultPricingService(),
                new DefaultDiscountAndCapService(),
                new DefaultTaxService()
        );
    }

    @Test
    @DisplayName("calculates bill with zero duration")
    void testCalculateBill_ZeroDuration() {
        LocalDateTime time = LocalDateTime.now();
        Tariff tariff = new Tariff(ZoneType.STANDARD, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.ZERO);
        DynamicPricingConfig config = new DynamicPricingConfig(1.5, 0.8, 1.3);
        DiscountInfo discount = new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0);

        BillingResult result = billingService.calculateBill(
                time, time, ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, tariff, config, discount, BigDecimal.ZERO,
                24, BigDecimal.valueOf(200), BigDecimal.valueOf(0.2)
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result.getBasePrice());
    }

    @Test
    @DisplayName("calculates bill with penalties")
    void testCalculateBill_WithPenalties() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.plusHours(2);

        Tariff tariff = new Tariff(
                ZoneType.STANDARD,
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                BigDecimal.ZERO
        );

        DynamicPricingConfig config = new DynamicPricingConfig(1.0, 0.9, 1.0);
        DiscountInfo discount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

        BillingResult result = billingService.calculateBill(
                entry, exit,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5,
                tariff,
                config,
                discount,
                BigDecimal.valueOf(50),   // penalties
                24,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(0.2)
        );

        assertNotNull(result);
        assertEquals(
                0,
                result.getPenaltiesTotal().compareTo(BigDecimal.valueOf(50))
        );
    }
    @Test
    @DisplayName("calculates bill for regular weekday")
    void testCalculateBill_RegularWeekday() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.plusHours(3);
        Tariff tariff = new Tariff(ZoneType.STANDARD, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.ZERO);
        DynamicPricingConfig config = new DynamicPricingConfig(1.0, 0.9, 1.0);
        DiscountInfo discount = new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0);

        BillingResult result = billingService.calculateBill(
                entry, exit, ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, tariff, config, discount, BigDecimal.ZERO,
                24, BigDecimal.valueOf(200), BigDecimal.valueOf(0.2)
        );

        assertNotNull(result);
        assertTrue(result.getBasePrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("result contains all required fields")
    void testCalculateBill_ResultFields() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.plusHours(1);
        Tariff tariff = new Tariff(ZoneType.STANDARD, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.ZERO);
        DynamicPricingConfig config = new DynamicPricingConfig(1.0, 0.9, 1.0);
        DiscountInfo discount = new DiscountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0);

        BillingResult result = billingService.calculateBill(
                entry, exit, ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, tariff, config, discount, BigDecimal.ZERO,
                24, BigDecimal.valueOf(200), BigDecimal.valueOf(0.2)
        );

        assertNotNull(result.getBasePrice());
        assertNotNull(result.getDiscountsTotal());
        assertNotNull(result.getPenaltiesTotal());
        assertNotNull(result.getNetPrice());
        assertNotNull(result.getTaxAmount());
        assertNotNull(result.getFinalPrice());
    }
}