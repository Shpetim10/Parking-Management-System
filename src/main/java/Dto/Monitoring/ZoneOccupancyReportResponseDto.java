package Dto.Monitoring;

import Enum.ZoneType;

public record ZoneOccupancyReportResponseDto(
        ZoneType zoneType,
        double averageOccupancy,
        int totalReservations,
        int noShowReservations
) { }