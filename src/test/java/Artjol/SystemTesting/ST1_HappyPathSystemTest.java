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

class ST1_HappyPathSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-1 Happy Path – park, bill, exit successfully")
    void happyPath_endToEndFlow() {

        // ================= GIVEN =================
        String userId = "U1";
        String plate = "AA123BB";

        // ================= WHEN =================
        EligibilityResponseDto eligibility =
                system.eligibilityController.checkEligibility(
                        new EligibilityRequestDto(
                                userId,
                                plate,
                                0, 0, 0, 0,
                                false,
                                LocalDateTime.now()
                        )
                );

        assertTrue(eligibility.allowed());

        SpotAssignmentResponseDto spot =
                system.zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                userId,
                                ZoneType.STANDARD,
                                LocalDateTime.now()
                        )
                );

        assertNotNull(spot);

        StartSessionResponseDto session =
                system.sessionController.startSession(
                        new StartSessionRequestDto(
                                userId,
                                plate,
                                spot.zoneId(),
                                spot.spotId(),
                                ZoneType.STANDARD,
                                false,
                                LocalDateTime.now().minusHours(2)
                        )
                );

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

        ExitAuthorizationResponseDto exit =
                system.exitController.authorizeExit(
                        new ExitAuthorizationRequestDto(
                                userId,
                                session.sessionId(),
                                plate
                        )
                );

        // ================= THEN =================
        assertTrue(exit.allowed());
        assertEquals(ExitFailureReason.NONE, exit.reason());

        assertTrue(bill.finalPrice().compareTo(BigDecimal.ZERO) > 0);

        ParkingSession stored =
                system.sessionRepo.findById(session.sessionId()).orElseThrow();

        assertEquals(SessionState.CLOSED, stored.getState());
    }
}
