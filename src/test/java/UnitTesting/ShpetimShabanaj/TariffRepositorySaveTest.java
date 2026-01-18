package UnitTesting.ShpetimShabanaj;

import Model.Tariff;
import Repository.TariffRepository;
import Repository.impl.InMemoryTariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import Enum.ZoneType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TariffRepositorySaveTest {
    private TariffRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTariffRepository(new HashMap<>());
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should store a new tariff successfully")
    void testSaveNewTariff() {
        ZoneType type = ZoneType.STANDARD;
        Tariff mockTariff = mock(Tariff.class);
        when(mockTariff.getZoneType()).thenReturn(type);

        repository.save(mockTariff);

        assertEquals(mockTariff, repository.findByZoneType(type));
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should throw NullPointerException when saving null")
    void testSaveNullTariff() {
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                repository.save(null)
        );
        assertEquals("tariff must not be null", ex.getMessage());
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should update the tariff when the same ZoneType is saved again")
    void testSaveOverwritesExistingTariff() {
        ZoneType type = ZoneType.STANDARD;
        Tariff firstTariff = mock(Tariff.class);
        Tariff secondTariff = mock(Tariff.class);

        when(firstTariff.getZoneType()).thenReturn(type);
        when(secondTariff.getZoneType()).thenReturn(type);

        repository.save(firstTariff);
        repository.save(secondTariff);

        assertEquals(secondTariff, repository.findByZoneType(type));
    }

}
