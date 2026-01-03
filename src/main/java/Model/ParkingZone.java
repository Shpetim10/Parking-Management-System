package Model;

import Model.Enum.ZoneType;

import java.util.ArrayList;
import java.util.List;

public class ParkingZone {

    private final String zoneId;
    private final ZoneType zoneType;
    private final double maxOccupancyThreshold; // 0.0 – 1.0
    private final List<ParkingSpot> spots = new ArrayList<>();

    public ParkingZone(String zoneId, ZoneType zoneType, double maxOccupancyThreshold) {
        if (zoneId == null || zoneId.isBlank()) {
            throw new IllegalArgumentException("Zone ID cannot be null or empty");
        }
        if (zoneType == null) {
            throw new IllegalArgumentException("Zone type cannot be null");
        }
        if (maxOccupancyThreshold < 0.0 || maxOccupancyThreshold > 1.0) {
            throw new IllegalArgumentException("Occupancy threshold must be between 0 and 1");
        }

        this.zoneId = zoneId;
        this.zoneType = zoneType;
        this.maxOccupancyThreshold = maxOccupancyThreshold;
    }

    public void addSpot(ParkingSpot spot) {
        if (spot == null) {
            throw new IllegalArgumentException("Parking spot cannot be null");
        }
        if (spot.getZoneType() != this.zoneType) {
            throw new IllegalArgumentException(
                    "Spot zone type does not match parking zone type"
            );
        }
        spots.add(spot);
    }

    public boolean hasFreeSpot() {
        return spots.stream().anyMatch(ParkingSpot::isFree);
    }

    public ParkingSpot getFirstFreeSpot() {
        return spots.stream()
                .filter(ParkingSpot::isFree)
                .findFirst()
                .orElse(null);
    }

    public String getZoneId() {
        return zoneId;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public double getMaxOccupancyThreshold() {
        return maxOccupancyThreshold;
    }

    public int getTotalSpots() {
        return spots.size();
    }

    public int getFreeSpotsCount() {
        return (int) spots.stream().filter(ParkingSpot::isFree).count();
    }
}