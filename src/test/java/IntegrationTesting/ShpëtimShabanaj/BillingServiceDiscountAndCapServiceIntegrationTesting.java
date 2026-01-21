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
import Service.impl.DefaultDiscountAndCapService;
import Settings.Settings;
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
@DisplayName("Pairwise Integration: DefaultBillingService -- DiscountAndCapService")
class BillingServiceDiscountAndCapServiceIntegrationTesting {

    private DefaultBillingService billingService;

    // REAL: DiscountAndCapService
    private DiscountAndCapService discountAndCapService;

    // STUBS: Other dependencies
    @Mock
    private DurationCalculator durationCalculator;
    @Mock
    private PricingService pricingService;
    @Mock
    private TaxService taxService;

    private Tariff standardTariff;
    private DynamicPricingConfig dynamicConfig;

    @BeforeEach
    void setUp() {
        discountAndCapService = new DefaultDiscountAndCapService();

        standardTariff = new Tariff(
                ZoneType.STANDARD,
                BigDecimal.valueOf(5.00),
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(20.00)
        );

        dynamicConfig = new DynamicPricingConfig(1.5, 0.8, 1.3);

        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(4, false));

        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(20.00));

        when(taxService.calculateTax(any(), any())).thenAnswer(invocation -> {
            BigDecimal net = invocation.getArgument(0);
            BigDecimal rate = invocation.getArgument(1);
            return net.multiply(rate);
        });

        billingService = new DefaultBillingService(
                durationCalculator,
                pricingService,
                discountAndCapService,
                taxService
        );
    }

    // IT-01: No discounts-price unchanged
    @Test
    @DisplayName("IT-01: Should return price unchanged when no discounts applied")
    void testCalculateBillNoDiscountsPriceUnchanged() {
        DiscountInfo noDiscount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

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

        assertAll("Verify no discount scenario",
                () -> assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getBasePrice())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscountsTotal())),
                () -> assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getNetPrice()))
        );
    }

    // IT-02: Subscription discount only
    @Test
    @DisplayName("IT-02: Should apply subscription discount percentage")
    void testCalculateBillSubscriptionDiscountApplied() {
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(100.00));

        DiscountInfo subscriptionDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.20),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                subscriptionDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll("Verify subscription discount",
                () -> assertEquals(0, BigDecimal.valueOf(100.00).compareTo(result.getBasePrice())),
                () -> assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getDiscountsTotal())),
                () -> assertEquals(0, BigDecimal.valueOf(80.00).compareTo(result.getNetPrice()))
        );
    }

    // IT-03: Promo percentage discount only
    @Test
    @DisplayName("IT-03: Should apply promo percentage discount")
    void testCalculateBillPromoPercentDiscountApplied() {
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(50.00));

        DiscountInfo promoDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.valueOf(0.15),
                BigDecimal.ZERO,
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                promoDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll("Verify promo percentage discount",
                () -> assertEquals(0, BigDecimal.valueOf(50.00).compareTo(result.getBasePrice())),
                () -> assertEquals(0, BigDecimal.valueOf(7.50).compareTo(result.getDiscountsTotal())),
                () -> assertEquals(0, BigDecimal.valueOf(42.50).compareTo(result.getNetPrice()))
        );
    }

    // IT-04: Fixed promo discount only
    @Test
    @DisplayName("IT-04: Should apply fixed promo discount amount")
    void testCalculateBillFixedPromoDiscountApplied() {
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(30.00));

        DiscountInfo fixedDiscount = new DiscountInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(5.00),
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                fixedDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll("Verify fixed promo discount",
                () -> assertEquals(0, BigDecimal.valueOf(30.00).compareTo(result.getBasePrice())),
                () -> assertEquals(0, BigDecimal.valueOf(5.00).compareTo(result.getDiscountsTotal())),
                () -> assertEquals(0, BigDecimal.valueOf(25.00).compareTo(result.getNetPrice()))
        );
    }

    // IT-05: Multiple discounts stacked in correct order
    @Test
    @DisplayName("IT-05: Should apply discounts in order: subscription → promo % → fixed")
    void testCalculateBillMultipleDiscountsAppliedInOrder() {
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(100.00));

        DiscountInfo multiDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.10),
                BigDecimal.valueOf(0.15),
                BigDecimal.valueOf(5.00),
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                multiDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll("Verify discount stacking order",
                () -> assertEquals(0, BigDecimal.valueOf(100.00).compareTo(result.getBasePrice())),
                () -> assertEquals(0, BigDecimal.valueOf(28.50).compareTo(result.getDiscountsTotal())),
                () -> assertEquals(0, BigDecimal.valueOf(71.50).compareTo(result.getNetPrice()))
        );
    }

    // IT-06: Penalties added before discounts
    @Test
    @DisplayName("IT-06: Should add penalties before applying discounts")
    void testCalculateBillPenaltiesBeforeDiscountsCorrectOrder() {
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(50.00));

        DiscountInfo discount = new DiscountInfo(
                BigDecimal.valueOf(0.20),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        BigDecimal penalties = BigDecimal.valueOf(30.00);

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                discount,
                penalties,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll("Verify penalties added before discounts",
                () -> assertEquals(0, BigDecimal.valueOf(50.00).compareTo(result.getBasePrice())),
                () -> assertEquals(0, BigDecimal.valueOf(30.00).compareTo(result.getPenaltiesTotal())),
                () -> assertEquals(0, BigDecimal.valueOf(16.00).compareTo(result.getDiscountsTotal())),
                () -> assertEquals(0, BigDecimal.valueOf(64.00).compareTo(result.getNetPrice()))
        );
    }

    // IT-07: Global cap applied when amount exceeds Settings max capacity
    @Test
    @DisplayName("IT-07: Should apply global max price cap from Settings")
    void testCalculateBillExceedsGlobalCapCapApplied() {
        BigDecimal cap = Settings.getMaxPriceCapacity();
        BigDecimal base = cap.multiply(BigDecimal.valueOf(2));

        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(10, false));
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(base);

        DiscountInfo noDiscount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 19, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                noDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(9999.99),
                BigDecimal.valueOf(0.20)
        );

        BigDecimal expectedNet = cap.setScale(2);
        BigDecimal expectedDiscounts = base.subtract(expectedNet);

        assertAll("Verify global cap applied",
                () -> assertEquals(0, base.compareTo(result.getBasePrice())),
                () -> assertEquals(0, expectedNet.compareTo(result.getNetPrice())),
                () -> assertEquals(0, expectedDiscounts.compareTo(result.getDiscountsTotal()))
        );
    }

    // IT-08: Global cap not applied when below capacity
    @Test
    @DisplayName("IT-08: Should not apply cap when price below global capacity")
    void testCalculateBillBelowGlobalCapNoCapApplied() {
        BigDecimal cap = Settings.getMaxPriceCapacity();
        BigDecimal base = cap.divide(BigDecimal.valueOf(4), 2, BigDecimal.ROUND_HALF_UP);

        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(base);

        DiscountInfo noDiscount = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );

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
                cap,
                BigDecimal.valueOf(0.20)
        );

        assertAll("Verify no cap when below capacity",
                () -> assertEquals(0, base.compareTo(result.getBasePrice())),
                () -> assertEquals(0, base.compareTo(result.getNetPrice())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscountsTotal()))
        );
    }

    // IT-09: Excessive discounts clamped to zero
    @Test
    @DisplayName("IT-09: Should prevent negative price from excessive discounts")
    void testCalculateBillExcessiveDiscountsClampedToZero() {
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(BigDecimal.valueOf(10.00));

        DiscountInfo excessiveDiscount = new DiscountInfo(
                BigDecimal.valueOf(0.50),
                BigDecimal.valueOf(0.50),
                BigDecimal.valueOf(20.00),
                false,
                0
        );

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 10, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                excessiveDiscount,
                BigDecimal.ZERO,
                24,
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.20)
        );

        assertAll("Verify price clamped to zero",
                () -> assertEquals(0, BigDecimal.valueOf(10.00).compareTo(result.getBasePrice())),
                () -> assertEquals(0, BigDecimal.valueOf(10.00).compareTo(result.getDiscountsTotal())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getNetPrice()))
        );
    }

    // IT-10: Penalties with discounts and cap together
    @Test
    @DisplayName("IT-10: Should handle penalties, discounts, and global cap together")
    void testCalculateBillPenaltiesDiscountsCapAllApplied() {
        BigDecimal cap = Settings.getMaxPriceCapacity();
        BigDecimal base = cap.multiply(BigDecimal.valueOf(2)); // above cap

        when(durationCalculator.calculateDuration(any(), any(), anyInt()))
                .thenReturn(new DurationInfo(10, false));
        when(pricingService.calculateBasePrice(anyInt(), any(), any(), anyDouble(), any(), any()))
                .thenReturn(base);

        DiscountInfo discount = new DiscountInfo(
                BigDecimal.valueOf(0.10),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                0
        );

        BigDecimal penalties = BigDecimal.valueOf(20.00);

        BillingResult result = billingService.calculateBill(
                LocalDateTime.of(2026, 1, 15, 9, 0),
                LocalDateTime.of(2026, 1, 15, 19, 0),
                ZoneType.STANDARD,
                DayType.WEEKDAY,
                TimeOfDayBand.OFF_PEAK,
                0.5,
                standardTariff,
                dynamicConfig,
                discount,
                penalties,
                24,
                cap,
                BigDecimal.valueOf(0.20)
        );

        BigDecimal startAmount = base.add(penalties);        // base + penalties
        BigDecimal afterSub = startAmount.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(0.10)));
        BigDecimal expectedNet = afterSub.compareTo(cap) > 0 ? cap.setScale(2) : afterSub.setScale(2);
        BigDecimal expectedDiscounts = startAmount.subtract(expectedNet);

        assertAll("Verify complex calculation with penalties, discounts and cap",
                () -> assertEquals(0, base.compareTo(result.getBasePrice())),
                () -> assertEquals(0, penalties.compareTo(result.getPenaltiesTotal())),
                () -> assertEquals(0, expectedDiscounts.compareTo(result.getDiscountsTotal())),
                () -> assertEquals(0, expectedNet.compareTo(result.getNetPrice()))
        );
    }
}
