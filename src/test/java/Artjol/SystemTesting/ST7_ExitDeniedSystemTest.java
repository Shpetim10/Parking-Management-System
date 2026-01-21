package Artjol.SystemTesting;


import Dto.Exit.*;
import Dto.Session.*;
import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Enum.*;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ST7_ExitDeniedSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-7 Exit denied due to vehicle mismatch")
    void exitDeniedVehicleMismatch() {

        SpotAssignmentResponseDto spot =
                system.zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                "U1",
                                ZoneType.STANDARD,
                                LocalDateTime.now()
                        )
                );

        StartSessionResponseDto session =
                system.sessionController.startSession(
                        new StartSessionRequestDto(
                                "U1",
                                "AA123BB",
                                spot.zoneId(),
                                spot.spotId(),
                                ZoneType.STANDARD,
                                false,
                                LocalDateTime.now()
                        )
                );

        ExitAuthorizationResponseDto exit =
                system.exitController.authorizeExit(
                        new ExitAuthorizationRequestDto(
                                "U1",
                                session.sessionId(),
                                "ZZ999ZZ" // wrong plate
                        )
                );

        assertFalse(exit.allowed());
        assertEquals(ExitFailureReason.VEHICLE_MISMATCH, exit.reason());
    }
}
