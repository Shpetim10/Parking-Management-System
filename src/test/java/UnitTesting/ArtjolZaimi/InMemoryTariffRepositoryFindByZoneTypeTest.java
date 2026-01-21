package UnitTesting.ArtjolZaimi;

import Model.Tariff;
import Enum.ZoneType;
import Repository.impl.InMemoryTariffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

// Unit Tests for M-69: InMemoryTariffRepository.findByZoneType

class InMemoryTariffRepositoryFindByZoneTypeTest {

    private InMemoryTariffRepository repository;

    @Mock
    private Tariff mockTariff;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Map<ZoneType, Tariff> initialTariffs = new EnumMap<>(ZoneType.class);
        initialTariffs.put(ZoneType.STANDARD, mockTariff);
        repository = new InMemoryTariffRepository(initialTariffs);
    }

    @Test
    @DisplayName("returns tariff for existing zone type")
    void testFindByZoneType_Exists() {
        Tariff found = repository.findByZoneType(ZoneType.STANDARD);

        assertEquals(mockTariff, found);
    }

    @Test
    @DisplayName("throws exception for non-existent zone type")
    void testFindByZoneType_NotExists() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.findByZoneType(ZoneType.VIP);
        });

        assertTrue(exception.getMessage().contains("No tariff configured"));
    }

    @Test
    @DisplayName("throws exception for null zone type")
    void testFindByZoneType_NullZoneType() {
        assertThrows(NullPointerException.class, () -> {
            repository.findByZoneType(null);
        });
    }

    @Test
    @DisplayName("returns correct tariff among multiple")
    void testFindByZoneType_MultipleZoneTypes() {
        Tariff regularTariff = new Tariff(ZoneType.STANDARD, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.ZERO);
        Tariff vipTariff = new Tariff(ZoneType.VIP, BigDecimal.valueOf(20), BigDecimal.valueOf(200), BigDecimal.ZERO);

        Map<ZoneType, Tariff> tariffs = new EnumMap<>(ZoneType.class);
        tariffs.put(ZoneType.STANDARD, regularTariff);
        tariffs.put(ZoneType.VIP, vipTariff);

        InMemoryTariffRepository repo = new InMemoryTariffRepository(tariffs);

        assertEquals(regularTariff, repo.findByZoneType(ZoneType.STANDARD));
        assertEquals(vipTariff, repo.findByZoneType(ZoneType.VIP));
    }

    @Test
    @DisplayName("returns updated tariff after save")
    void testFindByZoneType_AfterSave() {
        Tariff newTariff = mock(Tariff.class);
        when(newTariff.getZoneType()).thenReturn(ZoneType.STANDARD);

        repository.save(newTariff);

        Tariff found = repository.findByZoneType(ZoneType.STANDARD);
        assertEquals(newTariff, found);
    }


}