package Artjol.SystemTesting;

import Dto.Billing.*;
import Dto.Penalty.*;
import Dto.Session.*;
import Dto.Zone.SpotAssignmentRequestDto;
import Enum.*;
import Model.ParkingSession;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-2: Overstay Penalty System Test
 *
 * Covers:
 * FR-11 Penalty application (OVERSTAY)
 * FR-10 Billing includes penalties
 * FR-6  Session lifecycle
 * FR-9  Billing calculation
 * FR-13 Monitoring consistency
 */
class ST2_OverstayPenaltySystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-2 Overstay penalty is applied and included in billing")
    void overstayPenaltyIncludedInBilling() {

        // ===================== GIVEN =====================
        String userId = "U1";
        String plate = "AA123BB";
        LocalDateTime entryTime = LocalDateTime.now().minusHours(5);

        // Assign spot
        var spot = system.zoneController.assignSpot(
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

        // ===================== WHEN =====================
        // Apply overstay penalty
        ApplyPenaltyResponseDto penaltyResponse =
                system.penaltyController.applyPenalty(
                        new ApplyPenaltyRequestDto(
                                userId,
                                PenaltyType.OVERSTAY,
                                BigDecimal.valueOf(50),
                                LocalDateTime.now()
                        )
                );

        // Calculate billing AFTER penalty
        BillingResponse bill =
                system.billingController.calculateBill(
                        new BillingRequest(
                                session.sessionId(),
                                ZoneType.STANDARD,
                                DayType.WEEKDAY,
                                TimeOfDayBand.OFF_PEAK,
                                0.4,
                                LocalDateTime.now(),
                                BigDecimal.ZERO,
                                24
                        )
                );

        // ===================== THEN =====================
        assertEquals(
                BlacklistStatus.NONE,
                penaltyResponse.blacklistStatus(),
                "User should not be blacklisted yet"
        );

        assertEquals(
                BigDecimal.valueOf(50),
                bill.penaltiesTotal(),
                "Billing must include overstay penalty"
        );

        assertTrue(
                bill.finalPrice().compareTo(bill.netPrice()) > 0,
                "Final price must include penalty"
        );

        ParkingSession stored =
                system.sessionRepo.findById(session.sessionId()).orElseThrow();

        assertEquals(
                SessionState.PAID,
                stored.getState(),
                "Session should be PAID after billing"
        );
    }
}