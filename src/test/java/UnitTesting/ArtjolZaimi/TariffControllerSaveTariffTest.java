package UnitTesting.ArtjolZaimi;

import Controller.TariffController;
import Dto.Tariff.TariffDto;
import Enum.ZoneType;
import Model.Tariff;
import Repository.TariffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

// Unit Tests for M-120: TariffController.saveTariff

class TariffControllerSaveTariffTest {

    private TariffController controller;

    @Mock
    private TariffRepository mockRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TariffController(mockRepository);
    }

    @Test
    @DisplayName("saves tariff successfully")
    void testSaveTariff_Success() {
        TariffDto dto = new TariffDto(
                ZoneType.STANDARD,
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                BigDecimal.ZERO
        );

        controller.saveTariff(dto);

        verify(mockRepository).save(any(Tariff.class));
    }

    @Test
    @DisplayName("throws exception for null dto")
    void testSaveTariff_NullDto() {
        assertThrows(NullPointerException.class, () -> {
            controller.saveTariff(null);
        });
    }

    @Test
    @DisplayName("creates tariff with correct values")
    void testSaveTariff_CorrectValues() {
        TariffDto dto = new TariffDto(
                ZoneType.VIP,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(15)
        );
        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);

        controller.saveTariff(dto);

        verify(mockRepository).save(captor.capture());
        Tariff saved = captor.getValue();
        assertEquals(ZoneType.VIP, saved.getZoneType());
        assertEquals(BigDecimal.valueOf(20), saved.getBaseHourlyRate());
        assertEquals(BigDecimal.valueOf(200), saved.getDailyCap());
        assertEquals(BigDecimal.valueOf(15), saved.getWeekendOrHolidaySurchargePercent());
    }

    @Test
    @DisplayName("saves tariff with zero surcharge")
    void testSaveTariff_ZeroSurcharge() {
        TariffDto dto = new TariffDto(
                ZoneType.STANDARD,
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                BigDecimal.ZERO
        );
        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);

        controller.saveTariff(dto);

        verify(mockRepository).save(captor.capture());
        assertEquals(BigDecimal.ZERO, captor.getValue().getWeekendOrHolidaySurchargePercent());
    }

}