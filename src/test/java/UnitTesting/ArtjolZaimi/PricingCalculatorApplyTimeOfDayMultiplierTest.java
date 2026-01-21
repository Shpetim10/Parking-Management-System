package UnitTesting.ArtjolZaimi;

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

// Unit Tests for M-84: PricingCalculator.applyTimeOfDayMultiplier

class PricingCalculatorApplyTimeOfDayMultiplierTest {

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
    @DisplayName("no multiplier applied for OFF_PEAK")
    void testApplyTimeOfDay_OffPeak() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.5);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                2, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("20.00"), price);
    }

    @Test
    @DisplayName("applies multiplier for PEAK")
    void testApplyTimeOfDay_Peak() {
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
    @DisplayName("multiplier of 2.0 doubles price")
    void testApplyTimeOfDay_DoubleMultiplier() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(2.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                3, DayType.WEEKDAY, TimeOfDayBand.PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("60.00"), price);
    }

    @Test
    @DisplayName("multiplier of 1.0 keeps price same")
    void testApplyTimeOfDay_NoChangeMultiplier() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                2, DayType.WEEKDAY, TimeOfDayBand.PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("20.00"), price);
    }

    @Test
    @DisplayName("works with fractional multiplier")
    void testApplyTimeOfDay_FractionalMultiplier() {
        when(mockTariff.getBaseHourlyRate()).thenReturn(BigDecimal.TEN);
        when(mockTariff.getDailyCap()).thenReturn(BigDecimal.valueOf(100));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.25);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.0);

        BigDecimal price = pricingService.calculateBasePrice(
                4, DayType.WEEKDAY, TimeOfDayBand.PEAK, 0.5, mockTariff, mockConfig
        );

        assertEquals(new BigDecimal("50.00"), price);
    }

}
