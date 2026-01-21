package Artjol.SystemTesting;

import Dto.Exit.ExitAuthorizationRequestDto;
import Dto.Exit.ExitAuthorizationResponseDto;
import Dto.Session.StartSessionRequestDto;
import Dto.Zone.SpotAssignmentRequestDto;
import Enum.*;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-15: Exit denied when session is unpaid
 *
 * Covers:
 * FR-14 Exit authorization
 * FR-9  Billing enforcement (payment required before exit)
 * FR-6  Session lifecycle control
 */
class ST15_ExitDeniedUnpaidSessionSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-15 Exit is denied when session is unpaid")
    void exitDeniedWhenUnpaid() {

        // ===================== GIVEN =====================
        var spot = system.zoneController.assignSpot(
                new SpotAssignmentRequestDto("U1", ZoneType.STANDARD, LocalDateTime.now())
        );

        var session = system.sessionController.startSession(
                new StartSessionRequestDto(
                        "U1",
                        "PLATE-1",
                        spot.zoneId(),
                        spot.spotId(),
                        spot.zoneType(),
                        false,
                        LocalDateTime.now()
                )
        );

        // ===================== WHEN =====================
        ExitAuthorizationResponseDto exit =
                system.exitController.authorizeExit(
                        new ExitAuthorizationRequestDto(
                                "U1",
                                session.sessionId(),
                                "PLATE-1"
                        )
                );

        // ===================== THEN =====================
        assertFalse(exit.allowed(), "Exit must be denied if session is unpaid");
        assertEquals(
                ExitFailureReason.SESSION_NOT_PAID,
                exit.reason()
        );
    }
}