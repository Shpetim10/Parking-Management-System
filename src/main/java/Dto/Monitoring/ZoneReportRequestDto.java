package Dto.Monitoring;

public record ZoneReportRequestDto(
        String zoneId,
        int totalTimeSlotsObserved,
        int totalReservations,
        int noShowReservations
) { }