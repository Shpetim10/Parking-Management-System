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

// Unit Tests for M-87: PricingCalculator.applyDailyCap

class PricingCalculatorApplyDailyCapTest {

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
    @DisplayName("no cap applied when price below cap")
    void testApplyDailyCap_BelowCap() {
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
    @DisplayName("caps price when exceeds daily cap")
    void testApplyDailyCap_ExceedsCap() {
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
    @DisplayName("price equals cap exactly")
    void testApplyDailyCap_ExactlyCap() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                10, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("100.00"), price);
    }

    @Test
    @DisplayName("cap multiplied for multi-day duration")
    void testApplyDailyCap_MultiDay() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(50));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                50, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
        );

        // 50 hours = ~2.08 days, ceil = 3 days * 50 cap = 150
        assertEquals(new BigDecimal("150.00"), price);
    }

    @Test
    @DisplayName("no cap applied when cap is zero")
    void testApplyDailyCap_ZeroCap() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.ZERO);
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                10, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("100.00"), price);
    }
}
