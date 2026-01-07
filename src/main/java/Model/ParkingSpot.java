package Model;

import Enum.SpotState;
import Enum.ZoneType;

public class ParkingSpot {

    private final String spotId;
    private final ZoneType zoneType;
    private SpotState state;

    public ParkingSpot(String spotId, ZoneType zoneType) {
        if (spotId == null || spotId.isBlank()) {
            throw new IllegalArgumentException("Spot ID cannot be null or empty");
        }
        if (zoneType == null) {
            throw new IllegalArgumentException("Zone type cannot be null");
        }

        this.spotId = spotId;
        this.zoneType = zoneType;
        this.state = SpotState.FREE;
    }


    public void reserve() {
        if (state != SpotState.FREE) {
            throw new IllegalStateException(
                    "Spot can only be reserved if it is FREE"
            );
        }
        this.state = SpotState.RESERVED;
    }


    public void occupy() {
        if (state == SpotState.OCCUPIED) {
            throw new IllegalStateException(
                    "Spot is already OCCUPIED"
            );
        }
        this.state = SpotState.OCCUPIED;
    }


    public void release() {
        if (state != SpotState.OCCUPIED) {
            throw new IllegalStateException(
                    "Spot can only be released if it is OCCUPIED"
            );
        }
        this.state = SpotState.FREE;
    }

    public String getSpotId() {
        return spotId;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public SpotState getState() {
        return state;
    }


    public boolean isFree() {
        return state == SpotState.FREE;
    }

    public boolean isOccupied() {
        return state == SpotState.OCCUPIED;
    }
}