package Service.impl;

import Enum.BlacklistStatus;
import Enum.ZoneType;
import Model.*;
import Service.MonitoringService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MonitoringServiceImpl implements MonitoringService {

    // ✅ CENTRALIZED BUSINESS RULES
    private static final int MAX_PENALTIES_ALLOWED = 3;
    private static final Duration BLACKLIST_WINDOW = Duration.ofDays(30);

    private final List<LogEvent> logs = new ArrayList<>();

    @Override
    public BlacklistStatus updatePenaltyHistoryAndCheckBlacklist(
            String userId,
            Penalty newPenalty,
            PenaltyHistory history
    ) {
        history.addPenalty(newPenalty);

        Instant cutoff = Instant.now().minus(BLACKLIST_WINDOW);

        long penaltiesInWindow = history.getPenalties().stream()
                .filter(p -> p.getTimestamp().isAfter(cutoff))
                .count();

        return penaltiesInWindow > MAX_PENALTIES_ALLOWED
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
        return PenaltySummaryReport.from(histories);
    }

    @Override
    public ZoneOccupancyReport generateZoneReport(
            ZoneType zoneType,
            double averageOccupancy,
            int totalReservations,
            int noShowReservations
    ) {
        return new ZoneOccupancyReport(
                zoneType,
                averageOccupancy,
                totalReservations,
                noShowReservations
        );
    }
}