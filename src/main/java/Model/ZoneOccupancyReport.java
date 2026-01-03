package Model;

import Model.Enum.ZoneType;

public class ZoneOccupancyReport {

    private final ZoneType zoneType;
    private final double averageOccupancy;
    private final int totalReservations;
    private final int noShowReservations;

    public ZoneOccupancyReport(
            ZoneType zoneType,
            double averageOccupancy,
            int totalReservations,
            int noShowReservations
    ) {
        if (zoneType == null) {
            throw new IllegalArgumentException("Zone type cannot be null");
        }
        if (averageOccupancy < 0.0 || averageOccupancy > 1.0) {
            throw new IllegalArgumentException("Average occupancy must be between 0 and 1");
        }

        this.zoneType = zoneType;
        this.averageOccupancy = averageOccupancy;
        this.totalReservations = totalReservations;
        this.noShowReservations = noShowReservations;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public double getAverageOccupancy() {
        return averageOccupancy;
    }

    public int getTotalReservations() {
        return totalReservations;
    }

    public int getNoShowReservations() {
        return noShowReservations;
    }
}