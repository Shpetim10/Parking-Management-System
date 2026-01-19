package Artjol.UnitTesting;

import Model.ParkingZone;
import Enum.ZoneType;
import Repository.impl.InMemoryParkingZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;

// Unit Tests for M-57: InMemoryParkingZoneRepository.findAll

class InMemoryParkingZoneRepositoryFindAllTest {

    private InMemoryParkingZoneRepository repository;

    @Mock
    private ParkingZone mockZone;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new InMemoryParkingZoneRepository();
    }

    @Test
    @DisplayName("returns empty list for new repository")
    void testFindAll_EmptyRepository() {
        List<ParkingZone> zones = repository.findAll();

        assertNotNull(zones);
        assertTrue(zones.isEmpty());
    }

    @Test
    @DisplayName("returns all saved zones")
    void testFindAll_AllSavedZones() {
        when(mockZone.getZoneId()).thenReturn("zone-1");
        ParkingZone zone2 = mock(ParkingZone.class);
        when(zone2.getZoneId()).thenReturn("zone-2");

        repository.save(mockZone);
        repository.save(zone2);

        List<ParkingZone> zones = repository.findAll();

        assertEquals(2, zones.size());
        assertTrue(zones.contains(mockZone));
        assertTrue(zones.contains(zone2));
    }

    @Test
    @DisplayName("returns single zone")
    void testFindAll_SingleZone() {
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        repository.save(zone);

        List<ParkingZone> zones = repository.findAll();

        assertEquals(1, zones.size());
        assertEquals("zone-1", zones.get(0).getZoneId());
    }

    @Test
    @DisplayName("returns zones with different types")
    void testFindAll_DifferentZoneTypes() {
        ParkingZone regular = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingZone vip = new ParkingZone("zone-2", ZoneType.VIP, 0.9);

        repository.save(regular);
        repository.save(vip);

        List<ParkingZone> zones = repository.findAll();

        assertEquals(2, zones.size());
        assertTrue(zones.stream().anyMatch(z -> z.getZoneType() == ZoneType.STANDARD));
        assertTrue(zones.stream().anyMatch(z -> z.getZoneType() == ZoneType.VIP));
    }

    @Test
    @DisplayName("does not include duplicates")
    void testFindAll_NoDuplicates() {
        when(mockZone.getZoneId()).thenReturn("zone-1");

        repository.save(mockZone);
        repository.save(mockZone);

        List<ParkingZone> zones = repository.findAll();

        assertEquals(1, zones.size());
    }

    @Test
    @DisplayName("returns new list instance on each call")
    void testFindAll_NewListInstance() {
        when(mockZone.getZoneId()).thenReturn("zone-1");
        repository.save(mockZone);

        List<ParkingZone> zones1 = repository.findAll();
        List<ParkingZone> zones2 = repository.findAll();

        assertNotSame(zones1, zones2);
        assertEquals(zones1.size(), zones2.size());
    }

}