package Dto.Zone;

public class ParkingSpotDto {
    private String spotID;
    private String zoneId;

    public ParkingSpotDto(String spotID, String zoneId) {
        this.spotID = spotID;
        this.zoneId = zoneId;
    }

    public String getSpotID() {
        return spotID;
    }

    public void setSpotID(String spotID) {
        this.spotID = spotID;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}
