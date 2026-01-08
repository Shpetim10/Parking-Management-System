package Model;

import Enum.SessionState;
import java.time.Instant;
import java.util.Objects;

public class ParkingSession {

    private final String id;
    private final String userId;
    private final String vehiclePlate;
    private final Instant startTime;
    private SessionState state;

    public ParkingSession(String id, String userId, String vehiclePlate, Instant startTime) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.vehiclePlate = Objects.requireNonNull(vehiclePlate);
        this.startTime = Objects.requireNonNull(startTime);
        this.state = SessionState.OPEN;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = Objects.requireNonNull(state);
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public String getId() {
        return id;
    }

    public Object getUserId() {
        return userId;
    }
}
