package Artjol.SystemTesting;

import Dto.Billing.*;
import Dto.Eligibility.*;
import Dto.Exit.*;
import Dto.Session.*;
import Dto.Zone.*;
import Enum.*;
import Model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-1: Happy Path System Test
 *
 * Covers:
 * FR-1  User ACTIVE
 * FR-3  Vehicle
 * FR-5  Zone & spot
 * FR-6  Session lifecycle
 * FR-7  Eligibility
 * FR-9  Billing
 * FR-12 Billing record
 * FR-14 Exit authorization
 */
class ST1_HappyPathSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-1 Happy Path – user parks, pays, and exits successfully")
    void happyPath_endToEndFlow() {

        // ===================== GIVEN =====================
        String userId = "U1";
        String plate = "AA123BB";
        LocalDateTime entryTime = LocalDateTime.now().minusHours(2);

        // ===================== WHEN =====================
        // Eligibility
        EligibilityResponseDto eligibility =
                system.eligibilityController.checkEligibility(
                        new EligibilityRequestDto(
                                userId,
                                plate,
                                0,
                                0,
                                0,
                                0,
                                false,
                                LocalDateTime.now()
                        )
                );

        // Spot assignment
        SpotAssignmentResponseDto spot =
                system.zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                userId,
                                ZoneType.STANDARD,
                                LocalDateTime.now()
                        )
                );

        // Start session
        StartSessionResponseDto session =
                system.sessionController.startSession(
                        new StartSessionRequestDto(
                                userId,
                                plate,
                                spot.zoneId(),
                                spot.spotId(),
                                ZoneType.STANDARD,
                                false,
                                entryTime
                        )
                );

        // Billing
        BillingResponse bill =
                system.billingController.calculateBill(
                        new BillingRequest(
                                session.sessionId(),
                                ZoneType.STANDARD,
                                DayType.WEEKDAY,
                                TimeOfDayBand.OFF_PEAK,
                                0.3,
                                LocalDateTime.now(),
                                BigDecimal.ZERO,
                                24
                        )
                );

        // Exit
        ExitAuthorizationResponseDto exit =
                system.exitController.authorizeExit(
                        new ExitAuthorizationRequestDto(
                                userId,
                                session.sessionId(),
                                plate
                        )
                );

        // ===================== THEN =====================
        assertTrue(eligibility.allowed(), "Eligibility should be allowed");
        assertNotNull(spot, "Spot should be assigned");
        assertNotNull(session.sessionId(), "Session must exist");

        assertTrue(bill.finalPrice().compareTo(BigDecimal.ZERO) > 0,
                "Final price must be positive");

        assertTrue(exit.allowed(), "Exit must be allowed");
        assertEquals(ExitFailureReason.NONE, exit.reason());

        ParkingSession stored =
                system.sessionRepo.findById(session.sessionId()).orElseThrow();

        assertEquals(SessionState.CLOSED, stored.getState(),
                "Session must be CLOSED after exit");
    }
}