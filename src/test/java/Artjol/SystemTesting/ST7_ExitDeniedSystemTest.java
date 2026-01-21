package Artjol.SystemTesting;

import Dto.Exit.*;
import Enum.ExitFailureReason;
import Enum.ZoneType;
import Model.ParkingSession;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-7: Exit Denied System Test
 *
 * Covers:
 * FR-14 Exit authorization
 * FR-6  Session lifecycle integrity
 * FR-2  Enforcement rules
 */
class ST7_ExitDeniedSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-7 Exit is denied when plate at gate does not match session vehicle")
    void exitDeniedDueToPlateMismatch() {

        // GIVEN
        ParkingSession session =
                system.createActiveSession(
                        "U1",
                        "AA123BB",
                        ZoneType.STANDARD
                );

        // IMPORTANT: make session payable
                session.markPaid();
                system.sessionRepo.save(session);

        // WHEN
                ExitAuthorizationResponseDto response =
                        system.exitController.authorizeExit(
                                new ExitAuthorizationRequestDto(
                                        "U1",
                                        session.getId(),
                                        "WRONG-PLATE"
                                )
                        );

        // ===================== THEN =====================
        assertFalse(
                response.allowed(),
                "Exit must be denied when vehicle plate does not match"
        );

        assertEquals(
                ExitFailureReason.VEHICLE_MISMATCH,
                response.reason(),
                "Exit must fail due to vehicle mismatch"
        );

        // Session must remain active
        ParkingSession persisted =
                system.sessionRepo.findById(session.getId()).orElseThrow();

        assertTrue(
                persisted.isActive(),
                "Session must remain active after denied exit"
        );
    }
}