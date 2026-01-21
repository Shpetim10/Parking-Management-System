package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import Service.impl.DefaultPricingService;
import Model.Tariff;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultPricingService_getWeekendOrHolidaySurchargePercentTest {

    private DefaultPricingService pricingService;

    @Mock
    private Tariff mockTariff;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pricingService = new DefaultPricingService();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithPositiveSurcharge_ShouldReturnDecimalValue() {
        // Arrange
        BigDecimal surchargePercent = new BigDecimal("20.00"); // 20%
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(surchargePercent);

        // Act
        // We need to use reflection to access the private method
        BigDecimal result = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        BigDecimal expected = new BigDecimal("0.20"); // 20% as decimal
        assertEquals(0, expected.compareTo(result));
        verify(mockTariff).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithZeroSurcharge_ShouldReturnZero() {
        // Arrange
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(BigDecimal.ZERO);

        // Act
        BigDecimal result = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
        verify(mockTariff).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithNullSurcharge_ShouldReturnZero() {
        // Arrange
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(null);

        // Act
        BigDecimal result = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
        verify(mockTariff).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithNegativeSurcharge_ShouldReturnZero() {
        // Arrange
        BigDecimal negativeSurcharge = new BigDecimal("-10.00");
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(negativeSurcharge);

        // Act
        BigDecimal result = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
        verify(mockTariff).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithLargeSurcharge_ShouldConvertCorrectly() {
        // Arrange
        BigDecimal largeSurcharge = new BigDecimal("100.00"); // 100%
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(largeSurcharge);

        // Act
        BigDecimal result = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        BigDecimal expected = new BigDecimal("1.00"); // 100% as decimal
        assertEquals(0, expected.compareTo(result));
        verify(mockTariff).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithDecimalSurcharge_ShouldConvertCorrectly() {
        // Arrange
        BigDecimal decimalSurcharge = new BigDecimal("15.50"); // 15.5%
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(decimalSurcharge);

        // Act
        BigDecimal result = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        BigDecimal expected = new BigDecimal("0.155"); // 15.5% as decimal
        assertEquals(0, expected.compareTo(result));
        verify(mockTariff).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithVerySmallSurcharge_ShouldConvertCorrectly() {
        // Arrange
        BigDecimal smallSurcharge = new BigDecimal("0.01"); // 0.01%
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(smallSurcharge);

        // Act
        BigDecimal result = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        BigDecimal expected = new BigDecimal("0.0001"); // 0.01% as decimal
        assertEquals(0, expected.compareTo(result));
        verify(mockTariff).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_CalledMultipleTimes_ShouldReturnSameValue() {
        // Arrange
        BigDecimal surcharge = new BigDecimal("25.00");
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(surcharge);

        // Act
        BigDecimal result1 = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);
        BigDecimal result2 = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);
        BigDecimal result3 = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        assertEquals(0, result1.compareTo(result2));
        assertEquals(0, result1.compareTo(result3));
        verify(mockTariff, times(3)).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithSlightlyNegativeValue_ShouldReturnZero() {
        // Arrange
        BigDecimal slightlyNegative = new BigDecimal("-0.01");
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(slightlyNegative);

        // Act
        BigDecimal result = invokeGetWeekendOrHolidaySurchargePercent(mockTariff);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
        verify(mockTariff).getWeekendOrHolidaySurchargePercent();
    }

    @Test
    void getWeekendOrHolidaySurchargePercent_WithDifferentTariffs_ShouldReturnDifferentValues() {
        // Arrange
        Tariff mockTariff1 = mock(Tariff.class);
        Tariff mockTariff2 = mock(Tariff.class);

        when(mockTariff1.getWeekendOrHolidaySurchargePercent()).thenReturn(new BigDecimal("10.00"));
        when(mockTariff2.getWeekendOrHolidaySurchargePercent()).thenReturn(new BigDecimal("20.00"));

        // Act
        BigDecimal result1 = invokeGetWeekendOrHolidaySurchargePercent(mockTariff1);
        BigDecimal result2 = invokeGetWeekendOrHolidaySurchargePercent(mockTariff2);

        // Assert
        assertEquals(0, new BigDecimal("0.10").compareTo(result1));
        assertEquals(0, new BigDecimal("0.20").compareTo(result2));
        verify(mockTariff1).getWeekendOrHolidaySurchargePercent();
        verify(mockTariff2).getWeekendOrHolidaySurchargePercent();
    }

    // Helper method to invoke private method using reflection
    private BigDecimal invokeGetWeekendOrHolidaySurchargePercent(Tariff tariff) {
        try {
            java.lang.reflect.Method method = DefaultPricingService.class.getDeclaredMethod(
                    "getWeekendOrHolidaySurchargePercent",
                    Tariff.class
            );
            method.setAccessible(true);
            return (BigDecimal) method.invoke(pricingService, tariff);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method", e);
        }
    }
}