package Dto.Zone;
import Enum.ZoneType;

public class ParkingZoneDto {
    private String zoneId;
    private ZoneType zoneType;
    private double maxOccupancyThreshold;


    public ParkingZoneDto(String zoneId, String zoneType, double maxOccupancyThreshold) {
        this.zoneId = zoneId;
        this.zoneType = ZoneType.valueOf(zoneType);
        this.maxOccupancyThreshold = maxOccupancyThreshold;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public void setZoneType(ZoneType zoneType) {
        this.zoneType = zoneType;
    }

    public double getMaxOccupancyThreshold() {
        return maxOccupancyThreshold;
    }

    public void setMaxOccupancyThreshold(double maxOccupancyThreshold) {
        this.maxOccupancyThreshold = maxOccupancyThreshold;
    }
}
