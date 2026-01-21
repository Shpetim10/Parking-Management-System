package SystemTesting;

import Dto.Billing.*;
import Dto.Exit.ExitAuthorizationRequestDto;
import Dto.Session.StartSessionRequestDto;
import Dto.Zone.SpotAssignmentRequestDto;
import Enum.*;
import Model.ParkingSpot;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-13: Parking spot is released after successful exit
 *
 * Covers:
 * FR-14 Exit authorization
 * FR-5  Parking spot state management
 * FR-6  Session lifecycle completion
 */
class ST13_SpotReleasedAfterExitSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-13 Parking spot is released after successful exit")
    void spotReleasedAfterExit() {

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

        // Pay the session (required before exit)
        system.billingController.calculateBill(
                new BillingRequest(
                        session.sessionId(),
                        ZoneType.STANDARD,
                        DayType.WEEKDAY,
                        TimeOfDayBand.OFF_PEAK,
                        0.5,
                        LocalDateTime.now().plusHours(2),
                        BigDecimal.ZERO,
                        24
                )
        );

        // ===================== WHEN =====================
        system.exitController.authorizeExit(
                new ExitAuthorizationRequestDto(
                        "U1",
                        session.sessionId(),
                        "PLATE-1"
                )
        );

        // ===================== THEN =====================
        ParkingSpot releasedSpot =
                system.zoneRepo.findById(spot.zoneId())
                        .getSpots()
                        .stream()
                        .filter(s -> s.getSpotId().equals(spot.spotId()))
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                SpotState.FREE,
                releasedSpot.getState(),
                "Parking spot must be FREE after exit"
        );
    }
}