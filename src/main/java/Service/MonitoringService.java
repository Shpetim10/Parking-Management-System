package Service;

import Model.*;
import Enum.BlacklistStatus;

import java.time.Duration;
import java.util.List;

public interface MonitoringService {

    BlacklistStatus updatePenaltyHistoryAndCheckBlacklist(
            String userId,
            Penalty newPenalty,
            PenaltyHistory history,
            int maxPenaltiesAllowed,
            Duration window
    );

    void logEvent(LogEvent event);

    PenaltySummaryReport generatePenaltySummary(List<PenaltyHistory> histories);
}