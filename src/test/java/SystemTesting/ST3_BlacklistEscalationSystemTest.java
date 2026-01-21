package SystemTesting;

import Dto.Penalty.*;
import Enum.*;
import Model.PenaltyHistory;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-3: Blacklist Escalation System Test
 *
 * Covers:
 * FR-11 Penalty accumulation
 * FR-2  Blacklist escalation logic
 * FR-13 Monitoring & penalty tracking
 * FR-1  User account enforcement
 */
class ST3_BlacklistEscalationSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-3 User is blacklisted after exceeding penalty threshold")
    void userIsBlacklistedAfterMultiplePenalties() {

        // ===================== GIVEN =====================
        String userId = "U1";

        // ===================== WHEN =====================
        ApplyPenaltyResponseDto p1 =
                system.penaltyController.applyPenalty(
                        new ApplyPenaltyRequestDto(
                                userId,
                                PenaltyType.OVERSTAY,
                                BigDecimal.valueOf(30),
                                LocalDateTime.now()
                        )
                );

        ApplyPenaltyResponseDto p2 =
                system.penaltyController.applyPenalty(
                        new ApplyPenaltyRequestDto(
                                userId,
                                PenaltyType.LOST_TICKET,
                                BigDecimal.valueOf(40),
                                LocalDateTime.now()
                        )
                );

        ApplyPenaltyResponseDto p3 =
                system.penaltyController.applyPenalty(
                        new ApplyPenaltyRequestDto(
                                userId,
                                PenaltyType.MISUSE,
                                BigDecimal.valueOf(50),
                                LocalDateTime.now()
                        )
                );

        // 4th penalty → exceeds MAX_PENALTIES_ALLOWED (3)
        ApplyPenaltyResponseDto p4 =
                system.penaltyController.applyPenalty(
                        new ApplyPenaltyRequestDto(
                                userId,
                                PenaltyType.OVERSTAY,
                                BigDecimal.valueOf(60),
                                LocalDateTime.now()
                        )
                );

        // ===================== THEN =====================
        assertEquals(
                BlacklistStatus.NONE,
                p1.blacklistStatus(),
                "User should not be blacklisted after first penalty"
        );

        assertEquals(
                BlacklistStatus.NONE,
                p2.blacklistStatus(),
                "User should not be blacklisted after second penalty"
        );

        assertEquals(
                BlacklistStatus.NONE,
                p3.blacklistStatus(),
                "User should not be blacklisted at threshold"
        );

        assertEquals(
                BlacklistStatus.BLACKLISTED,
                p4.blacklistStatus(),
                "User must be blacklisted after exceeding penalty threshold"
        );

        // ===================== AND =====================
        PenaltyHistory history =
                system.penaltyRepo.findById(userId);

        assertEquals(
                4,
                history.getPenaltyCount(),
                "Penalty history must include all penalties"
        );

        assertEquals(
                BigDecimal.valueOf(180),
                history.getTotalPenaltyAmount(),
                "Total penalty amount must be accumulated correctly"
        );
    }
}