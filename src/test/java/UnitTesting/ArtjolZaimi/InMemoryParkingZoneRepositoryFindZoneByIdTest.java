package UnitTesting.ArtjolZaimi;

import Model.ParkingZone;
import Enum.ZoneType;
import Repository.impl.InMemoryParkingZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

// Unit Tests for M-60: InMemoryParkingZoneRepository.findZoneById

class InMemoryParkingZoneRepositoryFindZoneByIdTest {

    private InMemoryParkingZoneRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingZoneRepository();
    }

    @Test
    @DisplayName("returns null for non-existent zone")
    void testFindZoneById_NonExistent() {
        ParkingZone zone = repository.findZoneById("non-existent");
        assertNull(zone);
    }

    @Test
    @DisplayName("returns zone when exists")
    void testFindZoneById_Exists() {
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        repository.save(zone);

        ParkingZone found = repository.findZoneById("zone-1");

        assertNotNull(found);
        assertEquals("zone-1", found.getZoneId());
    }

    @Test
    @DisplayName("returns correct zone among multiple")
    void testFindZoneById_MultipleZones() {
        ParkingZone zone1 = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingZone zone2 = new ParkingZone("zone-2", ZoneType.VIP, 0.9);
        ParkingZone zone3 = new ParkingZone("zone-3", ZoneType.EV, 0.7);

        repository.save(zone1);
        repository.save(zone2);
        repository.save(zone3);

        ParkingZone found = repository.findZoneById("zone-2");

        assertNotNull(found);
        assertEquals("zone-2", found.getZoneId());
        assertEquals(ZoneType.VIP, found.getZoneType());
    }

    @Test
    @DisplayName("returns same instance as saved")
    void testFindZoneById_SameInstance() {
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        repository.save(zone);

        ParkingZone found = repository.findZoneById("zone-1");

        assertSame(zone, found);
    }

    @Test
    @DisplayName("case sensitive zone id")
    void testFindZoneById_CaseSensitive() {
        ParkingZone zone = new ParkingZone("Zone-1", ZoneType.STANDARD, 0.8);
        repository.save(zone);

        assertNotNull(repository.findZoneById("Zone-1"));
        assertNull(repository.findZoneById("zone-1"));
    }


    @Test
    @DisplayName("returns updated zone after save")
    void testFindZoneById_AfterUpdate() {
        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        repository.save(zone);

        ParkingZone updated = new ParkingZone("zone-1", ZoneType.VIP, 0.9);
        repository.save(updated);

        ParkingZone found = repository.findZoneById("zone-1");
        assertEquals(ZoneType.VIP, found.getZoneType());
    }
}