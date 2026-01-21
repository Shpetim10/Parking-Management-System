package Artjol.SystemTesting;

import Dto.Eligibility.EligibilityRequestDto;
import Dto.Eligibility.EligibilityResponseDto;
import Dto.Session.StartSessionRequestDto;
import Dto.Zone.SpotAssignmentRequestDto;
import Enum.*;
import Model.ParkingSession;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-11: Eligibility denied when unpaid session exists
 *
 * Covers:
 * FR-7  Eligibility to start session
 * FR-9  Billing enforcement (unpaid session check)
 * FR-1  User account enforcement
 */
class ST11_EligibilityDeniedUnpaidSessionSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-11 Eligibility denied when unpaid session exists")
    void eligibilityDeniedForUnpaidSession() {

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

        // Mark session as unpaid
        ParkingSession unpaidSession =
                system.sessionRepo.findById(session.sessionId()).orElseThrow();
        unpaidSession.setState(SessionState.PAYMENT_PENDING);
        system.sessionRepo.save(unpaidSession);

        // ===================== WHEN =====================
        EligibilityResponseDto response =
                system.eligibilityController.checkEligibility(
                        new EligibilityRequestDto(
                                "U1",
                                "AA123BB",
                                1,
                                1,
                                1,
                                2,
                                true,
                                LocalDateTime.now()
                        )
                );

        // ===================== THEN =====================
        assertFalse(
                response.allowed(),
                "A user with an unpaid (payment pending) session must not be eligible to start another session"
        );

        assertNotNull(
                response.reason(),
                "When eligibility is denied, the system must include a clear reason"
        );
    }
}