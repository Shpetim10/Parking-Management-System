package Artjol.SystemTesting;

import Dto.Billing.*;
import Dto.Penalty.ApplyPenaltyRequestDto;
import Dto.Session.*;
import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Enum.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ST2_OverstayPenaltySystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-2 Overstay includes penalty in billing")
    void overstayPenaltyIncluded() {

        SpotAssignmentResponseDto spot =
                system.zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                "U1",
                                ZoneType.STANDARD,
                                LocalDateTime.now()
                        )
                );

        StartSessionResponseDto session =
                system.sessionController.startSession(
                        new StartSessionRequestDto(
                                "U1",
                                "AA123BB",
                                spot.zoneId(),
                                spot.spotId(),
                                ZoneType.STANDARD,
                                false,
                                LocalDateTime.now().minusHours(5)
                        )
                );

        // Apply penalty
        system.penaltyController.applyPenalty(
                new ApplyPenaltyRequestDto(
                        "U1",
                        PenaltyType.OVERSTAY,
                        BigDecimal.valueOf(10),
                        LocalDateTime.now()
                )
        );

        BillingResponse bill =
                system.billingController.calculateBill(
                        new BillingRequest(
                                session.sessionId(),
                                ZoneType.STANDARD,
                                DayType.WEEKDAY,
                                TimeOfDayBand.OFF_PEAK,
                                0.4,
                                LocalDateTime.now(),
                                BigDecimal.valueOf(10),
                                3
                        )
                );

        assertEquals(
                BigDecimal.valueOf(10),
                bill.penaltiesTotal()
        );
    }
}