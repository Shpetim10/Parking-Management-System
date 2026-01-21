package UnitTesting.NikolaRigo;

import Enum.DayType;
import Enum.TimeOfDayBand;
import Model.DynamicPricingConfig;
import Model.Tariff;
import Service.impl.DefaultPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultPricingServiceTest_validateInputs {

    private DefaultPricingService service;
    private Tariff mockTariff;
    private DynamicPricingConfig mockConfig;

    @BeforeEach
    void setUp() {
        service = new DefaultPricingService();
        mockTariff = mock(Tariff.class);
        mockConfig = mock(DynamicPricingConfig.class);

        // Setup default mock behaviors to prevent NullPointerException
        when(mockTariff.getBaseHourlyRate()).thenReturn(new BigDecimal("10.00"));
        when(mockTariff.getDailyCap()).thenReturn(new BigDecimal("100.00"));
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(new BigDecimal("20.00"));
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.5);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.8);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.3);
    }

    @Test
    void withValidInputs_ShouldNotThrowException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.75;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );
    }

    @Test
    void withZeroDurationHours_ShouldNotThrowException() {
        // Arrange
        int durationHours = 0;
        double occupancyRatio = 0.5;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );
    }

    @Test
    void withNegativeDurationHours_ShouldThrowIllegalArgumentException() {
        // Arrange
        int durationHours = -1;
        double occupancyRatio = 0.5;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );

        assertEquals("durationHours must be a finite value >= 0.0", exception.getMessage());
    }

    @Test
    void withOccupancyRatioZero_ShouldNotThrowException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.0;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );
    }

    @Test
    void withOccupancyRatioOne_ShouldNotThrowException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 1.0;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );
    }

    @Test
    void withOccupancyRatioNegative_ShouldThrowIllegalArgumentException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = -0.1;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );

        assertEquals("occupancyRatio must be between 0.0 and 1.0 inclusive", exception.getMessage());
    }

    @Test
    void withOccupancyRatioGreaterThanOne_ShouldThrowIllegalArgumentException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 1.5;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );

        assertEquals("occupancyRatio must be between 0.0 and 1.0 inclusive", exception.getMessage());
    }

    @Test
    void withNullDayType_ShouldThrowNullPointerException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.5;
        DayType dayType = null;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );

        assertEquals("dayType must not be null", exception.getMessage());
    }

    @Test
    void withNullTimeOfDayBand_ShouldThrowNullPointerException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.5;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = null;

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );

        assertEquals("timeOfDayBand must not be null", exception.getMessage());
    }

    @Test
    void withNullTariff_ShouldThrowNullPointerException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.5;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, null, mockConfig)
        );

        assertEquals("tariff must not be null", exception.getMessage());
    }

    @Test
    void withNullConfig_ShouldThrowNullPointerException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.5;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, null)
        );

        assertEquals("config must not be null", exception.getMessage());
    }

    @Test
    void withAllNullObjects_ShouldThrowNullPointerException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.5;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> service.calculateBasePrice(durationHours, null, null,
                        occupancyRatio, null, null)
        );
    }

    @Test
    void withBoundaryOccupancyRatio_ShouldNotThrowException() {
        // Arrange
        int durationHours = 5;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert - Test lower boundary
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        0.0, mockTariff, mockConfig)
        );

        // Act & Assert - Test upper boundary
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        1.0, mockTariff, mockConfig)
        );
    }

    @Test
    void withValidMidRangeOccupancy_ShouldNotThrowException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.5;
        DayType dayType = DayType.WEEKEND;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.OFF_PEAK;

        // Act & Assert
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );
    }

    @Test
    void withLargeDurationHours_ShouldNotThrowException() {
        // Arrange
        int durationHours = 1000;
        double occupancyRatio = 0.5;
        DayType dayType = DayType.WEEKDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );
    }

    @Test
    void withHolidayDayType_ShouldNotThrowException() {
        // Arrange
        int durationHours = 5;
        double occupancyRatio = 0.5;
        DayType dayType = DayType.HOLIDAY;
        TimeOfDayBand timeOfDayBand = TimeOfDayBand.PEAK;

        // Act & Assert
        assertDoesNotThrow(() ->
                service.calculateBasePrice(durationHours, dayType, timeOfDayBand,
                        occupancyRatio, mockTariff, mockConfig)
        );
    }
}