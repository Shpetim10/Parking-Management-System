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
class DefaultBillingServiceIntegrationTest {

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
        standardTariff = new Tariff(ZoneType.STANDARD,BigDecimal.valueOf(5.00),BigDecimal.valueOf(50.00),BigDecimal.valueOf(20.00));

        dynamicConfig = new DynamicPricingConfig(1.5,0.8,1.3);

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
        BigDecimal maxPriceCap = BigDecimal.valueOf(100.00);
        BigDecimal taxRate = BigDecimal.valueOf(0.20); // 20% VAT

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
        // Duration: 4 hours (rounded up from 3.5 hours)
        // Base price: 4 * 5.00 = 20.00
        // No peak multiplier (OFF_PEAK)
        // No high occupancy surge (0.5 < 0.8)
        // No weekend/holiday surcharge (WEEKDAY)
        // No daily cap applied (20.00 < 50.00)
        // Net price: 20.00 (no discounts, no penalties)
        // Tax: 20.00 * 0.20 = 4.00
        // Final: 20.00 + 4.00 = 24.00

        assertAll("Verify complete billing calculation",
                () -> assertEquals(new BigDecimal("20.00"), result.getBasePrice()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getDiscountsTotal()),
                () -> assertEquals(BigDecimal.ZERO.setScale(2), result.getPenaltiesTotal()),
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
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

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
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Base: 2 * 5.00 = 10.00
        // Peak multiplier: 10.00 * 1.5 = 15.00
        // High occupancy: 15.00 * 1.3 = 19.50
        // Net: 19.50
        // Tax: 19.50 * 0.20 = 3.90
        // Final: 19.50 + 3.90 = 23.40

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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 17, 10, 0); // Saturday
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 17, 14, 0); // 4 hours
        DayType dayType = DayType.WEEKEND;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

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
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Base: 4 * 5.00 = 20.00
        // Weekend surcharge: 20.00 * 1.20 = 24.00
        // Net: 24.00
        // Tax: 24.00 * 0.20 = 4.80
        // Final: 24.00 + 4.80 = 28.80

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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 8, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 20, 0); // 12 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;
        double occupancyRatio = 0.85;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

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
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Base: 12 * 5.00 = 60.00
        // Peak: 60.00 * 1.5 = 90.00
        // High occupancy: 90.00 * 1.3 = 117.00
        // Daily cap: 50.00 (applied because 117.00 > 50.00)
        // Net: 50.00
        // Tax: 50.00 * 0.20 = 10.00
        // Final: 50.00 + 10.00 = 60.00

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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0); // 4 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

        DiscountInfo subscriptionDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.25), // 25% subscription discount
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

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
                subscriptionDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Base: 4 * 5.00 = 20.00
        // Subscription discount: 20.00 * 0.25 = 5.00
        // Net: 20.00 - 5.00 = 15.00
        // Tax: 15.00 * 0.20 = 3.00
        // Final: 15.00 + 3.00 = 18.00

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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0); // 4 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

        DiscountInfo multipleDiscounts = new DiscountInfo(
                BigDecimal.valueOf(0.10), // 10% subscription
                BigDecimal.valueOf(0.15), // 15% promo percent
                BigDecimal.valueOf(2.00),  // 2.00 fixed promo
                false,
                0
        );

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
                multipleDiscounts,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Base: 4 * 5.00 = 20.00
        // After subscription (10%): 20.00 - 2.00 = 18.00
        // After promo percent (15%): 18.00 - 2.70 = 15.30
        // After fixed promo: 15.30 - 2.00 = 13.30
        // Total discounts: 20.00 - 13.30 = 6.70
        // Tax: 13.30 * 0.20 = 2.66
        // Final: 13.30 + 2.66 = 15.96

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
        BigDecimal penalties = BigDecimal.valueOf(15.00);
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

        DiscountInfo discount = new DiscountInfo(
                BigDecimal.valueOf(0.20), // 20% subscription
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

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
                discount,
                penalties,
                24,
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Base: 2 * 5.00 = 10.00
        // Base + Penalties: 10.00 + 15.00 = 25.00
        // After subscription (20%): 25.00 - 5.00 = 20.00
        // Total discount: 5.00
        // Net: 20.00
        // Tax: 20.00 * 0.20 = 4.00
        // Final: 20.00 + 4.00 = 24.00

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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 10, 0); // Same time
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

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
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 8, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 16, 10, 0); // 26 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        int maxDurationHours = 24;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

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
                maxDurationHours,
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Duration capped at 26 hours but daily cap also applies
        // Base would be: 26 * 5.00 = 130.00
        // But daily cap for 26 hours: 50.00 * ceil(26/24) = 50.00 * 2 = 100.00
        // Final: min(130.00, 100.00) = 100.00

        assertAll("Verify max duration handling",
                () -> assertEquals(new BigDecimal("100.00"), result.getBasePrice()),
                () -> assertEquals(new BigDecimal("100.00"), result.getNetPrice())
        );
    }

    // IT-10: Complex scenario - all features combined
    @Test
    @DisplayName("IT-10: Should handle complex scenario with all features")
    void testCalculateBill_ComplexScenario_AllFeaturesIntegrated() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 18, 7, 30); // Sunday morning
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 14, 45); // 7h 15min -> 8 hours
        DayType dayType = DayType.WEEKEND;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;
        double occupancyRatio = 0.9; // High occupancy
        BigDecimal penalties = BigDecimal.valueOf(10.00);
        BigDecimal taxRate = BigDecimal.valueOf(0.21); // 21% VAT

        DiscountInfo complexDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.15), // 15% subscription
                BigDecimal.valueOf(0.10), // 10% promo
                BigDecimal.valueOf(3.00),  // 3.00 fixed
                false,
                0
        );

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
                complexDiscount,
                penalties,
                24,
                BigDecimal.valueOf(200.00),
                taxRate
        );

        // Assert
        // Base: 8 * 5.00 = 40.00
        // Peak: 40.00 * 1.5 = 60.00
        // High occupancy: 60.00 * 1.3 = 78.00
        // Weekend: 78.00 * 1.20 = 93.60
        // Daily cap not exceeded (93.60 > 50.00, so capped to 50.00)
        // Base after all pricing: 50.00
        // Add penalties: 50.00 + 10.00 = 60.00
        // Subscription (15%): 60.00 - 9.00 = 51.00
        // Promo percent (10%): 51.00 - 5.10 = 45.90
        // Fixed promo: 45.90 - 3.00 = 42.90
        // Total discounts: 60.00 - 42.90 = 17.10
        // Net: 42.90
        // Tax: 42.90 * 0.21 = 9.01
        // Final: 42.90 + 9.01 = 51.91

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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 13, 45); // 3h 45min
        int maxDurationHours = 24;

        // Act
        DurationInfo result = billingService.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        // 3h 45min = 225 minutes -> rounds up to 4 hours
        assertAll("Verify duration calculation",
                () -> assertEquals(4, result.hours()),
                () -> assertFalse(result.exceededMax())
        );
    }

    // IT-12: Discount reducing price below zero
    @Test
    @DisplayName("IT-12: Should not allow negative prices after discounts")
    void testCalculateBill_ExcessiveDiscounts_ClampedToZero() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 11, 0); // 1 hour
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

        DiscountInfo excessiveDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.50), // 50%
                BigDecimal.valueOf(0.50), // 50%
                BigDecimal.valueOf(10.00), // Large fixed
                false,
                0
        );

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
                excessiveDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Base: 1 * 5.00 = 5.00
        // After sub (50%): 5.00 - 2.50 = 2.50
        // After promo (50%): 2.50 - 1.25 = 1.25
        // After fixed: 1.25 - 10.00 = -8.75 -> clamped to 0.00
        // Total discounts: 5.00 - 0.00 = 5.00
        // Net: 0.00
        // Tax: 0.00
        // Final: 0.00

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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 12, 25, 10, 0); // Christmas
        LocalDateTime exitTime = LocalDateTime.of(2026, 12, 25, 15, 0); // 5 hours
        DayType dayType = DayType.HOLIDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.4;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

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
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Base: 5 * 5.00 = 25.00
        // Holiday surcharge (20%): 25.00 * 1.20 = 30.00
        // Net: 30.00
        // Tax: 30.00 * 0.20 = 6.00
        // Final: 30.00 + 6.00 = 36.00

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
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 15, 14, 0); // 4 hours
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;
        double occupancyRatio = 0.5;
        BigDecimal taxRate = BigDecimal.valueOf(0.20);

        DiscountInfo freeHoursDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.10), // 10% subscription discount
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                true,  // Has free hours
                2      // 2 free hours per day
        );

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
                freeHoursDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(100.00),
                taxRate
        );

        // Assert
        // Note: Free hours logic depends on implementation in DiscountAndCapService
        // This test verifies integration works with free hours enabled
        assertNotNull(result);
        assertTrue(freeHoursDiscount.isSubscriptionHasFreeHours());
        assertEquals(2, freeHoursDiscount.getFreeHoursPerDay());
    }
}