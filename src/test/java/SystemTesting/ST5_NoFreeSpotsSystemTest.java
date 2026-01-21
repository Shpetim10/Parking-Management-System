package SystemTesting;

import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Enum.ZoneType;
import Model.ParkingSpot;
import Model.ParkingZone;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-5: No Free Spots System Test
 *
 * Covers:
 * FR-5  Zones & parking spots management
 * FR-3  Parking spot allocation
 * FR-8  Spot availability enforcement
 */
class ST5_NoFreeSpotsSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();

        /*
         * GIVEN:
         * A STANDARD zone exists with spots,
         * and ALL spots are already occupied
         */
        ParkingZone zone = system.zoneRepo.findById("Z1");
        zone.getSpots().forEach(ParkingSpot::occupy);

        // Persist state change
        system.zoneRepo.save(zone);
    }

    @Test
    @DisplayName("ST-5 No free spots prevents assignment")
    void noFreeSpotsPreventsAssignment() {

        // ===================== WHEN =====================
        SpotAssignmentResponseDto response =
                system.zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                "U1",
                                ZoneType.STANDARD,
                                LocalDateTime.now()
                        )
                );

        // ===================== THEN =====================
        assertNull(
                response,
                "Spot assignment must fail when no free spots are available"
        );
    }
}