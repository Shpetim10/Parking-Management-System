package UnitTesting.ShpetimShabanaj;

import Model.Tariff;
import Enum.DayType;
import Service.impl.DefaultPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PricingServiceApplyWeekendSurgeTest {
    private DefaultPricingService pricingService;
    private Tariff mockTariff;

    @BeforeEach
    void setUp() {
        pricingService = new DefaultPricingService();
        mockTariff = mock(Tariff.class);
    }

    @Test
    @DisplayName("TC-01: Should not apply surcharge on a regular Weekday")
    void testNoSurchargeOnWeekday() {
        BigDecimal basePrice = new BigDecimal("10.00");
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(new BigDecimal("20.0"));

        BigDecimal result = pricingService.applyWeekendOrHolidaySurcharge(basePrice, DayType.WEEKDAY, mockTariff);

        assertEquals(basePrice,result);
    }

    @Test
    @DisplayName("TC-02: Should apply 20% surcharge on a Weekend")
    void testSurchargeOnWeekend() {
        BigDecimal basePrice = new BigDecimal("10.00");
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(new BigDecimal("20.0"));

        BigDecimal result = pricingService.applyWeekendOrHolidaySurcharge(basePrice, DayType.WEEKEND, mockTariff);

        // 10.00 + (10.00 * 0.20) = 12.00
        assertEquals(new BigDecimal("12.00"), result);
    }

    @Test
    @DisplayName("TC-03: Should apply 10% surcharge on a Holiday")
    void testSurchargeOnHoliday() {
        BigDecimal basePrice = new BigDecimal("50.00");
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(new BigDecimal("10.0"));

        BigDecimal result = pricingService.applyWeekendOrHolidaySurcharge(basePrice, DayType.HOLIDAY, mockTariff);

        // 50.00 + (50.00 * 0.10) = 55.00
        assertEquals(new BigDecimal("55.00"),result);
    }

    @Test
    @DisplayName("TC-04: Should apply 0% surcharge on a Weekend")
    void testSurchargeOnWeekendWhenSurchargeIsZero() {
        BigDecimal basePrice = new BigDecimal("20.00");
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(new BigDecimal("0.0"));

        BigDecimal result = pricingService.applyWeekendOrHolidaySurcharge(basePrice, DayType.WEEKEND, mockTariff);

        // 20.00 + (20.00 * 0.00) = 20.00
        assertEquals(new BigDecimal("20.00"),result);
    }

    @Test
    @DisplayName("TC-05: Should return zero if the input price is null")
    void testNullPriceHandling() {
        BigDecimal result = pricingService.applyWeekendOrHolidaySurcharge(null, DayType.WEEKEND, mockTariff);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("TC-06: Should return zero if the input day type is null")
    void testNullDayTypeHandling() {
        BigDecimal result = pricingService.applyWeekendOrHolidaySurcharge(new BigDecimal("20.00"), null, mockTariff);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("TC-07: Should return zero if the input tariff object is null")
    void testNullTariffHandling() {
        BigDecimal result = pricingService.applyWeekendOrHolidaySurcharge(new BigDecimal("20.00"), DayType.WEEKEND, null);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result);
    }
}
