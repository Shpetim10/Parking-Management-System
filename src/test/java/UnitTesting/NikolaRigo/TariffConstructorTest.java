package UnitTesting.NikolaRigo;

import Model.Tariff;
import org.junit.jupiter.api.Test;
import Enum.ZoneType;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class TariffConstructorTest {

    @Test
    void constructor_WithValidParameters_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = new BigDecimal("5.00");
        BigDecimal dailyCap = new BigDecimal("50.00");
        BigDecimal surcharge = new BigDecimal("0.20");

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, surcharge);

        // Assert
        assertNotNull(tariff);
        assertEquals(zoneType, tariff.getZoneType());
        assertEquals(baseHourlyRate, tariff.getBaseHourlyRate());
        assertEquals(dailyCap, tariff.getDailyCap());
        assertEquals(surcharge, tariff.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    void constructor_WithNullZoneType_ShouldThrowNullPointerException() {
        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Tariff(
                        null,
                        new BigDecimal("5.00"),
                        new BigDecimal("50.00"),
                        new BigDecimal("0.20")
                )
        );

        assertEquals("zoneType must not be null", exception.getMessage());
    }

    @Test
    void constructor_WithNullBaseHourlyRate_ShouldThrowNullPointerException() {
        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Tariff(
                        ZoneType.STANDARD,
                        null,
                        new BigDecimal("50.00"),
                        new BigDecimal("0.20")
                )
        );

        assertEquals("baseHourlyRate must not be null", exception.getMessage());
    }

    @Test
    void constructor_WithNullDailyCap_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = new BigDecimal("5.00");
        BigDecimal surcharge = new BigDecimal("0.20");

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, null, surcharge);

        // Assert
        assertNotNull(tariff);
        assertNull(tariff.getDailyCap());
        assertEquals(zoneType, tariff.getZoneType());
        assertEquals(baseHourlyRate, tariff.getBaseHourlyRate());
    }

    @Test
    void constructor_WithNegativeBaseHourlyRate_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Tariff(
                        ZoneType.STANDARD,
                        new BigDecimal("-5.00"),
                        new BigDecimal("50.00"),
                        new BigDecimal("0.20")
                )
        );

        assertEquals("baseHourlyRate must not be negative", exception.getMessage());
    }

    @Test
    void constructor_WithNegativeDailyCap_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Tariff(
                        ZoneType.STANDARD,
                        new BigDecimal("5.00"),
                        new BigDecimal("-50.00"),
                        new BigDecimal("0.20")
                )
        );

        assertEquals("dailyCap must not be negative", exception.getMessage());
    }

    @Test
    void constructor_WithZeroBaseHourlyRate_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = BigDecimal.ZERO;
        BigDecimal dailyCap = new BigDecimal("50.00");
        BigDecimal surcharge = new BigDecimal("0.20");

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, surcharge);

        // Assert
        assertEquals(BigDecimal.ZERO, tariff.getBaseHourlyRate());
    }

    @Test
    void constructor_WithZeroDailyCap_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = new BigDecimal("5.00");
        BigDecimal dailyCap = BigDecimal.ZERO;
        BigDecimal surcharge = new BigDecimal("0.20");

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, surcharge);

        // Assert
        assertEquals(BigDecimal.ZERO, tariff.getDailyCap());
    }

    @Test
    void constructor_WithNullWeekendSurcharge_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = new BigDecimal("5.00");
        BigDecimal dailyCap = new BigDecimal("50.00");

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, null);

        // Assert
        assertNotNull(tariff);
        assertNull(tariff.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    void constructor_WithNegativeWeekendSurcharge_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = new BigDecimal("5.00");
        BigDecimal dailyCap = new BigDecimal("50.00");
        BigDecimal surcharge = new BigDecimal("-0.10");

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, surcharge);

        // Assert
        assertEquals(surcharge, tariff.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    void constructor_WithZeroWeekendSurcharge_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = new BigDecimal("5.00");
        BigDecimal dailyCap = new BigDecimal("50.00");
        BigDecimal surcharge = BigDecimal.ZERO;

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, surcharge);

        // Assert
        assertEquals(BigDecimal.ZERO, tariff.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    void constructor_WithDifferentZoneTypes_ShouldCreateInstances() {
        // Arrange
        ZoneType[] zoneTypes = {ZoneType.STANDARD, ZoneType.VIP}; // Add other types if they exist
        BigDecimal baseHourlyRate = new BigDecimal("5.00");
        BigDecimal dailyCap = new BigDecimal("50.00");
        BigDecimal surcharge = new BigDecimal("0.20");

        // Act & Assert
        for (ZoneType zoneType : zoneTypes) {
            Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, surcharge);
            assertEquals(zoneType, tariff.getZoneType());
        }
    }

    @Test
    void constructor_WithLargeBaseHourlyRate_ShouldCreateInstance() {
        // Arrange
        BigDecimal largeRate = new BigDecimal("999.99");

        // Act
        Tariff tariff = new Tariff(
                ZoneType.STANDARD,
                largeRate,
                new BigDecimal("1000.00"),
                new BigDecimal("0.20")
        );

        // Assert
        assertEquals(largeRate, tariff.getBaseHourlyRate());
    }

    @Test
    void constructor_WithLargeDailyCap_ShouldCreateInstance() {
        // Arrange
        BigDecimal largeCap = new BigDecimal("9999.99");

        // Act
        Tariff tariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("10.00"),
                largeCap,
                new BigDecimal("0.20")
        );

        // Assert
        assertEquals(largeCap, tariff.getDailyCap());
    }

    @Test
    void constructor_WithDecimalValues_ShouldPreservePrecision() {
        // Arrange
        BigDecimal baseHourlyRate = new BigDecimal("5.75");
        BigDecimal dailyCap = new BigDecimal("57.50");
        BigDecimal surcharge = new BigDecimal("0.15");

        // Act
        Tariff tariff = new Tariff(ZoneType.STANDARD, baseHourlyRate, dailyCap, surcharge);

        // Assert
        assertEquals("5.75", tariff.getBaseHourlyRate().toPlainString());
        assertEquals("57.50", tariff.getDailyCap().toPlainString());
        assertEquals("0.15", tariff.getWeekendOrHolidaySurchargePercent().toPlainString());
    }

    @Test
    void constructor_WithVerySmallPositiveValues_ShouldCreateInstance() {
        // Arrange
        BigDecimal smallRate = new BigDecimal("0.01");
        BigDecimal smallCap = new BigDecimal("0.10");
        BigDecimal smallSurcharge = new BigDecimal("0.001");

        // Act
        Tariff tariff = new Tariff(ZoneType.STANDARD, smallRate, smallCap, smallSurcharge);

        // Assert
        assertEquals(smallRate, tariff.getBaseHourlyRate());
        assertEquals(smallCap, tariff.getDailyCap());
        assertEquals(smallSurcharge, tariff.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    void constructor_WithHighSurchargePercent_ShouldCreateInstance() {
        // Arrange
        BigDecimal highSurcharge = new BigDecimal("2.50"); // 250% surcharge

        // Act
        Tariff tariff = new Tariff(
                ZoneType.STANDARD,
                new BigDecimal("5.00"),
                new BigDecimal("50.00"),
                highSurcharge
        );

        // Assert
        assertEquals(highSurcharge, tariff.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    void constructor_WithNullDailyCapAndNullSurcharge_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = new BigDecimal("5.00");

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, null, null);

        // Assert
        assertNotNull(tariff);
        assertEquals(zoneType, tariff.getZoneType());
        assertEquals(baseHourlyRate, tariff.getBaseHourlyRate());
        assertNull(tariff.getDailyCap());
        assertNull(tariff.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    void constructor_WithAllMinimumValidValues_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.STANDARD;
        BigDecimal baseHourlyRate = BigDecimal.ZERO;
        BigDecimal dailyCap = BigDecimal.ZERO;
        BigDecimal surcharge = BigDecimal.ZERO;

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, surcharge);

        // Assert
        assertNotNull(tariff);
        assertEquals(BigDecimal.ZERO, tariff.getBaseHourlyRate());
        assertEquals(BigDecimal.ZERO, tariff.getDailyCap());
        assertEquals(BigDecimal.ZERO, tariff.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    void constructor_ForPremiumZone_ShouldCreateInstance() {
        // Arrange
        ZoneType zoneType = ZoneType.VIP;
        BigDecimal baseHourlyRate = new BigDecimal("10.00");
        BigDecimal dailyCap = new BigDecimal("100.00");
        BigDecimal surcharge = new BigDecimal("0.50");

        // Act
        Tariff tariff = new Tariff(zoneType, baseHourlyRate, dailyCap, surcharge);

        // Assert
        assertEquals(ZoneType.VIP, tariff.getZoneType());
        assertEquals(new BigDecimal("10.00"), tariff.getBaseHourlyRate());
    }
}
