package SystemTesting;

import Dto.Billing.*;
import Dto.Exit.ExitAuthorizationRequestDto;
import Dto.Exit.ExitAuthorizationResponseDto;
import Dto.Session.StartSessionRequestDto;
import Dto.Zone.SpotAssignmentRequestDto;
import Enum.*;
import Model.ParkingSession;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-16: Session is closed after successful paid exit
 *
 * Covers:
 * FR-14 Exit authorization
 * FR-6  Session lifecycle management
 * FR-9  Billing confirmation before exit
 */
class ST16_SessionClosedAfterExitSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-16 Session is closed after successful paid exit")
    void sessionClosedAfterExit() {

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

        // Pay the session
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
        ExitAuthorizationResponseDto exit =
                system.exitController.authorizeExit(
                        new ExitAuthorizationRequestDto(
                                "U1",
                                session.sessionId(),
                                "PLATE-1"
                        )
                );

        // ===================== THEN =====================
        assertTrue(exit.allowed(), "Exit must be allowed after payment");

        ParkingSession closedSession =
                system.sessionRepo.findById(session.sessionId()).orElseThrow();

        assertEquals(
                SessionState.CLOSED,
                closedSession.getState(),
                "Session must be CLOSED after exit"
        );
    }
}