package Artjol.SystemTesting;

import Dto.Billing.*;
import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Session.StartSessionRequestDto;
import Enum.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-12: Daily Price Cap System Test
 *
 * Covers:
 * FR-9  Billing calculation
 * FR-10 Daily price cap enforcement
 * FR-13 System-derived billing rules
 */
class ST12_DailyCapSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-13 Daily cap limits base price")
    void dailyCapApplied() {

        // ===================== GIVEN =====================
        var spot = system.zoneController.assignSpot(
                new SpotAssignmentRequestDto("U1", ZoneType.STANDARD, LocalDateTime.now())
        );
        assertNotNull(spot);

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
        BillingResponse bill =
                system.billingController.calculateBill(
                        new BillingRequest(
                                session.sessionId(),
                                ZoneType.STANDARD,
                                DayType.WEEKDAY,
                                TimeOfDayBand.OFF_PEAK,
                                0.5,
                                LocalDateTime.now().plusHours(20), // very long session
                                BigDecimal.ZERO,
                                24
                        )
                );

        // ===================== THEN =====================
        assertTrue(
                bill.basePrice().compareTo(BigDecimal.valueOf(25)) <= 0,
                "Base price must not exceed daily cap"
        );
    }
}