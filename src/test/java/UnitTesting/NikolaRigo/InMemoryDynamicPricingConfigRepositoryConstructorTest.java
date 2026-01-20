package UnitTesting.NikolaRigo;

import org.junit.jupiter.api.Test;
import Model.DynamicPricingConfig;
import Repository.impl.InMemoryDynamicPricingConfigRepository;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryDynamicPricingConfigRepositoryConstructorTest {

    @Test
    void constructor_WithValidInitialConfig_ShouldCreateInstance() {
        // Arrange
        DynamicPricingConfig initialConfig = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(initialConfig);

        // Assert
        assertNotNull(repository);
    }

    @Test
    void constructor_WithNullInitialConfig_ShouldThrowNullPointerException() {
        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new InMemoryDynamicPricingConfigRepository(null)
        );

        assertEquals("initialConfig must not be null", exception.getMessage());
    }

    @Test
    void constructor_ShouldStoreInitialConfig() {
        // Arrange
        DynamicPricingConfig initialConfig = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(initialConfig);

        // Assert
        DynamicPricingConfig activeConfig = repository.getActiveConfig();
        assertEquals(initialConfig, activeConfig);
    }

    @Test
    void constructor_ShouldReturnSameConfigInstance() {
        // Arrange
        DynamicPricingConfig initialConfig = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(initialConfig);

        // Assert
        assertSame(initialConfig, repository.getActiveConfig());
    }

    @Test
    void constructor_WithDifferentConfigs_ShouldCreateSeparateInstances() {
        // Arrange
        DynamicPricingConfig config1 = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );
        DynamicPricingConfig config2 = new DynamicPricingConfig(
                2.0,
                0.9,
                1.5
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository1 =
                new InMemoryDynamicPricingConfigRepository(config1);
        InMemoryDynamicPricingConfigRepository repository2 =
                new InMemoryDynamicPricingConfigRepository(config2);

        // Assert
        assertNotSame(repository1, repository2);
        assertEquals(config1, repository1.getActiveConfig());
        assertEquals(config2, repository2.getActiveConfig());
    }

    @Test
    void constructor_MultipleInstances_ShouldBeIndependent() {
        // Arrange
        DynamicPricingConfig config1 = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );
        DynamicPricingConfig config2 = new DynamicPricingConfig(
                2.0,
                0.9,
                1.5
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository1 =
                new InMemoryDynamicPricingConfigRepository(config1);
        InMemoryDynamicPricingConfigRepository repository2 =
                new InMemoryDynamicPricingConfigRepository(config2);

        DynamicPricingConfig newConfig = new DynamicPricingConfig(
                2.5,
                0.95,
                1.8
        );
        repository1.save(newConfig);

        // Assert
        // repository1 should have the new config
        assertEquals(newConfig, repository1.getActiveConfig());

        // repository2 should still have its original config (independent state)
        assertEquals(config2, repository2.getActiveConfig());
    }

    @Test
    void constructor_ShouldAcceptSameConfigMultipleTimes() {
        // Arrange
        DynamicPricingConfig config = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository1 =
                new InMemoryDynamicPricingConfigRepository(config);
        InMemoryDynamicPricingConfigRepository repository2 =
                new InMemoryDynamicPricingConfigRepository(config);

        // Assert
        assertNotSame(repository1, repository2);
        assertSame(config, repository1.getActiveConfig());
        assertSame(config, repository2.getActiveConfig());
    }

    @Test
    void constructor_WithMinimumValidThresholds_ShouldCreateInstance() {
        // Arrange
        DynamicPricingConfig config = new DynamicPricingConfig(
                0.01,
                0.0,
                0.01
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(config);

        // Assert
        assertNotNull(repository);
        assertEquals(config, repository.getActiveConfig());
    }

    @Test
    void constructor_WithMaximumValidThresholds_ShouldCreateInstance() {
        // Arrange
        DynamicPricingConfig config = new DynamicPricingConfig(
                10.0,
                1.0,
                10.0
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(config);

        // Assert
        assertNotNull(repository);
        assertEquals(config, repository.getActiveConfig());
    }

    @Test
    void constructor_WithTypicalPricingConfig_ShouldCreateInstance() {
        // Arrange
        DynamicPricingConfig config = new DynamicPricingConfig(
                1.5,  // 50% peak hour increase
                0.8,  // 80% occupancy threshold
                1.25  // 25% high occupancy increase
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(config);

        // Assert
        assertNotNull(repository);
        DynamicPricingConfig activeConfig = repository.getActiveConfig();
        assertEquals(1.5, activeConfig.getPeakHourMultiplier());
        assertEquals(0.8, activeConfig.getHighOccupancyThreshold());
        assertEquals(1.25, activeConfig.getHighOccupancyMultiplier());
    }

    @Test
    void constructor_WithAggressivePricingConfig_ShouldCreateInstance() {
        // Arrange
        DynamicPricingConfig config = new DynamicPricingConfig(
                3.0,  // 200% peak hour increase
                0.5,  // 50% occupancy threshold
                2.0   // 100% high occupancy increase
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(config);

        // Assert
        assertNotNull(repository);
        assertEquals(config, repository.getActiveConfig());
    }

    @Test
    void constructor_WithConservativePricingConfig_ShouldCreateInstance() {
        // Arrange
        DynamicPricingConfig config = new DynamicPricingConfig(
                1.1,  // 10% peak hour increase
                0.95, // 95% occupancy threshold
                1.05  // 5% high occupancy increase
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(config);

        // Assert
        assertNotNull(repository);
        assertEquals(config, repository.getActiveConfig());
    }

    @Test
    void constructor_ShouldMakeConfigImmediatelyAvailable() {
        // Arrange
        DynamicPricingConfig config = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(config);

        // Assert
        // Should be able to retrieve config immediately after construction
        DynamicPricingConfig retrievedConfig = repository.getActiveConfig();
        assertNotNull(retrievedConfig);
        assertSame(config, retrievedConfig);
    }

    @Test
    void constructor_AfterConstruction_ConfigShouldNotBeNull() {
        // Arrange
        DynamicPricingConfig config = new DynamicPricingConfig(
                1.5,
                0.8,
                1.3
        );

        // Act
        InMemoryDynamicPricingConfigRepository repository =
                new InMemoryDynamicPricingConfigRepository(config);

        // Assert
        assertNotNull(repository.getActiveConfig());
    }
}
