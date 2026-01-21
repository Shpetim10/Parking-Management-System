package IntegrationTesting.ShpëtimShabanaj;

import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Record.DurationInfo;
import Service.DiscountAndCapService;
import Service.DurationCalculator;
import Service.PricingService;
import Service.TaxService;
import Service.impl.DefaultBillingService;
import Service.impl.DefaultPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: DefaultBillingService - PricingService")
class BillingServicePricingServiceIntegrationTesting {

    private DefaultBillingService billingService;

    // REAL: PricingService
    private PricingService pricingService;

    // STUBS: Other dependencies
    @Mock
    private DurationCalculator durationCalculator;
    @Mock
    private DiscountAndCapService discountAndCapService;
    @Mock
    private TaxService taxService;

    private Tariff standardTariff;
    private Tariff evTariff;
    private Tariff vipTariff;
    private DynamicPricingConfig standardConfig;
    private DynamicPricingConfig aggressiveConfig;
    private DiscountInfo noDiscount;

    @BeforeEach
    void setUp() {
        pricingService = new DefaultPricingService();

        standardTariff = new Tariff(
                ZoneType.STANDARD,
                BigDecimal.valueOf(5.00),
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        evTariff = new Tariff(
                ZoneType.EV,
                BigDecimal.valueOf(3.00),
                BigDecimal.valueOf(30.00),
                BigDecimal.valueOf(15.00)
        );

        vipTariff = new Tariff(
                ZoneType.VIP,
                BigDecimal.valueOf(10.00),
                BigDecimal.valueOf(80.00),
                BigDecimal.valueOf(25.00)
        );

        standardConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);
        aggressiveConfig = new DynamicPricingConfig(2.0, 0.7, 1.5);

        noDiscount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(4, false));

        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(taxService.calculateTax(any(), any()))
                .thenReturn(BigDecimal.ZERO);

        billingService = new DefaultBillingService(
                durationCalculator,
                pricingService,
                discountAndCapService,
                taxService
        );
    }

    // IT-01: Standard weekday off-peak pricing
    @Test
    @DisplayName("IT-01: Should calculate base price for standard weekday off-peak")
    void testCalculateBillStandardWeekdayOffPeakBasePrice() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(4, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getBasePrice()));
    }

    // IT-02: Peak hour multiplier applied
    @Test
    @DisplayName("IT-02: Should apply peak hour multiplier to base price")
    void testCalculateBillPeakHourMultiplierApplied() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(2, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 11, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.5,
                standardTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(15.00).compareTo(result.getBasePrice()));
    }

    // IT-03: High occupancy multiplier applied
    @Test
    @DisplayName("IT-03: Should apply high occupancy multiplier to base price")
    void testCalculateBillHighOccupancyMultiplierApplied() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(3, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 12, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.85,
                standardTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(19.50).compareTo(result.getBasePrice()));
    }

    // IT-04: Peak and high occupancy both applied
    @Test
    @DisplayName("IT-04: Should apply both peak and occupancy multipliers")
    void testCalculateBillPeakAndHighOccupancyBothApplied() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(2, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 11, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.9,
                standardTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(19.50).compareTo(result.getBasePrice()));
    }

    // IT-05: Weekend surcharge applied
    @Test
    @DisplayName("IT-05: Should apply weekend surcharge from tariff")
    void testCalculateBillWeekend_SurchargeApplied() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(4, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 18, 9, 0),
                LocalDateTime.of(2026, 1, 18, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKEND,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(24.00).compareTo(result.getBasePrice()));
    }

    // IT-06: Holiday surcharge applied
    @Test
    @DisplayName("IT-06: Should apply holiday surcharge from tariff")
    void testCalculateBillHolidaySurchargeApplied() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(4, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 12, 25, 9, 0),
                LocalDateTime.of(2026, 12, 25, 13, 0),
                ZoneType.STANDARD,
                DayType.HOLIDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(24.00).compareTo(result.getBasePrice()));
    }

    // IT-07: Different tariff rates (EV zone)
    @Test
    @DisplayName("IT-07: Should use EV zone tariff rate")
    void testCalculateBillEVZoneLowerRateApplied() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(4, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.EV,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                evTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(30.00),
                BigDecimal.valueOf(15.00)
        );

        assertEquals(0, BigDecimal.valueOf(12.00).compareTo(result.getBasePrice()));
    }

    // IT-08: Different tariff rates (VIP zone)
    @Test
    @DisplayName("IT-08: Should use VIP zone tariff rate")
    void testCalculateBillVIPZoneHigherRateApplied() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(3, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 12, 0),
                ZoneType.VIP,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                vipTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(80.00),
                BigDecimal.valueOf(25.00)
        );

        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(result.getBasePrice()));
    }

    // IT-09: Aggressive dynamic pricing config
    @Test
    @DisplayName("IT-09: Should use aggressive peak multiplier from config")
    void testCalculateBillAggressiveConfigHigherMultiplier() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(2, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 11, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.PEAK,
                0.5,
                standardTariff,
                aggressiveConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getBasePrice()));
    }

    // IT-10: Lower occupancy threshold triggers multiplier
    @Test
    @DisplayName("IT-10: Should trigger occupancy multiplier with lower threshold")
    void testCalculateBillLowerThresholdTriggersEarlier() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(2, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 11, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.75,
                standardTariff,
                aggressiveConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(15.00).compareTo(result.getBasePrice()));
    }

    // IT-11: Combined: Weekend + Peak + High Occupancy
    @Test
    @DisplayName("IT-11: Should combine weekend, peak, and occupancy multipliers")
    void testCalculateBillAllMultipliersCombined() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(3, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 18, 9, 0),
                LocalDateTime.of(2026, 1, 18, 12, 0),
                ZoneType.STANDARD,
                DayType.WEEKEND,
                TimeOfDayBand.PEAK,
                0.9,
                standardTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.valueOf(35.10).compareTo(result.getBasePrice()));
    }

    // IT-12: Zero hours yields zero price
    @Test
    @DisplayName("IT-12: Should calculate zero price for zero hours")
    void testCalculateBillZeroHoursZeroPrice() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(0, false));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 9, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                standardConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(result.getBasePrice()));
    }
}
