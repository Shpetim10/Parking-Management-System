package Model;

import Enum.SessionState;
import Enum.ZoneType;
import Enum.TimeOfDayBand;
import Enum.DayType;

import java.time.LocalDateTime;
import java.util.Objects;

public class ParkingSession {

    private final String id;
    private final String userId;
    private final String vehiclePlate;
    private final String zoneId;
    private final String spotId;
    private final TimeOfDayBand timeOfDayBand;
    private final DayType dayType;
    private final ZoneType zoneType;

    private final LocalDateTime startTime;
    private LocalDateTime endTime;

    private SessionState state;

    public ParkingSession(
            String id,
            String userId,
            String vehiclePlate,
            String zoneId,
            String spotId,
            TimeOfDayBand timeOfDayBand,
            DayType dayType,
            ZoneType zoneType,
            LocalDateTime startTime
    ) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.vehiclePlate = Objects.requireNonNull(vehiclePlate);
        this.zoneId = Objects.requireNonNull(zoneId);
        this.spotId = Objects.requireNonNull(spotId);
        this.zoneType = Objects.requireNonNull(zoneType);
        this.startTime = Objects.requireNonNull(startTime);
        this.timeOfDayBand = Objects.requireNonNull(timeOfDayBand);
        this.dayType = Objects.requireNonNull(dayType);
        this.state = SessionState.OPEN;
    }

    public void markPaid() {
        this.state = SessionState.PAID;
    }

    public void close(LocalDateTime endTime) {
        this.state = SessionState.CLOSED;
        this.endTime = endTime;
    }

    public boolean isActive() {
        return state == SessionState.OPEN || state == SessionState.PAID;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getSpotId() {
        return spotId;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }
    public TimeOfDayBand getTimeOfDayBand() {
        return timeOfDayBand;
    }

    public DayType getDayType() {
        return dayType;
    }
}