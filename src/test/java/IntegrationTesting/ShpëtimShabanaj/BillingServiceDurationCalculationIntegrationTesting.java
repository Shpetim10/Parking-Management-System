package IntegrationTesting.ShpëtimShabanaj;

import Enum.DayType;
import Enum.TimeOfDayBand;
import Enum.ZoneType;
import Model.*;
import Service.DiscountAndCapService;
import Service.DurationCalculator;
import Service.PricingService;
import Service.TaxService;
import Service.impl.DefaultBillingService;
import Service.impl.DefaultDurationCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: DefaultBillingService - DefaultDurationCalculator")
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingServiceDurationCalculationIntegrationTesting {

    private DefaultBillingService billingService;

    // REAL: DurationCalculator
    private DurationCalculator durationCalculator;

    // STUBS: Other dependencies
    @Mock private PricingService pricingService;
    @Mock private DiscountAndCapService discountAndCapService;
    @Mock private TaxService taxService;

    private Tariff standardTariff;
    private DynamicPricingConfig dynamicConfig;
    private DiscountInfo noDiscount;

    @BeforeEach
    void setUp() {
        durationCalculator = new DefaultDurationCalculator();

        standardTariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

        dynamicConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        noDiscount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

        // Default stubs - values aren't important for these duration-focused tests
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(new BigDecimal("20.00"));
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(new BigDecimal("20.00"));
        when(taxService.calculateTax(any(), any()))
                .thenReturn(new BigDecimal("4.00"));

        billingService = new DefaultBillingService(
                durationCalculator,
                pricingService,
                discountAndCapService,
                taxService
        );
    }

    // IT-01: Whole hours - no rounding needed
    @Test
    @DisplayName("IT-01: Should calculate exact whole hours correctly")
    void testCalculateBillWholeHoursExactDuration() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 15, 13, 0); // 4 hours

        billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(4), any(), any(), anyDouble(), any(), any()
        );
    }

    // IT-02: Partial hours - rounds up
    @Test
    @DisplayName("IT-02: Should round up partial hours")
    void testCalculateBillPartialHoursRoundsUp() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 15, 12, 30); // 3.5h -> 4

        billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(4), any(), any(), anyDouble(), any(), any()
        );
    }

    // IT-03: One minute rounds up to one hour
    @Test
    @DisplayName("IT-03: Should round up even 1 minute to 1 hour")
    void testCalculateBillOneMinuteRoundsUpToOneHour() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 15, 9, 1); // 1 min -> 1h

        billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(1), any(), any(), anyDouble(), any(), any()
        );
    }

    // IT-04: 59 minutes rounds up to one hour
    @Test
    @DisplayName("IT-04: Should round up 59 minutes to 1 hour")
    void testCalculateBill59MinutesRoundsUpToOneHour() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 15, 9, 59); // 59 min -> 1h

        billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(1), any(), any(), anyDouble(), any(), any()
        );
    }

    // IT-05: Zero duration (same entry/exit time)
    @Test
    @DisplayName("IT-05: Should handle zero duration correctly")
    void testCalculateBillZeroDurationZeroHours() {
        LocalDateTime sameTime = LocalDateTime.of(2026, 1, 15, 9, 0);

        billingService.calculateBill(
                sameTime, sameTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(0), any(), any(), anyDouble(), any(), any()
        );
    }

    // IT-06: Long duration - multi-day (EXCEEDS max, but does NOT throw in your implementation)
    @Test
    @DisplayName("IT-06: Should calculate multi-day duration hours (even if it exceeds max) without throwing")
    void testCalculateBillMultipleDaysCorrectHoursNoThrow() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 17, 14, 30); // 53.5h -> 54

        billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24, // <= 24 required by DefaultDurationCalculator
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(54), any(), any(), anyDouble(), any(), any()
        );

        // Optional sanity check against the REAL calculator:
        var info = durationCalculator.calculateDuration(entryTime, exitTime, 24);
        assertTrue(info.exceededMax(), "Expected exceededMax=true for 54h vs max 24");
        assertEquals(54, info.hours());
    }

    // IT-07: maxDurationHours <= 0 throws
    @Test
    @DisplayName("IT-07: Should throw when maxDurationHours <= 0")
    void testCalculateBillMaxDurationZeroThrows() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 15, 10, 0);

        var ex = assertThrows(IllegalArgumentException.class, () -> billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 0, // invalid
                new BigDecimal("50.00"), new BigDecimal("0.20")
        ));

        assertEquals("maxDurationHours must be > 0", ex.getMessage());
        verifyNoInteractions(pricingService);
    }

    // IT-08: maxDurationHours > 24 throws
    @Test
    @DisplayName("IT-08: Should throw when maxDurationHours > 24")
    void testCalculateBillMaxDurationOver24Throws() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 15, 10, 0);

        var ex = assertThrows(IllegalArgumentException.class, () -> billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 25, // invalid
                new BigDecimal("50.00"), new BigDecimal("0.20")
        ));

        assertEquals("maxDurationHours must be <=24", ex.getMessage());
        verifyNoInteractions(pricingService);
    }

    // IT-09: Exit before entry time throws
    @Test
    @DisplayName("IT-09: Should throw when exit time is before entry time")
    void testCalculateBillExitBeforeEntryThrows() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 13, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 15, 9, 0);

        var ex = assertThrows(IllegalArgumentException.class, () -> billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        ));

        assertEquals("exitTime must not be before entryTime", ex.getMessage());
        verifyNoInteractions(pricingService);
    }

    // IT-10: Duration at max boundary (exactly max hours)
    @Test
    @DisplayName("IT-10: Should accept duration exactly at max boundary")
    void testCalculateBillExactlyMaxDurationAccepted() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 16, 9, 0); // 24h

        billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(24), any(), any(), anyDouble(), any(), any()
        );
    }

    // IT-11: One minute over max (does NOT throw in your implementation)
    @Test
    @DisplayName("IT-11: Should compute duration even if 1 minute over max (no throw)")
    void testCalculateBillOneMinuteOverMaxNoThrow() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exitTime  = LocalDateTime.of(2026, 1, 16, 9, 1); // 24h 1m -> 25h

        billingService.calculateBill(
                entryTime, exitTime,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(25), any(), any(), anyDouble(), any(), any()
        );

        var info = durationCalculator.calculateDuration(entryTime, exitTime, 24);
        assertTrue(info.exceededMax());
        assertEquals(25, info.hours());
    }

    // IT-12: Multiple durations calculated independently
    @Test
    @DisplayName("IT-12: Should calculate different durations independently")
    void testCalculateBillMultipleCallsIndependentCalculations() {
        // First call: 3 hours
        LocalDateTime entry1 = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime exit1  = LocalDateTime.of(2026, 1, 15, 12, 0);

        billingService.calculateBill(
                entry1, exit1,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(3), any(), any(), anyDouble(), any(), any()
        );

        // Reset and second call: 7 hours
        reset(pricingService);
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(new BigDecimal("35.00"));

        LocalDateTime entry2 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime exit2  = LocalDateTime.of(2026, 1, 15, 17, 0);

        billingService.calculateBill(
                entry2, exit2,
                ZoneType.STANDARD, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK,
                0.5, standardTariff, dynamicConfig, noDiscount,
                BigDecimal.ZERO, 24,
                new BigDecimal("50.00"), new BigDecimal("0.20")
        );

        verify(pricingService, times(1)).calculateBasePrice(
                eq(7), any(), any(), anyDouble(), any(), any()
        );
    }
}
