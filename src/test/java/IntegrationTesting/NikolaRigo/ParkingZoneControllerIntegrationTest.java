package IntegrationTesting.NikolaRigo;

import Controller.ParkingZoneController;
import Dto.Zone.ParkingSpotDto;
import Dto.Zone.ParkingZoneDto;
import Enum.SpotState;
import Enum.ZoneType;
import Model.ParkingSpot;
import Model.ParkingZone;
import Repository.ParkingZoneRepository;
import Repository.impl.InMemoryParkingZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for ParkingZoneController
 * 
 * Neighbourhood Integration Testing (Radius 1):
 * - REAL: InMemoryParkingZoneRepository (direct dependency)
 * 
 * Tests the integration between ParkingZoneController and ParkingZoneRepository
 */
@DisplayName("Integration Tests - ParkingZoneController")
class ParkingZoneControllerIntegrationTest {

    private ParkingZoneController parkingZoneController;
    private ParkingZoneRepository parkingZoneRepository;

    @BeforeEach
    void setUp() {
        // Initialize real in-memory repository
        parkingZoneRepository = new InMemoryParkingZoneRepository();

        // Create controller with real repository
        parkingZoneController = new ParkingZoneController(parkingZoneRepository);
    }

    // =========================================================================
    // CONSTRUCTOR TESTS
    // =========================================================================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create controller with valid repository")
        void constructor_WithValidRepository_ShouldCreate() {
            ParkingZoneController controller = new ParkingZoneController(parkingZoneRepository);
            assertNotNull(controller);
        }
    }

    // =========================================================================
    // createParkingZone() INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("createParkingZone() Integration Tests")
    class CreateParkingZoneTests {

        @Test
        @DisplayName("Should create STANDARD parking zone and persist to repository")
        void createParkingZone_Standard_ShouldPersist() {
            // Arrange
            ParkingZoneDto dto = new ParkingZoneDto("zone-1", "STANDARD", 0.8);

            // Act
            parkingZoneController.createParkingZone(dto);

            // Assert - Verify persisted in repository
            assertTrue(parkingZoneRepository.zoneExists("zone-1"));
            ParkingZone zone = parkingZoneRepository.findById("zone-1");
            assertNotNull(zone);
            assertEquals("zone-1", zone.getZoneId());
            assertEquals(ZoneType.STANDARD, zone.getZoneType());
            assertEquals(0.8, zone.getMaxOccupancyThreshold(), 0.001);
        }

        @Test
        @DisplayName("Should create EV parking zone and persist to repository")
        void createParkingZone_EV_ShouldPersist() {
            // Arrange
            ParkingZoneDto dto = new ParkingZoneDto("zone-ev", "EV", 0.9);

            // Act
            parkingZoneController.createParkingZone(dto);

            // Assert
            assertTrue(parkingZoneRepository.zoneExists("zone-ev"));
            ParkingZone zone = parkingZoneRepository.findById("zone-ev");
            assertEquals(ZoneType.EV, zone.getZoneType());
            assertEquals(0.9, zone.getMaxOccupancyThreshold(), 0.001);
        }

        @Test
        @DisplayName("Should create VIP parking zone and persist to repository")
        void createParkingZone_VIP_ShouldPersist() {
            // Arrange
            ParkingZoneDto dto = new ParkingZoneDto("zone-vip", "VIP", 0.7);

            // Act
            parkingZoneController.createParkingZone(dto);

            // Assert
            assertTrue(parkingZoneRepository.zoneExists("zone-vip"));
            ParkingZone zone = parkingZoneRepository.findById("zone-vip");
            assertEquals(ZoneType.VIP, zone.getZoneType());
            assertEquals(0.7, zone.getMaxOccupancyThreshold(), 0.001);
        }

        @Test
        @DisplayName("Should throw exception when creating duplicate zone")
        void createParkingZone_DuplicateId_ShouldThrow() {
            // Arrange - Create first zone
            ParkingZoneDto dto1 = new ParkingZoneDto("zone-dup", "STANDARD", 0.8);
            parkingZoneController.createParkingZone(dto1);

            // Act & Assert - Attempt to create duplicate
            ParkingZoneDto dto2 = new ParkingZoneDto("zone-dup", "VIP", 0.9);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> parkingZoneController.createParkingZone(dto2)
            );
            assertEquals("A Parking Zone with this id already exists", exception.getMessage());
        }

        @Test
        @DisplayName("Should create zone with minimum occupancy threshold (0.0)")
        void createParkingZone_MinThreshold_ShouldPersist() {
            // Arrange
            ParkingZoneDto dto = new ParkingZoneDto("zone-min", "STANDARD", 0.0);

            // Act
            parkingZoneController.createParkingZone(dto);

            // Assert
            ParkingZone zone = parkingZoneRepository.findById("zone-min");
            assertEquals(0.0, zone.getMaxOccupancyThreshold(), 0.001);
        }

        @Test
        @DisplayName("Should create zone with maximum occupancy threshold (1.0)")
        void createParkingZone_MaxThreshold_ShouldPersist() {
            // Arrange
            ParkingZoneDto dto = new ParkingZoneDto("zone-max", "STANDARD", 1.0);

            // Act
            parkingZoneController.createParkingZone(dto);

            // Assert
            ParkingZone zone = parkingZoneRepository.findById("zone-max");
            assertEquals(1.0, zone.getMaxOccupancyThreshold(), 0.001);
        }

        @Test
        @DisplayName("Should throw exception for invalid zone type")
        void createParkingZone_InvalidZoneType_ShouldThrow() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ParkingZoneDto("zone-invalid", "INVALID_TYPE", 0.8)
            );
        }

        @Test
        @DisplayName("Should throw exception for negative occupancy threshold")
        void createParkingZone_NegativeThreshold_ShouldThrow() {
            // Arrange
            ParkingZoneDto dto = new ParkingZoneDto("zone-neg", "STANDARD", -0.1);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    parkingZoneController.createParkingZone(dto)
            );
        }

        @Test
        @DisplayName("Should throw exception for occupancy threshold greater than 1.0")
        void createParkingZone_ThresholdGreaterThanOne_ShouldThrow() {
            // Arrange
            ParkingZoneDto dto = new ParkingZoneDto("zone-over", "STANDARD", 1.1);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    parkingZoneController.createParkingZone(dto)
            );
        }

        @Test
        @DisplayName("Should handle case-insensitive zone type")
        void createParkingZone_LowerCaseZoneType_ShouldWork() {
            // Arrange
            ParkingZoneDto dto = new ParkingZoneDto("zone-lower", "standard", 0.8);

            // Act
            parkingZoneController.createParkingZone(dto);

            // Assert
            ParkingZone zone = parkingZoneRepository.findById("zone-lower");
            assertEquals(ZoneType.STANDARD, zone.getZoneType());
        }
    }

    // =========================================================================
    // addSpot() INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("addSpot() Integration Tests")
    class AddSpotTests {

        @BeforeEach
        void setUpZone() {
            // Create a zone to add spots to
            ParkingZoneDto zoneDto = new ParkingZoneDto("zone-1", "STANDARD", 0.8);
            parkingZoneController.createParkingZone(zoneDto);
        }

        @Test
        @DisplayName("Should add spot to existing zone")
        void addSpot_ToExistingZone_ShouldPersist() {
            // Arrange
            ParkingSpotDto spotDto = new ParkingSpotDto("spot-1", "zone-1");

            // Act
            parkingZoneController.addSpot(spotDto);

            // Assert
            ParkingZone zone = parkingZoneRepository.findById("zone-1");
            assertEquals(1, zone.getTotalSpots());
            assertTrue(parkingZoneRepository.spotExists("spot-1"));
        }

        @Test
        @DisplayName("Should add multiple spots to same zone")
        void addSpot_MultipleSpots_ShouldPersist() {
            // Arrange & Act
            parkingZoneController.addSpot(new ParkingSpotDto("spot-1", "zone-1"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-2", "zone-1"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-3", "zone-1"));

            // Assert
            ParkingZone zone = parkingZoneRepository.findById("zone-1");
            assertEquals(3, zone.getTotalSpots());
            assertTrue(parkingZoneRepository.spotExists("spot-1"));
            assertTrue(parkingZoneRepository.spotExists("spot-2"));
            assertTrue(parkingZoneRepository.spotExists("spot-3"));
        }

        @Test
        @DisplayName("Should create spot with FREE state by default")
        void addSpot_DefaultState_ShouldBeFree() {
            // Arrange
            ParkingSpotDto spotDto = new ParkingSpotDto("spot-free", "zone-1");

            // Act
            parkingZoneController.addSpot(spotDto);

            // Assert
            ParkingSpot spot = parkingZoneRepository.findSpotById("spot-free");
            assertNotNull(spot);
            assertEquals(SpotState.FREE, spot.getState());
            assertTrue(spot.isFree());
        }

        @Test
        @DisplayName("Should throw exception when adding duplicate spot")
        void addSpot_DuplicateSpotId_ShouldThrow() {
            // Arrange - Add first spot
            parkingZoneController.addSpot(new ParkingSpotDto("spot-dup", "zone-1"));

            // Act & Assert - Attempt to add duplicate
            ParkingSpotDto duplicateSpot = new ParkingSpotDto("spot-dup", "zone-1");
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> parkingZoneController.addSpot(duplicateSpot)
            );
            assertEquals("A Parking Spot with this id already exists", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when zone does not exist")
        void addSpot_NonExistentZone_ShouldThrow() {
            // Arrange
            ParkingSpotDto spotDto = new ParkingSpotDto("spot-orphan", "non-existent-zone");

            // Act & Assert
            assertThrows(Exception.class, () ->
                    parkingZoneController.addSpot(spotDto)
            );
        }

        @Test
        @DisplayName("Spot should reference correct zone")
        void addSpot_ShouldReferenceCorrectZone() {
            // Arrange
            ParkingSpotDto spotDto = new ParkingSpotDto("spot-ref", "zone-1");

            // Act
            parkingZoneController.addSpot(spotDto);

            // Assert
            ParkingSpot spot = parkingZoneRepository.findSpotById("spot-ref");
            assertNotNull(spot.getParkingZone());
            assertEquals("zone-1", spot.getParkingZone().getZoneId());
            assertEquals(ZoneType.STANDARD, spot.getParkingZone().getZoneType());
        }
    }

    // =========================================================================
    // REPOSITORY INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("Repository Integration Tests")
    class RepositoryIntegrationTests {

        @Test
        @DisplayName("Should create multiple zones and maintain isolation")
        void multipleZones_ShouldMaintainIsolation() {
            // Arrange & Act
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-a", "STANDARD", 0.8));
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-b", "EV", 0.9));
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-c", "VIP", 0.7));

            // Add spots to different zones
            parkingZoneController.addSpot(new ParkingSpotDto("spot-a1", "zone-a"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-a2", "zone-a"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-b1", "zone-b"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-c1", "zone-c"));

            // Assert - Each zone has correct spots
            ParkingZone zoneA = parkingZoneRepository.findById("zone-a");
            ParkingZone zoneB = parkingZoneRepository.findById("zone-b");
            ParkingZone zoneC = parkingZoneRepository.findById("zone-c");

            assertEquals(2, zoneA.getTotalSpots());
            assertEquals(1, zoneB.getTotalSpots());
            assertEquals(1, zoneC.getTotalSpots());
        }

        @Test
        @DisplayName("Should find all zones after creation")
        void findAllZones_ShouldReturnAllCreated() {
            // Arrange
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-1", "STANDARD", 0.8));
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-2", "EV", 0.9));
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-3", "VIP", 0.7));

            // Act
            List<ParkingZone> allZones = parkingZoneRepository.findAll();

            // Assert
            assertEquals(3, allZones.size());
        }

        @Test
        @DisplayName("Should correctly track spot existence across zones")
        void spotExists_AcrossZones_ShouldTrackCorrectly() {
            // Arrange
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-1", "STANDARD", 0.8));
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-2", "EV", 0.9));

            parkingZoneController.addSpot(new ParkingSpotDto("spot-1", "zone-1"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-2", "zone-2"));

            // Assert
            assertTrue(parkingZoneRepository.spotExists("spot-1"));
            assertTrue(parkingZoneRepository.spotExists("spot-2"));
            assertFalse(parkingZoneRepository.spotExists("spot-3"));
            assertFalse(parkingZoneRepository.spotExists("non-existent"));
        }

        @Test
        @DisplayName("Should find spot by ID across zones")
        void findSpotById_AcrossZones_ShouldFindCorrectly() {
            // Arrange
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-1", "STANDARD", 0.8));
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-2", "EV", 0.9));

            parkingZoneController.addSpot(new ParkingSpotDto("spot-x", "zone-1"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-y", "zone-2"));

            // Act & Assert
            ParkingSpot spotX = parkingZoneRepository.findSpotById("spot-x");
            ParkingSpot spotY = parkingZoneRepository.findSpotById("spot-y");

            assertNotNull(spotX);
            assertNotNull(spotY);
            assertEquals("zone-1", spotX.getParkingZone().getZoneId());
            assertEquals("zone-2", spotY.getParkingZone().getZoneId());
        }

        @Test
        @DisplayName("Should return null for non-existent spot")
        void findSpotById_NonExistent_ShouldReturnNull() {
            // Arrange
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-1", "STANDARD", 0.8));

            // Act
            ParkingSpot spot = parkingZoneRepository.findSpotById("non-existent");

            // Assert
            assertNull(spot);
        }
    }

    // =========================================================================
    // ZONE FUNCTIONALITY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Zone Functionality Tests")
    class ZoneFunctionalityTests {

        @Test
        @DisplayName("Zone should report hasFreeSpot correctly")
        void zone_HasFreeSpot_ShouldReportCorrectly() {
            // Arrange
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-1", "STANDARD", 0.8));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-1", "zone-1"));

            // Act
            ParkingZone zone = parkingZoneRepository.findById("zone-1");

            // Assert
            assertTrue(zone.hasFreeSpot());
        }

        @Test
        @DisplayName("Zone should return first free spot")
        void zone_GetFirstFreeSpot_ShouldReturnCorrectly() {
            // Arrange
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-1", "STANDARD", 0.8));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-1", "zone-1"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-2", "zone-1"));

            // Act
            ParkingZone zone = parkingZoneRepository.findById("zone-1");
            ParkingSpot freeSpot = zone.getFirstFreeSpot();

            // Assert
            assertNotNull(freeSpot);
            assertEquals("spot-1", freeSpot.getSpotId());
        }

        @Test
        @DisplayName("Zone should track free spots count correctly")
        void zone_FreeSpotsCount_ShouldTrackCorrectly() {
            // Arrange
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-1", "STANDARD", 0.8));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-1", "zone-1"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-2", "zone-1"));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-3", "zone-1"));

            ParkingZone zone = parkingZoneRepository.findById("zone-1");

            // Assert - All spots free initially
            assertEquals(3, zone.getFreeSpotsCount());

            // Occupy one spot
            ParkingSpot spot1 = parkingZoneRepository.findSpotById("spot-1");
            spot1.occupy();

            // Assert - Now 2 free
            assertEquals(2, zone.getFreeSpotsCount());
        }

        @Test
        @DisplayName("Zone should return null when no free spots available")
        void zone_GetFirstFreeSpot_WhenAllOccupied_ShouldReturnNull() {
            // Arrange
            parkingZoneController.createParkingZone(new ParkingZoneDto("zone-1", "STANDARD", 0.8));
            parkingZoneController.addSpot(new ParkingSpotDto("spot-1", "zone-1"));

            ParkingZone zone = parkingZoneRepository.findById("zone-1");
            ParkingSpot spot = parkingZoneRepository.findSpotById("spot-1");
            spot.occupy();

            // Act
            ParkingSpot freeSpot = zone.getFirstFreeSpot();

            // Assert
            assertNull(freeSpot);
            assertFalse(zone.hasFreeSpot());
        }
    }

    // =========================================================================
    // END-TO-END WORKFLOW TESTS
    // =========================================================================

    @Nested
    @DisplayName("End-to-End Workflow Tests")
    class EndToEndTests {

        @Test
        @DisplayName("Should complete full zone creation and spot management workflow")
        void fullWorkflow_CreateZoneAndManageSpots() {
            // 1. Create zone
            parkingZoneController.createParkingZone(new ParkingZoneDto("parking-a", "STANDARD", 0.85));

            // 2. Add multiple spots
            parkingZoneController.addSpot(new ParkingSpotDto("A-001", "parking-a"));
            parkingZoneController.addSpot(new ParkingSpotDto("A-002", "parking-a"));
            parkingZoneController.addSpot(new ParkingSpotDto("A-003", "parking-a"));

            // 3. Verify zone state
            ParkingZone zone = parkingZoneRepository.findById("parking-a");
            assertEquals(3, zone.getTotalSpots());
            assertEquals(3, zone.getFreeSpotsCount());
            assertTrue(zone.hasFreeSpot());

            // 4. Reserve a spot
            ParkingSpot spot1 = parkingZoneRepository.findSpotById("A-001");
            spot1.reserve();
            assertEquals(SpotState.RESERVED, spot1.getState());

            // 5. Occupy a spot
            ParkingSpot spot2 = parkingZoneRepository.findSpotById("A-002");
            spot2.occupy();
            assertEquals(SpotState.OCCUPIED, spot2.getState());

            // 6. Check free count again
            assertEquals(1, zone.getFreeSpotsCount());

            // 7. Get first free spot (should be A-003)
            ParkingSpot freeSpot = zone.getFirstFreeSpot();
            assertEquals("A-003", freeSpot.getSpotId());
        }

        @Test
        @DisplayName("Should support multiple zone types with spots")
        void multiZoneWorkflow_AllZoneTypes() {
            // Create zones of different types
            parkingZoneController.createParkingZone(new ParkingZoneDto("standard-zone", "STANDARD", 0.8));
            parkingZoneController.createParkingZone(new ParkingZoneDto("ev-zone", "EV", 0.9));
            parkingZoneController.createParkingZone(new ParkingZoneDto("vip-zone", "VIP", 0.7));

            // Add spots to each
            parkingZoneController.addSpot(new ParkingSpotDto("STD-1", "standard-zone"));
            parkingZoneController.addSpot(new ParkingSpotDto("STD-2", "standard-zone"));
            parkingZoneController.addSpot(new ParkingSpotDto("EV-1", "ev-zone"));
            parkingZoneController.addSpot(new ParkingSpotDto("VIP-1", "vip-zone"));
            parkingZoneController.addSpot(new ParkingSpotDto("VIP-2", "vip-zone"));

            // Verify
            assertEquals(2, parkingZoneRepository.findById("standard-zone").getTotalSpots());
            assertEquals(1, parkingZoneRepository.findById("ev-zone").getTotalSpots());
            assertEquals(2, parkingZoneRepository.findById("vip-zone").getTotalSpots());

            // Verify spot zone references
            assertEquals(ZoneType.STANDARD, parkingZoneRepository.findSpotById("STD-1").getParkingZone().getZoneType());
            assertEquals(ZoneType.EV, parkingZoneRepository.findSpotById("EV-1").getParkingZone().getZoneType());
            assertEquals(ZoneType.VIP, parkingZoneRepository.findSpotById("VIP-1").getParkingZone().getZoneType());
        }
    }
}
