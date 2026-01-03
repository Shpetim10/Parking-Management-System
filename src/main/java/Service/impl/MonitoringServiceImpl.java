package Service.impl;

import Model.*;
import Enum.BlacklistStatus;
import Service.MonitoringService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MonitoringServiceImpl implements MonitoringService {

    private final List<LogEvent> logs = new ArrayList<>();

    @Override
    public BlacklistStatus updatePenaltyHistoryAndCheckBlacklist(
            String userId,
            Penalty newPenalty,
            PenaltyHistory history,
            int maxPenaltiesAllowed,
            Duration window
    ) {

        if (userId == null || history == null || newPenalty == null || window == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        history.addPenalty(newPenalty);

        Instant cutoff = Instant.now().minus(window);

        long penaltiesInWindow = history.getPenalties().stream()
                .filter(p -> p.getTimestamp().isAfter(cutoff))
                .count();

        boolean exceedsThreshold = penaltiesInWindow > maxPenaltiesAllowed;

        return exceedsThreshold
                ? BlacklistStatus.CANDIDATE_FOR_BLACKLISTING
                : BlacklistStatus.NONE;
    }

    @Override
    public void logEvent(LogEvent event) {
        if (event != null) {
            logs.add(event);
        }
    }

    @Override
    public PenaltySummaryReport generatePenaltySummary(List<PenaltyHistory> histories) {

        if (histories == null) {
            throw new IllegalArgumentException("Histories cannot be null");
        }

        return PenaltySummaryReport.from(histories);
    }

    public List<LogEvent> getLogs() {
        return List.copyOf(logs);
    }
}