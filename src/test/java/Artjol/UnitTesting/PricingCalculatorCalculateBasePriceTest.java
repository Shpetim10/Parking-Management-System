package Artjol.UnitTesting;

import Model.Tariff;
import Model.DynamicPricingConfig;
import Enum.*;
import Service.PricingService;
import Service.impl.DefaultPricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

// Unit Tests for M-81: PricingCalculator.calculateBasePrice

class PricingCalculatorCalculateBasePriceTest {

    private PricingService pricingService;

    @Mock
    private Tariff mockTariff;

    @Mock
    private DynamicPricingConfig mockConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pricingService = new DefaultPricingService();
    }

    @Test
    @DisplayName("calculates zero price for zero duration")
    void testCalculateBasePrice_ZeroDuration() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.8);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                0, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("0.00"), price);
    }

    @Test
    @DisplayName("calculates price for single hour")
    void testCalculateBasePrice_SingleHour() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                1, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("10.00"), price);
    }

    @Test
    @DisplayName("calculates price for multiple hours")
    void testCalculateBasePrice_MultipleHours() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                5, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("50.00"), price);
    }

    @Test
    @DisplayName("applies peak hour multiplier")
    void testCalculateBasePrice_PeakHourMultiplier() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.5);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                2, DayType.WEEKDAY, TimeOfDayBand.PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("30.00"), price);
    }

    @Test
    @DisplayName("applies high occupancy surcharge")
    void testCalculateBasePrice_HighOccupancySurcharge() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.7);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.5);

        BigDecimal price = pricingService.calculateBasePrice(
                2, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.8, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("30.00"), price);
    }

    @Test
    @DisplayName("applies daily cap")
    void testCalculateBasePrice_DailyCap() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(50));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                10, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("50.00"), price);
    }

    @Test
    @DisplayName("throws exception for null tariff")
    void testCalculateBasePrice_NullTariff() {
        assertThrows(NullPointerException.class, () -> {
            pricingService.calculateBasePrice(
                    1, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, null, mockConfig
            );
        });
    }

    @Test
    @DisplayName("throws exception for negative duration")
    void testCalculateBasePrice_NegativeDuration() {
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.8);

        assertThrows(IllegalArgumentException.class, () -> {
            pricingService.calculateBasePrice(
                    -1, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
            );
        });
    }
}