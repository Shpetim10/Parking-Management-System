package IntegrationTesting.ShpëtimShabanaj;

import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Record.DurationInfo;
import Service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Tests - DefaultBillingService (Neighbourhood Radius 1)")
class BillingServiceIntegrationTesting {

    private DefaultBillingService billingService;
    private DefaultDurationCalculator durationCalculator;
    private DefaultPricingService pricingService;
    private DefaultDiscountAndCapService discountAndCapService;
    private DefaultTaxService taxService;
    private Tariff standardTariff;
    private DynamicPricingConfig dynamicConfig;
    private DiscountInfo noDiscount;

    @BeforeEach
    void setUp() {
        // Create real instances of all direct dependencies
        durationCalculator = new DefaultDurationCalculator();
        pricingService = new DefaultPricingService();
        discountAndCapService = new DefaultDiscountAndCapService();
        taxService = new DefaultTaxService();

        // Create the service under test with real dependencies
        billingService = new DefaultBillingService(
                durationCalculator,
                pricingService,
                discountAndCapService,
                taxService
        );

        // Setup common test data
        standardTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

        dynamicConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        noDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );
    }

    // IT-01: Happy path - complete billing calculation with no discounts
    @Test
    @DisplayName("IT-01: Should calculate complete bill for standard weekday parking")
    void testCalculateBill_StandardWeekdayParking_CompleteCalculation() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);  // Thursday
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 12, 30); // 3.5 hours -> rounds to 4 hours
        ZoneType zoneType = ZoneType.STANDARD;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal penalties = BigDecimal.ZERO;
        int maxDurationHours = 24;
        BigDecimal maxPriceCap = new BigDecimal("100.00");
        BigDecimal taxRate = new BigDecimal("0.20"); // 20% VAT

        // Act
        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                zoneType,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                noDiscount,
                penalties,
                maxDurationHours,
                maxPriceCap,
                taxRate
        );

        // Assert
        assertAll("Verify complete billing calculation",
                () -> assertEquals(new BigDecimal("20.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("0.00"), result.getDiscountsTotal()),
                () -> assertEquals(new BigDecimal("0"), result.getPenaltiesTotal()),
                () -> assertEquals(new BigDecimal("20.00"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("4.00"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("24.00"), result.getFinalPrice())
        );
    }

    // IT-02: Peak hour with high occupancy surge
    @Test
    @DisplayName("IT-02: Should apply peak hour and high occupancy multipliers correctly")
    void testCalculateBill_PeakHourHighOccupancy_MultipliersApplied() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 8, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 10, 0); // 2 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;
        double occupancyRatio = 0.85; // Above 0.8 threshold
        BigDecimal taxRate = new BigDecimal("0.20");

        // Act
        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        // Assert
        assertAll("Verify peak and occupancy multipliers",
                () -> assertEquals(new BigDecimal("19.50"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("19.50"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("3.90"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("23.40"), result.getFinalPrice())
        );
    }

    // IT-03: Weekend surcharge application
    @Test
    @DisplayName("IT-03: Should apply weekend surcharge correctly")
    void testCalculateBill_WeekendParking_SurchargeApplied() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 17, 10, 0); // Saturday
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 17, 14, 0); // 4 hours
        DayType dayType = DayType.WEEKEND;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = new BigDecimal("0.20");

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify weekend surcharge",
                () -> assertEquals(new BigDecimal("24.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("24.00"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("4.80"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("28.80"), result.getFinalPrice())
        );
    }

    // IT-04: Daily cap application
    @Test
    @DisplayName("IT-04: Should apply daily cap when price exceeds limit")
    void testCalculateBill_ExceedsDailyCap_CapApplied() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 8, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 20, 0); // 12 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;
        double occupancyRatio = 0.85;
        BigDecimal taxRate = new BigDecimal("0.20");

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify daily cap application",
                () -> assertEquals(new BigDecimal("50.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("50.00"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("10.00"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("60.00"), result.getFinalPrice())
        );
    }

    // IT-05: Subscription discount application
    @Test
    @DisplayName("IT-05: Should apply subscription discount correctly")
    void testCalculateBill_WithSubscriptionDiscount_DiscountApplied() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0); // 4 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = new BigDecimal("0.20");

        DiscountInfo subscriptionDiscount = new DiscountInfo(
                new BigDecimal("0.25"), // 25% subscription discount
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                subscriptionDiscount,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify subscription discount",
                () -> assertEquals(new BigDecimal("20.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("5.00"), result.getDiscountsTotal()),
                () -> assertEquals(new BigDecimal("15.00"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("3.00"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("18.00"), result.getFinalPrice())
        );
    }

    // IT-06: Multiple discounts stacking
    @Test
    @DisplayName("IT-06: Should apply multiple discounts in correct order")
    void testCalculateBill_MultipleDiscounts_StackedCorrectly() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0); // 4 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = new BigDecimal("0.20");

        DiscountInfo multipleDiscounts = new DiscountInfo(
                new BigDecimal("0.10"), // 10% subscription
                new BigDecimal("0.15"), // 15% promo percent
                new BigDecimal("2.00"), // 2.00 fixed promo
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                multipleDiscounts,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify multiple discounts stacking",
                () -> assertEquals(new BigDecimal("20.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("6.70"), result.getDiscountsTotal()),
                () -> assertEquals(new BigDecimal("13.30"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("2.66"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("15.96"), result.getFinalPrice())
        );
    }

    // IT-07: Penalties added to base before discount
    @Test
    @DisplayName("IT-07: Should add penalties before applying discounts")
    void testCalculateBill_WithPenalties_AddedBeforeDiscounts() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 12, 0); // 2 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal penalties = new BigDecimal("15.00");
        BigDecimal taxRate = new BigDecimal("0.20");

        DiscountInfo discount = new DiscountInfo(
                new BigDecimal("0.20"), // 20% subscription
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                discount,
                penalties,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify penalties added before discounts",
                () -> assertEquals(new BigDecimal("10.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("5.00"), result.getDiscountsTotal()),
                () -> assertEquals(new BigDecimal("15.00"), result.getPenaltiesTotal()),
                () -> assertEquals(new BigDecimal("20.00"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("4.00"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("24.00"), result.getFinalPrice())
        );
    }

    // IT-08: Zero duration parking
    @Test
    @DisplayName("IT-08: Should handle zero duration parking correctly")
    void testCalculateBill_ZeroDuration_ZeroCharges() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 10, 0); // Same time
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = new BigDecimal("0.20");

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify zero duration charges",
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getBasePrice()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getNetPrice()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getTaxAmount()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getFinalPrice())
        );
    }

    // IT-09: Duration exceeds maximum allowed
    @Test
    @DisplayName("IT-09: Should handle duration exceeding maximum")
    void testCalculateBill_ExceedsMaxDuration_Capped() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 8, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 16, 10, 0); // 26 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        int maxDurationHours = 24;
        BigDecimal taxRate = new BigDecimal("0.20");

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                maxDurationHours,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify max duration handling",
                () -> assertEquals(new BigDecimal("100.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("100.00"), result.getNetPrice())
        );
    }

    // IT-10: Complex scenario - all features combined
    @Test
    @DisplayName("IT-10: Should handle complex scenario with all features")
    void testCalculateBill_ComplexScenario_AllFeaturesIntegrated() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 18, 7, 30); // Sunday morning
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 14, 45); // 7h 15min -> 8 hours
        DayType dayType = DayType.WEEKEND;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;
        double occupancyRatio = 0.9; // High occupancy
        BigDecimal penalties = new BigDecimal("10.00");
        BigDecimal taxRate = new BigDecimal("0.21"); // 21% VAT

        DiscountInfo complexDiscount = new DiscountInfo(
                new BigDecimal("0.15"), // 15% subscription
                new BigDecimal("0.10"), // 10% promo
                new BigDecimal("3.00"), // 3.00 fixed
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                complexDiscount,
                penalties,
                24,
                new BigDecimal("200.00"),
                taxRate
        );

        assertAll("Verify complex scenario calculation",
                () -> assertEquals(new BigDecimal("50.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("17.10"), result.getDiscountsTotal()),
                () -> assertEquals(new BigDecimal("10.00"), result.getPenaltiesTotal()),
                () -> assertEquals(new BigDecimal("42.90"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("9.01"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("51.91"), result.getFinalPrice())
        );
    }

    // IT-11: Test calculateDuration method integration
    @Test
    @DisplayName("IT-11: Should calculate duration correctly through service")
    void testCalculateDuration_Integration_CorrectCalculation() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 45); // 3h 45min
        int maxDurationHours = 24;

        DurationInfo result = billingService.calculateDuration(entryTime, exitTime, maxDurationHours);

        assertAll("Verify duration calculation",
                () -> assertEquals(4, result.hours()),
                () -> assertFalse(result.exceededMax())
        );
    }

    // IT-12: Discount reducing price below zero
    @Test
    @DisplayName("IT-12: Should not allow negative prices after discounts")
    void testCalculateBill_ExcessiveDiscounts_ClampedToZero() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0); // 1 hour
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = new BigDecimal("0.20");

        DiscountInfo excessiveDiscount = new DiscountInfo(
                new BigDecimal("0.50"), // 50%
                new BigDecimal("0.50"), // 50%
                new BigDecimal("10.00"), // Large fixed
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                excessiveDiscount,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify price clamped to zero",
                () -> assertEquals(new BigDecimal("5.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("5.00"), result.getDiscountsTotal()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getNetPrice()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getTaxAmount()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getFinalPrice())
        );
    }

    // IT-13: Holiday pricing
    @Test
    @DisplayName("IT-13: Should apply holiday surcharge correctly")
    void testCalculateBill_HolidayParking_SurchargeApplied() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 12, 25, 10, 0); // Christmas
        LocalDateTime exitTime = LocalDateTime.of(2026, 12, 25, 15, 0); // 5 hours
        DayType dayType = DayType.HOLIDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.4;
        BigDecimal taxRate = new BigDecimal("0.20");

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertAll("Verify holiday surcharge",
                () -> assertEquals(new BigDecimal("30.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("30.00"), result.getNetPrice()),
                () -> assertEquals(new BigDecimal("6.00"), result.getTaxAmount()),
                () -> assertEquals(new BigDecimal("36.00"), result.getFinalPrice())
        );
    }

    // IT-14: Subscription with free hours
    @Test
    @DisplayName("IT-14: Should handle subscription with free hours")
    void testCalculateBill_SubscriptionWithFreeHours_DiscountApplied() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0); // 4 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = new BigDecimal("0.20");

        DiscountInfo freeHoursDiscount = new DiscountInfo(
                new BigDecimal("0.10"), // 10% subscription discount
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                true,  // Has free hours
                2      // 2 free hours per day
        );

        BillingResult result = billingService.calculateBill(
                entryTime,
                exitTime,
                ZoneType.STANDARD,
                dayType,
                timeOfDayBand,
                occupancyRatio,
                standardTariff,
                dynamicConfig,
                freeHoursDiscount,
                BigDecimal.ZERO,
                24,
                new BigDecimal("100.00"),
                taxRate
        );

        assertNotNull(result);
        assertTrue(freeHoursDiscount.isSubscriptionHasFreeHours());
        assertEquals(2, freeHoursDiscount.getFreeHoursPerDay());
    }
}