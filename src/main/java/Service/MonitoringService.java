package Service;

import Enum.BlacklistStatus;
import Enum.ZoneType;
import Model.*;

import java.time.Duration;
import java.util.List;

public interface MonitoringService {

    BlacklistStatus updatePenaltyHistoryAndCheckBlacklist(
            String userId,
            Penalty newPenalty,
            PenaltyHistory history
    );

    void logEvent(LogEvent event);

    PenaltySummaryReport generatePenaltySummary(List<PenaltyHistory> histories);


    ZoneOccupancyReport generateZoneReport(
            ZoneType zoneType,
            double averageOccupancy,
            int totalReservations,
            int noShowReservations
    );
}