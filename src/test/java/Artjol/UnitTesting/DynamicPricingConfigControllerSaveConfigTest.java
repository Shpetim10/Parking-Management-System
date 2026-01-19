package Artjol.UnitTesting;

import Controller.DynamicPricingController;
import Dto.DynamicPricingConfig.DynamicPricingConfigDto;
import Model.DynamicPricingConfig;
import Repository.DynamicPricingConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit Tests for M-105: DynamicPricingConfigController.saveConfig

class DynamicPricingConfigControllerSaveConfigTest {

    private DynamicPricingController controller;

    @Mock
    private DynamicPricingConfigRepository mockRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new DynamicPricingController(mockRepository);
    }

    @Test
    @DisplayName("saves config successfully")
    void testSaveConfig_Success() {
        DynamicPricingConfigDto dto = new DynamicPricingConfigDto(1.5, 0.8, 1.3);

        controller.saveConfig(dto);

        verify(mockRepository).save(any(DynamicPricingConfig.class));
    }

    @Test
    @DisplayName("throws exception for null dto")
    void testSaveConfig_NullDto() {
        assertThrows(NullPointerException.class, () -> {
            controller.saveConfig(null);
        });
    }

    @Test
    @DisplayName("creates config with correct values")
    void testSaveConfig_CorrectValues() {
        DynamicPricingConfigDto dto = new DynamicPricingConfigDto(2.0, 0.9, 1.8);
        ArgumentCaptor<DynamicPricingConfig> captor = ArgumentCaptor.forClass(DynamicPricingConfig.class);

        controller.saveConfig(dto);

        verify(mockRepository).save(captor.capture());
        DynamicPricingConfig saved = captor.getValue();
        assertEquals(2.0, saved.getPeakHourMultiplier());
        assertEquals(0.9, saved.getHighOccupancyThreshold());
        assertEquals(1.8, saved.getHighOccupancyMultiplier());
    }

    @Test
    @DisplayName("saves config with minimum values")
    void testSaveConfig_MinimumValues() {
        DynamicPricingConfigDto dto = new DynamicPricingConfigDto(0.0001, 0.0, 0.0001);
        ArgumentCaptor<DynamicPricingConfig> captor = ArgumentCaptor.forClass(DynamicPricingConfig.class);

        controller.saveConfig(dto);

        verify(mockRepository).save(captor.capture());
        assertNotNull(captor.getValue());
    }

    @Test
    @DisplayName("saves config with maximum threshold")
    void testSaveConfig_MaximumThreshold() {
        DynamicPricingConfigDto dto = new DynamicPricingConfigDto(1.5, 1.0, 1.5);
        ArgumentCaptor<DynamicPricingConfig> captor = ArgumentCaptor.forClass(DynamicPricingConfig.class);

        controller.saveConfig(dto);

        verify(mockRepository).save(captor.capture());
        assertEquals(1.0, captor.getValue().getHighOccupancyThreshold());
    }

}
