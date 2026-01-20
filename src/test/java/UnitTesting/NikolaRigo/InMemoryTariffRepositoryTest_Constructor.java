package UnitTesting.NikolaRigo;

import Enum.ZoneType;
import Model.Tariff;
import Repository.impl.InMemoryTariffRepository;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryTariffRepositoryTest_Constructor {

    @Test
    void withValidInitialTariffs_ShouldCreateRepository() {
        // Arrange
        Tariff mockStandardTariff = mock(Tariff.class);
        when(mockStandardTariff.getZoneType()).thenReturn(ZoneType.STANDARD);

        Tariff mockPremiumTariff = mock(Tariff.class);
        when(mockPremiumTariff.getZoneType()).thenReturn(ZoneType.VIP);

        Map<ZoneType, Tariff> initialTariffs = new EnumMap<>(ZoneType.class);
        initialTariffs.put(ZoneType.STANDARD, mockStandardTariff);
        initialTariffs.put(ZoneType.VIP, mockPremiumTariff);

        // Act
        InMemoryTariffRepository repository = new InMemoryTariffRepository(initialTariffs);

        // Assert
        assertNotNull(repository);
        assertEquals(mockStandardTariff, repository.findByZoneType(ZoneType.STANDARD));
        assertEquals(mockPremiumTariff, repository.findByZoneType(ZoneType.VIP));
    }

    @Test
    void withEmptyInitialTariffs_ShouldCreateEmptyRepository() {
        // Arrange
        Map<ZoneType, Tariff> initialTariffs = new EnumMap<>(ZoneType.class);

        // Act
        InMemoryTariffRepository repository = new InMemoryTariffRepository(initialTariffs);

        // Assert
        assertNotNull(repository);
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByZoneType(ZoneType.STANDARD));
    }

    @Test
    void withNullInitialTariffs_ShouldThrowNullPointerException() {
        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new InMemoryTariffRepository(null)
        );

        assertEquals("initialTariffs must not be null", exception.getMessage());
    }

    @Test
    void withAllZoneTypes_ShouldStoreAllTariffs() {
        // Arrange
        Tariff mockStandardTariff = mock(Tariff.class);
        when(mockStandardTariff.getZoneType()).thenReturn(ZoneType.STANDARD);

        Tariff mockPremiumTariff = mock(Tariff.class);
        when(mockPremiumTariff.getZoneType()).thenReturn(ZoneType.VIP);

        Tariff mockEvTariff = mock(Tariff.class);
        when(mockEvTariff.getZoneType()).thenReturn(ZoneType.EV);

        Map<ZoneType, Tariff> initialTariffs = new EnumMap<>(ZoneType.class);
        initialTariffs.put(ZoneType.STANDARD, mockStandardTariff);
        initialTariffs.put(ZoneType.VIP, mockPremiumTariff);
        initialTariffs.put(ZoneType.EV, mockEvTariff);

        // Act
        InMemoryTariffRepository repository = new InMemoryTariffRepository(initialTariffs);

        // Assert
        assertNotNull(repository);
        assertEquals(mockStandardTariff, repository.findByZoneType(ZoneType.STANDARD));
        assertEquals(mockPremiumTariff, repository.findByZoneType(ZoneType.VIP));
        assertEquals(mockEvTariff, repository.findByZoneType(ZoneType.EV));
    }

    @Test
    void shouldCreateDefensiveCopy_ModifyingOriginalMapShouldNotAffectRepository() {
        // Arrange
        Tariff mockStandardTariff = mock(Tariff.class);
        when(mockStandardTariff.getZoneType()).thenReturn(ZoneType.STANDARD);

        Tariff mockPremiumTariff = mock(Tariff.class);
        when(mockPremiumTariff.getZoneType()).thenReturn(ZoneType.VIP);

        Map<ZoneType, Tariff> initialTariffs = new EnumMap<>(ZoneType.class);
        initialTariffs.put(ZoneType.STANDARD, mockStandardTariff);

        // Act
        InMemoryTariffRepository repository = new InMemoryTariffRepository(initialTariffs);

        // Modify original map after repository creation
        initialTariffs.put(ZoneType.VIP, mockPremiumTariff);

        // Assert
        assertEquals(mockStandardTariff, repository.findByZoneType(ZoneType.STANDARD));
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByZoneType(ZoneType.VIP));
    }

    @Test
    void withSingleTariff_ShouldCreateRepositoryWithOneTariff() {
        // Arrange
        Tariff mockStandardTariff = mock(Tariff.class);
        when(mockStandardTariff.getZoneType()).thenReturn(ZoneType.STANDARD);

        Map<ZoneType, Tariff> initialTariffs = new EnumMap<>(ZoneType.class);
        initialTariffs.put(ZoneType.STANDARD, mockStandardTariff);

        // Act
        InMemoryTariffRepository repository = new InMemoryTariffRepository(initialTariffs);

        // Assert
        assertNotNull(repository);
        assertEquals(mockStandardTariff, repository.findByZoneType(ZoneType.STANDARD));
    }

    @Test
    void withHashMapInitialTariffs_ShouldStillWork() {
        // Arrange
        Tariff mockStandardTariff = mock(Tariff.class);
        when(mockStandardTariff.getZoneType()).thenReturn(ZoneType.STANDARD);

        Map<ZoneType, Tariff> initialTariffs = new HashMap<>();
        initialTariffs.put(ZoneType.STANDARD, mockStandardTariff);

        // Act
        InMemoryTariffRepository repository = new InMemoryTariffRepository(initialTariffs);

        // Assert
        assertNotNull(repository);
        assertEquals(mockStandardTariff, repository.findByZoneType(ZoneType.STANDARD));
    }

    @Test
    void withNullTariffValue_ShouldAcceptButThrowWhenAccessed() {
        // Arrange
        Map<ZoneType, Tariff> initialTariffs = new EnumMap<>(ZoneType.class);
        initialTariffs.put(ZoneType.STANDARD, null);

        // Act
        InMemoryTariffRepository repository = new InMemoryTariffRepository(initialTariffs);

        // Assert
        assertNotNull(repository);
        // findByZoneType will throw IllegalArgumentException because tariff is null
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByZoneType(ZoneType.STANDARD));
    }
}
