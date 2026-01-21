package Artjol.SystemTesting;

import Dto.Monitoring.PenaltySummaryResponseDto;
import Dto.Penalty.ApplyPenaltyRequestDto;
import Enum.PenaltyType;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import Enum.UserStatus;

/**
 * ST-8: Monitoring & Penalty Summary System Test
 *
 * Covers:
 * FR-13 Monitoring & reports
 * FR-11 Penalty aggregation
 */
class ST8_MonitoringSummarySystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();

        system.userRepo.save(new Model.User("U2", UserStatus.ACTIVE));
        system.userRepo.save(new Model.User("U3", UserStatus.ACTIVE));
    }

    @Test
    @DisplayName("ST-8 Penalty summary aggregates penalties correctly")
    void penaltySummaryAggregatesCorrectly() {

        // ===================== GIVEN =====================
        system.penaltyController.applyPenalty(
                new ApplyPenaltyRequestDto(
                        "U1",
                        PenaltyType.OVERSTAY,
                        BigDecimal.valueOf(20),
                        LocalDateTime.now()
                )
        );

        system.penaltyController.applyPenalty(
                new ApplyPenaltyRequestDto(
                        "U2",
                        PenaltyType.LOST_TICKET,
                        BigDecimal.valueOf(40),
                        LocalDateTime.now()
                )
        );

        system.penaltyController.applyPenalty(
                new ApplyPenaltyRequestDto(
                        "U3",
                        PenaltyType.MISUSE,
                        BigDecimal.valueOf(30),
                        LocalDateTime.now()
                )
        );

        system.penaltyController.applyPenalty(
                new ApplyPenaltyRequestDto(
                        "U1",
                        PenaltyType.OVERSTAY,
                        BigDecimal.valueOf(10),
                        LocalDateTime.now()
                )
        );

        // ===================== WHEN =====================
        PenaltySummaryResponseDto summary =
                system.monitoringController.generatePenaltySummary();

        // ===================== THEN =====================
        assertEquals(
                BigDecimal.valueOf(30),
                summary.totalOverstay()
        );

        assertEquals(
                BigDecimal.valueOf(40),
                summary.totalLostTicket()
        );

        assertEquals(
                BigDecimal.valueOf(30),
                summary.totalMisuse()
        );
    }
}