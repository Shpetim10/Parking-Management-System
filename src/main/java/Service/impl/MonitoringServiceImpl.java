package Service.impl;

import Enum.BlacklistStatus;
import Enum.ZoneType;
import Model.*;
import Service.MonitoringService;

import Settings.Settings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MonitoringServiceImpl implements MonitoringService {
    private final List<LogEvent> logs = new ArrayList<>();

    @Override
    public BlacklistStatus updatePenaltyHistoryAndCheckBlacklist(
            String userId,
            Penalty newPenalty,
            PenaltyHistory history
    ) {
        history.addPenalty(newPenalty);

        LocalDateTime cutoff = LocalDateTime.now().minus(Settings.BLACKLIST_WINDOW);

        long penaltiesInWindow = history.getPenalties().stream()
                .filter(p -> p.getTimestamp().isAfter(cutoff))
                .count();

        return penaltiesInWindow > Settings.MAX_PENALTIES_ALLOWED
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