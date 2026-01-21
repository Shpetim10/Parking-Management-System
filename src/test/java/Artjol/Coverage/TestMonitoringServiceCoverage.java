package Artjol.Coverage;

import Enum.BlacklistStatus;
import Enum.PenaltyType;
import Model.Penalty;
import Model.PenaltyHistory;
import Service.MonitoringService;
import Service.impl.MonitoringServiceImpl;
import Settings.Settings;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TestMonitoringServiceCoverage {

    private final MonitoringService monitoringService =
            new MonitoringServiceImpl();

    // MC/DC-1
    @Test
    void testFirstPenaltyDoesNotBlacklistUser() {
        PenaltyHistory history = new PenaltyHistory();

        Penalty penalty = new Penalty(
                PenaltyType.OVERSTAY,
                BigDecimal.TEN,
                LocalDateTime.now()
        );

        BlacklistStatus status =
                monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                        "U1",
                        penalty,
                        history
                );

        assertEquals(BlacklistStatus.NONE, status);
        assertEquals(1, history.getPenaltyCount());
    }

    // MC/DC-2
    @Test
    void testPenaltiesBelowThresholdDoNotBlacklist() {
        PenaltyHistory history = new PenaltyHistory();

        // strictly BELOW threshold
        for (int i = 0; i < Settings.MAX_PENALTIES_ALLOWED - 1; i++) {
            history.addPenalty(new Penalty(
                    PenaltyType.OVERSTAY,
                    BigDecimal.TEN,
                    LocalDateTime.now().minusDays(1)
            ));
        }

        Penalty newPenalty = new Penalty(
                PenaltyType.LOST_TICKET,
                BigDecimal.TEN,
                LocalDateTime.now()
        );

        BlacklistStatus status =
                monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                        "U1",
                        newPenalty,
                        history
                );

        assertEquals(BlacklistStatus.NONE, status);
    }
    // MC/DC-3
    @Test
    void testPenaltiesExceedingThresholdTriggersBlacklistCandidate() {
        PenaltyHistory history = new PenaltyHistory();

        for (int i = 0; i < Settings.MAX_PENALTIES_ALLOWED; i++) {
            history.addPenalty(new Penalty(
                    PenaltyType.OVERSTAY,
                    BigDecimal.TEN,
                    LocalDateTime.now()
            ));
        }

        Penalty newPenalty = new Penalty(
                PenaltyType.MISUSE,
                BigDecimal.TEN,
                LocalDateTime.now()
        );

        BlacklistStatus status =
                monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                        "U1",
                        newPenalty,
                        history
                );

        assertEquals(
                BlacklistStatus.BLACKLISTED,
                status
        );
    }

    // MC/DC-4
    @Test
    void testOldPenaltiesOutsideWindowAreIgnored() {
        PenaltyHistory history = new PenaltyHistory();

        // penalties older than blacklist window
        for (int i = 0; i < Settings.MAX_PENALTIES_ALLOWED + 2; i++) {
            history.addPenalty(new Penalty(
                    PenaltyType.OVERSTAY,
                    BigDecimal.TEN,
                    LocalDateTime.now()
                            .minus(Settings.BLACKLIST_WINDOW)
                            .minusDays(1)
            ));
        }

        Penalty newPenalty = new Penalty(
                PenaltyType.LOST_TICKET,
                BigDecimal.TEN,
                LocalDateTime.now()
        );

        BlacklistStatus status =
                monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                        "U1",
                        newPenalty,
                        history
                );

        assertEquals(BlacklistStatus.NONE, status);
    }

    // MC/DC-5
    @Test
    void testOnlyRecentPenaltiesAreCountedForBlacklist() {
        PenaltyHistory history = new PenaltyHistory();

        // old penalties (ignored)
        history.addPenalty(new Penalty(
                PenaltyType.OVERSTAY,
                BigDecimal.TEN,
                LocalDateTime.now().minus(Settings.BLACKLIST_WINDOW).minusDays(1)
        ));

        // recent penalties (counted)
        for (int i = 0; i < Settings.MAX_PENALTIES_ALLOWED; i++) {
            history.addPenalty(new Penalty(
                    PenaltyType.OVERSTAY,
                    BigDecimal.TEN,
                    LocalDateTime.now()
            ));
        }

        Penalty newPenalty = new Penalty(
                PenaltyType.MISUSE,
                BigDecimal.TEN,
                LocalDateTime.now()
        );

        BlacklistStatus status =
                monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                        "U1",
                        newPenalty,
                        history
                );

        assertEquals(
                BlacklistStatus.BLACKLISTED,
                status
        );
    }
}