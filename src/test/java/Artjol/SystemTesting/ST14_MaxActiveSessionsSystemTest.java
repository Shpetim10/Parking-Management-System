package Artjol.SystemTesting;

import Dto.Eligibility.EligibilityRequestDto;
import Dto.Eligibility.EligibilityResponseDto;
import Dto.Session.StartSessionRequestDto;
import Dto.Zone.SpotAssignmentRequestDto;
import Enum.ZoneType;
import Model.Vehicle;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-14: Active Session Limit Enforcement System Test
 *
 * Covers:
 * FR-7  Eligibility checks
 * FR-1  User account enforcement
 * FR-6  Session lifecycle integrity
 */
class ST14_MaxActiveSessionsSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();

        // ✅ Explicitly register vehicle used in this test
        system.vehicleRepo.save(new Vehicle("PLATE-1", "U1"));
    }

    @Test
    @DisplayName("ST-16 User cannot exceed max active sessions")
    void activeSessionLimitEnforced() {

        // ===================== GIVEN =====================
        var spot = system.zoneController.assignSpot(
                new SpotAssignmentRequestDto("U1", ZoneType.STANDARD, LocalDateTime.now())
        );

        system.sessionController.startSession(
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
        EligibilityResponseDto eligibility =
                system.eligibilityController.checkEligibility(
                        new EligibilityRequestDto(
                                "U1",
                                "PLATE-1",
                                1,  // active sessions for vehicle
                                1,  // total active sessions for user
                                1,  // sessions today
                                1,  // hours used today
                                false,
                                LocalDateTime.now()
                        )
                );

        // ===================== THEN =====================
        assertFalse(
                eligibility.allowed(),
                "Eligibility should be denied once the user has reached the maximum number of active sessions"
        );

        assertNotNull(
                eligibility.reason(),
                "If the system denies eligibility, it should explain why"
        );
    }
}