package Artjol.SystemTesting;


import Dto.Zone.*;
import Enum.*;
import Model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ST5_NoFreeSpotsSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();

        // Occupy all spots
        ParkingZone zone = system.zoneRepo.findById("Z1");
        zone.getSpots().forEach(ParkingSpot::occupy);
    }

    @Test
    @DisplayName("ST-5 No free spots prevents assignment")
    void noFreeSpots() {

        SpotAssignmentResponseDto spot =
                system.zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                "U1",
                                ZoneType.STANDARD,
                                LocalDateTime.now()
                        )
                );

        assertNull(spot);
    }
}