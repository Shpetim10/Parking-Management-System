package IntegrationTesting.ShpëtimShabanaj;

import Controller.DynamicPricingController;
import Dto.DynamicPricingConfig.DynamicPricingConfigDto;
import Model.DynamicPricingConfig;
import Repository.impl.InMemoryDynamicPricingConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Testing: DynamicPricingController -- InMemoryDynamicPricingConfigRepository")
class DynamicPricingConfigIntegrationTesting {

    private DynamicPricingController dynamicPricingController;
    private InMemoryDynamicPricingConfigRepository configRepository;

    @BeforeEach
    void setUp() {
        DynamicPricingConfig initialConfig = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );

        configRepository = new InMemoryDynamicPricingConfigRepository(initialConfig);
        dynamicPricingController = new DynamicPricingController(configRepository);
    }

    // IT-01: Happy path - get active config as DTO
    @Test
    @DisplayName("IT-01: Should return DTO matching current active dynamic pricing config")
    void testGetActiveConfigReturnsCurrentConfigAsDto() {
        DynamicPricingConfigDto dto = dynamicPricingController.getActiveConfig();

        assertAll(
                () -> assertEquals(1.5, dto.peakHourMultiplier(), 0.0001),
                () -> assertEquals(0.8, dto.highOccupancyThreshold(), 0.0001),
                () -> assertEquals(1.3, dto.highOccupancyMultiplier(), 0.0001)
        );
    }

    // IT-02: saveConfig updates repository and subsequent getActiveConfig reflects changes
    @Test
    @DisplayName("IT-02: Should update active config and reflect it via getActiveConfig")
    void testSaveConfigThenGetActiveConfigReturnsUpdatedValues() {
        DynamicPricingConfigDto newDto = new DynamicPricingConfigDto(
                2.0,
                0.75,
                1.6
        );

        dynamicPricingController.saveConfig(newDto);

        DynamicPricingConfig activeConfig = configRepository.getActiveConfig();
        DynamicPricingConfigDto dtoFromController = dynamicPricingController.getActiveConfig();

        assertAll(
                () -> assertEquals(2.0, activeConfig.getPeakHourMultiplier(), 0.0001),
                () -> assertEquals(0.75, activeConfig.getHighOccupancyThreshold(), 0.0001),
                () -> assertEquals(1.6, activeConfig.getHighOccupancyMultiplier(), 0.0001),
                () -> assertEquals(2.0, dtoFromController.peakHourMultiplier(), 0.0001),
                () -> assertEquals(0.75, dtoFromController.highOccupancyThreshold(), 0.0001),
                () -> assertEquals(1.6, dtoFromController.highOccupancyMultiplier(), 0.0001)
        );
    }

    // IT-03: Null dto validation in saveConfig
    @Test
    @DisplayName("IT-03: Should throw NullPointerException when saving null config DTO")
    void testSaveConfigNullDtoThrowsNPE() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> dynamicPricingController.saveConfig(null)
        );

        assertEquals("dto must not be null", ex.getMessage());
    }

    // IT-04: Repository retains config instance and controller always reads from it
    @Test
    @DisplayName("IT-04: Should reflect repository changes when config saved directly in repository")
    void testRepositoryDirectSaveReflectedThroughController() {
        DynamicPricingConfig directConfig = new DynamicPricingConfig(
                1.8,
                0.9,
                1.4
        );

        configRepository.save(directConfig);

        DynamicPricingConfigDto dto = dynamicPricingController.getActiveConfig();

        assertAll(
                () -> assertEquals(1.8, dto.peakHourMultiplier(), 0.0001),
                () -> assertEquals(0.9, dto.highOccupancyThreshold(), 0.0001),
                () -> assertEquals(1.4, dto.highOccupancyMultiplier(), 0.0001)
        );
    }

    // IT-05: Multiple consecutive saves
    @Test
    @DisplayName("IT-05: Should use the last saved config when multiple configs are saved")
    void testSaveConfigMultipleCallsLastConfigWins() {
        DynamicPricingConfigDto firstDto = new DynamicPricingConfigDto(
                1.6,
                0.7,
                1.4
        );

        DynamicPricingConfigDto secondDto = new DynamicPricingConfigDto(
                2.2,
                0.9,
                1.8
        );

        dynamicPricingController.saveConfig(firstDto);
        dynamicPricingController.saveConfig(secondDto);

        DynamicPricingConfigDto dto = dynamicPricingController.getActiveConfig();

        assertAll(
                () -> assertEquals(2.2, dto.peakHourMultiplier(), 0.0001),
                () -> assertEquals(0.9, dto.highOccupancyThreshold(), 0.0001),
                () -> assertEquals(1.8, dto.highOccupancyMultiplier(), 0.0001)
        );
    }

    // IT-06: Edge numeric values
    @Test
    @DisplayName("IT-06: Should persist and return edge numeric values in config")
    void testSaveConfigEdgeNumericValuesPersistedAndReturned() {
        DynamicPricingConfigDto edgeDto = new DynamicPricingConfigDto(
                0.01,
                0.99,
                0.5
        );

        dynamicPricingController.saveConfig(edgeDto);

        DynamicPricingConfig activeConfig = configRepository.getActiveConfig();
        DynamicPricingConfigDto dto = dynamicPricingController.getActiveConfig();

        assertAll(
                () -> assertEquals(0.01, activeConfig.getPeakHourMultiplier(), 0.000001),
                () -> assertEquals(0.99, activeConfig.getHighOccupancyThreshold(), 0.000001),
                () -> assertEquals(0.5, activeConfig.getHighOccupancyMultiplier(), 0.000001),
                () -> assertEquals(0.01, dto.peakHourMultiplier(), 0.000001),
                () -> assertEquals(0.99, dto.highOccupancyThreshold(), 0.000001),
                () -> assertEquals(0.5, dto.highOccupancyMultiplier(), 0.000001)
        );
    }

}

