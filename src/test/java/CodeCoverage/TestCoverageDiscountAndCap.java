package CodeCoverage;

import Model.DiscountInfo;
import Service.impl.DefaultDiscountAndCapService;
import Service.impl.DefaultDurationCalculator;
import Settings.Settings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TestCoverageDiscountAndCap {
    static DefaultDiscountAndCapService defaultDiscountAndCapService;
    static DiscountInfo discountInfoWithNoDiscounts;
    @BeforeAll
     static void setUpClass() {
        defaultDiscountAndCapService=new DefaultDiscountAndCapService();
        discountInfoWithNoDiscounts=new DiscountInfo(
                new BigDecimal(0),
                new BigDecimal(0),
                new BigDecimal(0),
                false,
                0);
    }
    //TC-1
    @Test
    void testInvalidBasePrice(){
        assertThrows(IllegalArgumentException.class, () -> {
            defaultDiscountAndCapService.applyDiscountAndCaps(
                    new BigDecimal("-1"),
                    discountInfoWithNoDiscounts,
                    BigDecimal.ZERO
                    );
        });
    }

    //TC-2
    @Test
    void testInvalidPenalties(){

        assertThrows(IllegalArgumentException.class, () -> {
            defaultDiscountAndCapService.applyDiscountAndCaps(
                    BigDecimal.ZERO,
                    discountInfoWithNoDiscounts,
                    new BigDecimal(-1)
            );
        });
    }
    //TC-3
    @Test
    void throwsWhenMaxPriceCapNegative() {
        try (MockedStatic<Settings> mocked = mockStatic(Settings.class)) {
            mocked.when(Settings::getMaxPriceCapacity)
                    .thenReturn(new BigDecimal("-1"));

            assertThrows(IllegalArgumentException.class, () ->
                    defaultDiscountAndCapService.applyDiscountAndCaps(
                            BigDecimal.ZERO,
                            discountInfoWithNoDiscounts,
                            BigDecimal.ZERO
                    )
            );
        }
    }

    //TC-4
    @Test
    void testWithZeroBasePrice(){
        assertEquals(new BigDecimal("0.00"),
                defaultDiscountAndCapService.applyDiscountAndCaps(
                        BigDecimal.ZERO,
                        discountInfoWithNoDiscounts,
                        BigDecimal.ZERO
                ));
    }
    //TC-5
    @Test
    void testWithSubscriptionDiscount(){
        DiscountInfo discountInfo= mock(DiscountInfo.class);
        when(discountInfo.hasSubscriptionPercentDiscount()).thenReturn(true);
        when(discountInfo.getSubscriptionDiscountPercent()).thenReturn(new BigDecimal("0.10"));

        assertEquals(new BigDecimal("108.00"),
                defaultDiscountAndCapService.applyDiscountAndCaps(
                        new BigDecimal("100.00"),
                        discountInfo,
                        new BigDecimal("20.00")
                ));
    }
    //TC-6
    @Test
    void testWithSubscriptionDiscountAndPromo(){
        DiscountInfo discountInfo= mock(DiscountInfo.class);
        // for subscription
        when(discountInfo.hasSubscriptionPercentDiscount()).thenReturn(true);
        when(discountInfo.getSubscriptionDiscountPercent()).thenReturn(new BigDecimal("0.10"));
        // for promo
        when(discountInfo.hasPromoPercentDiscount()).thenReturn(true);
        when(discountInfo.getPromoDiscountPercent()).thenReturn(new BigDecimal("0.20"));

        assertEquals(new BigDecimal("86.40"),
                defaultDiscountAndCapService.applyDiscountAndCaps(
                        new BigDecimal("100.00"),
                        discountInfo,
                        new BigDecimal("20.00")
                ));
    }
    //TC-7
    @Test
    void testWithSubscriptionDiscountAndPromoAndFixedPromo(){
        DiscountInfo discountInfo= mock(DiscountInfo.class);
        // for subscription
        when(discountInfo.hasSubscriptionPercentDiscount()).thenReturn(true);
        when(discountInfo.getSubscriptionDiscountPercent()).thenReturn(new BigDecimal("0.10"));
        // for promo
        when(discountInfo.hasPromoPercentDiscount()).thenReturn(true);
        when(discountInfo.getPromoDiscountPercent()).thenReturn(new BigDecimal("0.20"));
        // for fixed promo
        when(discountInfo.hasPromoFixedDiscount()).thenReturn(true);
        when(discountInfo.getPromoDiscountFixed()).thenReturn(new BigDecimal("10.00"));

        assertEquals(new BigDecimal("76.40"),
                defaultDiscountAndCapService.applyDiscountAndCaps(
                        new BigDecimal("100.00"),
                        discountInfo,
                        new BigDecimal("20.00")
                ));
    }
    //TC-8
    @Test
    void testWithSubscriptionDiscountAndPromoAndFixedPromoWhenResultLessThanZero(){
        DiscountInfo discountInfo= mock(DiscountInfo.class);
        // for subscription
        when(discountInfo.hasSubscriptionPercentDiscount()).thenReturn(true);
        when(discountInfo.getSubscriptionDiscountPercent()).thenReturn(new BigDecimal("0.10"));
        // for promo
        when(discountInfo.hasPromoPercentDiscount()).thenReturn(true);
        when(discountInfo.getPromoDiscountPercent()).thenReturn(new BigDecimal("0.20"));
        // for fixed promo
        when(discountInfo.hasPromoFixedDiscount()).thenReturn(true);
        when(discountInfo.getPromoDiscountFixed()).thenReturn(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("0.00"),
                defaultDiscountAndCapService.applyDiscountAndCaps(
                        new BigDecimal("100.00"),
                        discountInfo,
                        new BigDecimal("20.00")
                ));
    }
    //TC-9
    @Test
    void testWithNoDiscountAndHigherThanCapBasePrice(){
        assertEquals(new BigDecimal("1000000.00"),
                defaultDiscountAndCapService.applyDiscountAndCaps(
                        new BigDecimal("1200000"),
                        discountInfoWithNoDiscounts,
                        new BigDecimal("0.00")
                ));
    }
    //TC-10
    @Test
    void testWithNoDiscountAndHighPriceButLessThanCap(){
        assertEquals(new BigDecimal("900000.00"),
                defaultDiscountAndCapService.applyDiscountAndCaps(
                        new BigDecimal("900000"),
                        discountInfoWithNoDiscounts,
                        new BigDecimal("0.00")
                ));
    }
}
