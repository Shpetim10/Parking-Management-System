package UnitTesting.ArtjolZaimi;
import Model.DynamicPricingConfig;
import Repository.impl.InMemoryDynamicPricingConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit Tests for M-42: InMemoryDynamicPricingConfigRepository.getActiveConfig

class InMemoryDynamicPricingConfigRepositoryGetActiveConfigTest {

    private InMemoryDynamicPricingConfigRepository repository;

    @Mock
    private DynamicPricingConfig mockConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new InMemoryDynamicPricingConfigRepository(mockConfig);
    }

    @Test
    @DisplayName("getActiveConfig returns initial config")
    void testGetActiveConfig_ReturnsInitialConfig() {
        DynamicPricingConfig config = repository.getActiveConfig();

        assertEquals(mockConfig, config);
    }

    @Test
    @DisplayName("getActiveConfig returns updated config after save")
    void testGetActiveConfig_ReturnsUpdatedConfig() {
        DynamicPricingConfig newConfig = mock(DynamicPricingConfig.class);

        repository.save(newConfig);

        DynamicPricingConfig activeConfig = repository.getActiveConfig();
        assertEquals(newConfig, activeConfig);
        assertNotEquals(mockConfig, activeConfig);
    }



    @Test
    @DisplayName("getActiveConfig after multiple saves returns latest")
    void testGetActiveConfig_AfterMultipleSaves() {
        DynamicPricingConfig config1 = mock(DynamicPricingConfig.class);
        DynamicPricingConfig config2 = mock(DynamicPricingConfig.class);
        DynamicPricingConfig config3 = mock(DynamicPricingConfig.class);

        repository.save(config1);
        assertEquals(config1, repository.getActiveConfig());

        repository.save(config2);
        assertEquals(config2, repository.getActiveConfig());

        repository.save(config3);
        assertEquals(config3, repository.getActiveConfig());
    }

    @Test
    @DisplayName("getActiveConfig returns same instance on multiple calls")
    void testGetActiveConfig_SameInstanceMultipleCalls() {
        DynamicPricingConfig config1 = repository.getActiveConfig();
        DynamicPricingConfig config2 = repository.getActiveConfig();

        assertSame(config1, config2);
    }



    @Test
    @DisplayName("getActiveConfig not null")
    void testGetActiveConfig_NotNull() {
        DynamicPricingConfig config = repository.getActiveConfig();

        assertNotNull(config);
    }


}