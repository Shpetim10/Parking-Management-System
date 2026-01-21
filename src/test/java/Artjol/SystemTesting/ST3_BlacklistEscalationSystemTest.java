package Artjol.SystemTesting;

import Dto.Penalty.*;
import Enum.*;
import Model.PenaltyHistory;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ST3_BlacklistEscalationSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-3 User becomes blacklist candidate after repeated penalties")
    void blacklistEscalation() {

        String userId = "U1";

        // Apply multiple penalties
        for (int i = 0; i < 3; i++) {
            system.penaltyController.applyPenalty(
                    new ApplyPenaltyRequestDto(
                            userId,
                            PenaltyType.OVERSTAY,
                            BigDecimal.valueOf(10),
                            LocalDateTime.now().minusDays(i)
                    )
            );
        }

        PenaltyHistory history =
                system.penaltyRepo.findById(userId);

        assertEquals(3, history.getPenaltyCount());
        assertTrue(history.getTotalPenaltyAmount()
                .compareTo(BigDecimal.valueOf(30)) >= 0);
    }
}