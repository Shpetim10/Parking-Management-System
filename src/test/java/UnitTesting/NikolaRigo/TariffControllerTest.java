package UnitTesting.NikolaRigo;

import Controller.TariffController;
import Dto.Tariff.TariffDto;
import Model.Tariff;
import Repository.TariffRepository;
import Enum.ZoneType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TariffControllerTest {

    @Mock
    private TariffRepository tariffRepository;

    private TariffController tariffController;

    @BeforeEach
    void setUp() {
        tariffController = new TariffController(tariffRepository);
    }

    @Test
    @DisplayName("getTariffForZone: Should return correct DTO when zone type string is valid")
    void getTariffForZone_Success() {
        // Arrange
        String validZoneString = "VIP";
        ZoneType expectedEnum = ZoneType.VIP;

        // 1. Use BigDecimal for your expected values
        BigDecimal expectedRate = BigDecimal.valueOf(5.0);
        BigDecimal expectedCap = BigDecimal.valueOf(20.0);
        BigDecimal expectedSurcharge = BigDecimal.valueOf(10.0);

        // Setup the mock Model
        Tariff mockTariff = mock(Tariff.class);
        when(mockTariff.getZoneType()).thenReturn(expectedEnum);

        // 2. Return the BigDecimal variables
        when(mockTariff.getBaseHourlyRate()).thenReturn(expectedRate);
        when(mockTariff.getDailyCap()).thenReturn(expectedCap);
        when(mockTariff.getWeekendOrHolidaySurchargePercent()).thenReturn(expectedSurcharge);

        // Define Repo behavior
        when(tariffRepository.findByZoneType(expectedEnum)).thenReturn(mockTariff);

        // Act
        TariffDto result = tariffController.getTariffForZone(validZoneString);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEnum, result.zoneType());

        // 3. Assert Equals matching types (BigDecimal vs BigDecimal)
        assertEquals(expectedRate, result.baseHourlyRate());
        assertEquals(expectedCap, result.dailyCap());
        assertEquals(expectedSurcharge, result.weekendOrHolidaySurchargePercent());

        verify(tariffRepository).findByZoneType(expectedEnum);
    }

    @Test
    @DisplayName("getTariffForZone: Should throw IllegalArgumentException for unknown zone string")
    void getTariffForZone_InvalidZoneType() {
        // Arrange
        String invalidZoneString = "SUPER_SECRET_ZONE";

        // Act & Assert
        // Enum.valueOf throws IllegalArgumentException when the name doesn't exist
        assertThrows(IllegalArgumentException.class, () -> {
            tariffController.getTariffForZone(invalidZoneString);
        });

        // Verify repo was never called because it failed at the Enum conversion step
        verifyNoInteractions(tariffRepository);
    }

    @Test
    @DisplayName("getTariffForZone: Should throw NullPointerException when input is null")
    void getTariffForZone_NullInput() {
        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            tariffController.getTariffForZone(null);
        });

        assertEquals("zoneTypeName must not be null", exception.getMessage());
        verifyNoInteractions(tariffRepository);
    }
}
