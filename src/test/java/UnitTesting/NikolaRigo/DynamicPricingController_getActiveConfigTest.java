package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import Controller.DynamicPricingController;
import Dto.DynamicPricingConfig.DynamicPricingConfigDto;
import Model.DynamicPricingConfig;
import Repository.DynamicPricingConfigRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DynamicPricingController_getActiveConfigTest {

    private DynamicPricingController controller;

    @Mock
    private DynamicPricingConfigRepository mockRepository;

    @Mock
    private DynamicPricingConfig mockConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new DynamicPricingController(mockRepository);
    }

    @Test
    void getActiveConfig_ShouldReturnDtoWithCorrectValues() {
        // Arrange
        when(mockRepository.getActiveConfig()).thenReturn(mockConfig);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.5);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.8);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.3);

        // Act
        DynamicPricingConfigDto result = controller.getActiveConfig();

        // Assert
        assertNotNull(result);
        assertEquals(1.5, result.peakHourMultiplier());
        assertEquals(0.8, result.highOccupancyThreshold());
        assertEquals(1.3, result.highOccupancyMultiplier());
        verify(mockRepository).getActiveConfig();
        verify(mockConfig).getPeakHourMultiplier();
        verify(mockConfig).getHighOccupancyThreshold();
        verify(mockConfig).getHighOccupancyMultiplier();
    }

    @Test
    void getActiveConfig_ShouldCallRepositoryGetActiveConfig() {
        // Arrange
        when(mockRepository.getActiveConfig()).thenReturn(mockConfig);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(2.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.9);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.5);

        // Act
        controller.getActiveConfig();

        // Assert
        verify(mockRepository, times(1)).getActiveConfig();
    }

    @Test
    void getActiveConfig_WithDifferentConfigValues_ShouldReturnCorrectDto() {
        // Arrange
        when(mockRepository.getActiveConfig()).thenReturn(mockConfig);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(3.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.5);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(2.0);

        // Act
        DynamicPricingConfigDto result = controller.getActiveConfig();

        // Assert
        assertNotNull(result);
        assertEquals(3.0, result.peakHourMultiplier());
        assertEquals(0.5, result.highOccupancyThreshold());
        assertEquals(2.0, result.highOccupancyMultiplier());
        verify(mockRepository).getActiveConfig();
    }

    @Test
    void getActiveConfig_WithMinimumValidValues_ShouldReturnCorrectDto() {
        // Arrange
        when(mockRepository.getActiveConfig()).thenReturn(mockConfig);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(0.01);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.0);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(0.01);

        // Act
        DynamicPricingConfigDto result = controller.getActiveConfig();

        // Assert
        assertNotNull(result);
        assertEquals(0.01, result.peakHourMultiplier());
        assertEquals(0.0, result.highOccupancyThreshold());
        assertEquals(0.01, result.highOccupancyMultiplier());
        verify(mockRepository).getActiveConfig();
    }

    @Test
    void getActiveConfig_WithMaximumValidValues_ShouldReturnCorrectDto() {
        // Arrange
        when(mockRepository.getActiveConfig()).thenReturn(mockConfig);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(10.0);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(1.0);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(10.0);

        // Act
        DynamicPricingConfigDto result = controller.getActiveConfig();

        // Assert
        assertNotNull(result);
        assertEquals(10.0, result.peakHourMultiplier());
        assertEquals(1.0, result.highOccupancyThreshold());
        assertEquals(10.0, result.highOccupancyMultiplier());
        verify(mockRepository).getActiveConfig();
    }

    @Test
    void getActiveConfig_WithPreciseDecimalValues_ShouldPreservePrecision() {
        // Arrange
        when(mockRepository.getActiveConfig()).thenReturn(mockConfig);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.12345);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.67890);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.98765);

        // Act
        DynamicPricingConfigDto result = controller.getActiveConfig();

        // Assert
        assertNotNull(result);
        assertEquals(1.12345, result.peakHourMultiplier(), 0.00001);
        assertEquals(0.67890, result.highOccupancyThreshold(), 0.00001);
        assertEquals(1.98765, result.highOccupancyMultiplier(), 0.00001);
        verify(mockRepository).getActiveConfig();
    }

    @Test
    void getActiveConfig_ShouldNotModifyRepositoryState() {
        // Arrange
        when(mockRepository.getActiveConfig()).thenReturn(mockConfig);
        when(mockConfig.getPeakHourMultiplier()).thenReturn(1.5);
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.8);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.3);

        // Act
        controller.getActiveConfig();

        // Assert
        verify(mockRepository).getActiveConfig();
        verify(mockRepository, never()).save(any());
        verifyNoMoreInteractions(mockRepository);
    }
}
