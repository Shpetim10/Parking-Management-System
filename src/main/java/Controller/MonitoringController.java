package Controller;

import Dto.Monitoring.LogEventDto;
import Dto.Monitoring.PenaltySummaryResponseDto;
import Dto.Monitoring.ZoneOccupancyReportResponseDto;
import Dto.Monitoring.ZoneReportRequestDto;
import Model.LogEvent;
import Model.ParkingZone;
import Model.PenaltyHistory;
import Model.PenaltySummaryReport;
import Model.ZoneOccupancyReport;
import Repository.ParkingZoneRepository;
import Repository.PenaltyHistoryRepository;
import Service.MonitoringService;

import java.util.List;
import java.util.Objects;

public class MonitoringController {

    private final MonitoringService monitoringService;
    private final PenaltyHistoryRepository penaltyHistoryRepository;
    private final ParkingZoneRepository parkingZoneRepository;

    public MonitoringController(
            MonitoringService monitoringService,
            PenaltyHistoryRepository penaltyHistoryRepository,
            ParkingZoneRepository parkingZoneRepository
    ) {
        this.monitoringService = Objects.requireNonNull(monitoringService);
        this.penaltyHistoryRepository = Objects.requireNonNull(penaltyHistoryRepository);
        this.parkingZoneRepository = Objects.requireNonNull(parkingZoneRepository);
    }

    public void logEvent(LogEventDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        LogEvent event = new LogEvent(dto.timestamp(), dto.type(), dto.details());
        monitoringService.logEvent(event);
    }

    public PenaltySummaryResponseDto generatePenaltySummary() {
        List<PenaltyHistory> histories = penaltyHistoryRepository.findAll();

        PenaltySummaryReport report = monitoringService.generatePenaltySummary(histories);

        return new PenaltySummaryResponseDto(
                report.getTotalOverstay(),
                report.getTotalLostTicket(),
                report.getTotalMisuse(),
                report.getBlacklistCandidatesCount()
        );
    }

    public ZoneOccupancyReportResponseDto generateZoneReport(ZoneReportRequestDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        ParkingZone zone = parkingZoneRepository.findById(dto.zoneId());

        double averageOccupancy = dto.totalTimeSlotsObserved() == 0
                ? 0.0
                : (double) dto.totalReservations() / (double) dto.totalTimeSlotsObserved();

        ZoneOccupancyReport report = monitoringService.generateZoneReport(
                zone.getZoneType(),
                averageOccupancy,
                dto.totalReservations(),
                dto.noShowReservations()
        );

        return new ZoneOccupancyReportResponseDto(
                report.getZoneType(),
                report.getAverageOccupancy(),
                report.getTotalReservations(),
                report.getNoShowReservations()
        );
    }
}