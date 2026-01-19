package Artjol.UnitTesting;

import Enum.BlacklistStatus;
import Enum.PenaltyType;
import Model.Penalty;
import Model.PenaltyHistory;
import Service.MonitoringService;
import Service.impl.MonitoringServiceImpl;
import Settings.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Unit Tests for M-93: MonitoringService.updatePenaltyHistoryAndCheckBlacklist

class MonitoringServiceUpdatePenaltyHistoryTest {

    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        monitoringService = new MonitoringServiceImpl();
    }

    @Test
    @DisplayName("adds penalty to history")
    void testUpdate_AddsPenaltyToHistory() {
        PenaltyHistory history = new PenaltyHistory();
        Penalty penalty = new Penalty(PenaltyType.OVERSTAY, BigDecimal.TEN, LocalDateTime.now());

        monitoringService.updatePenaltyHistoryAndCheckBlacklist("user-1", penalty, history);

        assertEquals(1, history.getPenaltyCount());
        assertTrue(history.getPenalties().contains(penalty));
    }

    @Test
    @DisplayName("returns NONE for first penalty")
    void testUpdate_FirstPenalty() {
        PenaltyHistory history = new PenaltyHistory();
        Penalty penalty = new Penalty(PenaltyType.OVERSTAY, BigDecimal.TEN, LocalDateTime.now());

        BlacklistStatus status = monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                "user-1", penalty, history
        );

        assertEquals(BlacklistStatus.NONE, status);
    }

    @Test
    @DisplayName("returns CANDIDATE when exceeds threshold")
    void testUpdate_ExceedsThreshold() {
        PenaltyHistory history = new PenaltyHistory();

        // Add penalties up to threshold
        for (int i = 0; i <= Settings.MAX_PENALTIES_ALLOWED; i++) {
            history.addPenalty(new Penalty(
                    PenaltyType.OVERSTAY, BigDecimal.TEN, LocalDateTime.now()
            ));
        }

        Penalty newPenalty = new Penalty(PenaltyType.LOST_TICKET, BigDecimal.TEN, LocalDateTime.now());

        BlacklistStatus status = monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                "user-1", newPenalty, history
        );

        assertEquals(BlacklistStatus.CANDIDATE_FOR_BLACKLISTING, status);
    }
}