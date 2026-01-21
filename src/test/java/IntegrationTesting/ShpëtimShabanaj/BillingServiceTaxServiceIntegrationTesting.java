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
import Service.impl.DefaultTaxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pairwise Integration: DefaultBillingService - TaxService")
class BillingServiceTaxServiceIntegrationTesting {

    private DefaultBillingService billingService;

    // REAL: TaxService
    private TaxService taxService;

    // STUBS: Other dependencies
    @Mock
    private DurationCalculator durationCalculator;
    @Mock
    private PricingService pricingService;
    @Mock
    private DiscountAndCapService discountAndCapService;

    private Tariff standardTariff;
    private DynamicPricingConfig dynamicConfig;
    private DiscountInfo noDiscount;

    @BeforeEach
    void setUp() {
        taxService = new DefaultTaxService();

        standardTariff = new Tariff(
                ZoneType.STANDARD,
                BigDecimal.valueOf(5.00),
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        dynamicConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        noDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(4, false));

        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(20.00));

        // Default: just return base price as net (to avoid nulls)
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(20.00));

        billingService = new DefaultBillingService(
                durationCalculator,
                pricingService,
                discountAndCapService,
                taxService
        );
    }

    // IT-01: Standard tax calculation (20%)
    @Test
    @DisplayName("IT-01: Should calculate tax at 20% of net price")
    void testCalculateBillStandardTaxTwentyPercent() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(100.00)); // net price

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getTaxAmount()));
    }

    // IT-02: Tax calculated on net price after discounts
    @Test
    @DisplayName("IT-02: Should calculate tax on net price after discounts")
    void testCalculateBillTaxOnNetPriceAfterDiscounts() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(80.00)); // net price after discounts

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertEquals(0, BigDecimal.valueOf(16.00).compareTo(result.getTaxAmount()));
    }

    // IT-03: Final price = net price + tax
    @Test
    @DisplayName("IT-03: Should calculate final price as net plus tax")
    void testCalculateBillFinalPriceNetPlusTax() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(50.00)); // net price

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll(
                () -> assertEquals(0, BigDecimal.valueOf(50.00).compareTo(result.getNetPrice())),
                () -> assertEquals(0, BigDecimal.valueOf(10.00).compareTo(result.getTaxAmount())),
                () -> assertEquals(0, BigDecimal.valueOf(60.00).compareTo(result.getFinalPrice()))
        );
    }

    // IT-04: Tax on zero net price
    @Test
    @DisplayName("IT-04: Should calculate zero tax for zero net price")
    void testCalculateBillZeroNetPriceZeroTax() {
        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(0, false));
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 9, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll(
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getNetPrice())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getTaxAmount())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getFinalPrice()))
        );
    }

    // IT-05: Tax decimal precision
    @Test
    @DisplayName("IT-05: Should round tax to 2 decimal places")
    void testCalculateBillTaxPrecisionTwoDecimals() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(33.33)); // net price

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertEquals(2, result.getTaxAmount().scale(), "Tax should have 2 decimal places");
    }

    // IT-06: Large net price tax calculation
    @Test
    @DisplayName("IT-06: Should calculate tax correctly for large amounts")
    void testCalculateBillLargeAmountTaxCalculated() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000.00));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll(
                () -> assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(result.getNetPrice())),
                () -> assertEquals(0, BigDecimal.valueOf(200.00).compareTo(result.getTaxAmount())),
                () -> assertEquals(0, BigDecimal.valueOf(1200.00).compareTo(result.getFinalPrice()))
        );
    }

    // IT-07: Tax with penalties included in net
    @Test
    @DisplayName("IT-07: Should calculate tax on net price including penalties")
    void testCalculateBillWithPenaltiesTaxOnTotal() {
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(50.00));
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(80.00)); // e.g. 50 base + 30 penalties

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.valueOf(30.00),
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll(
                () -> assertEquals(0, BigDecimal.valueOf(80.00).compareTo(result.getNetPrice())),
                () -> assertEquals(0, BigDecimal.valueOf(16.00).compareTo(result.getTaxAmount())),
                () -> assertEquals(0, BigDecimal.valueOf(96.00).compareTo(result.getFinalPrice()))
        );
    }

    // IT-08: Tax service used after discounts
    @Test
    @DisplayName("IT-08: Should calculate tax after applying discounts")
    void testCalculateBillExecutionOrderTaxAfterDiscounts() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(75.00));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertEquals(0, BigDecimal.valueOf(15.00).compareTo(result.getTaxAmount()));
    }

    // IT-09: Consistent tax calculation across calls
    @Test
    @DisplayName("IT-09: Should calculate tax consistently for same net price")
    void testCalculateBillConsistencySameNetSameTax() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(60.00));

        BillingResult result1 = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        BillingResult result2 = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 16, 10, 0),
                LocalDateTime.of(2026, 1, 16, 14, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertEquals(result1.getTaxAmount(), result2.getTaxAmount());
        assertEquals(0, BigDecimal.valueOf(12.00).compareTo(result1.getTaxAmount()));
        assertEquals(0, BigDecimal.valueOf(12.00).compareTo(result2.getTaxAmount()));
    }

    // IT-10: Tax calculation with various net prices
    @Test
    @DisplayName("IT-10: Should calculate tax correctly for various net prices")
    void testCalculateBillVariousNetPricesCorrectTaxCalculated() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(25.00));

        BillingResult result1 = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertEquals(0, BigDecimal.valueOf(5.00).compareTo(result1.getTaxAmount()));

        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(13.50));

        BillingResult result2 = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertEquals(0, BigDecimal.valueOf(2.70).compareTo(result2.getTaxAmount()));
    }

    // IT-11: Final price rounding
    @Test
    @DisplayName("IT-11: Should round final price to 2 decimal places")
    void testCalculateBillFinalPriceRoundedCorrectly() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(12.47));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll(
                () -> assertEquals(2, result.getTaxAmount().scale()),
                () -> assertEquals(2, result.getFinalPrice().scale())
        );
    }

    // IT-12: Tax on very small net price
    @Test
    @DisplayName("IT-12: Should handle tax calculation for very small amounts")
    void testCalculateBillSmallAmountTaxCalculated() {
        when(discountAndCapService.applyDiscountAndCaps(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(0.50));

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll(
                () -> assertEquals(0, BigDecimal.valueOf(0.50).compareTo(result.getNetPrice())),
                () -> assertEquals(0, BigDecimal.valueOf(0.10).compareTo(result.getTaxAmount())),
                () -> assertEquals(0, BigDecimal.valueOf(0.60).compareTo(result.getFinalPrice()))
        );
    }
}
