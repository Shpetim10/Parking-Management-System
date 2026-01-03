package Model;

import Enum.ZoneType;

import java.time.Instant;

public class SpotAssignmentRequest {

    private final String userId;
    private final ZoneType requestedZoneType;
    private final boolean hasEvRights;
    private final boolean hasVipRights;
    private final Instant requestedStartTime;

    public SpotAssignmentRequest(
            String userId,
            ZoneType requestedZoneType,
            boolean hasEvRights,
            boolean hasVipRights,
            Instant requestedStartTime
    ) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (requestedZoneType == null) {
            throw new IllegalArgumentException("Requested zone type cannot be null");
        }
        if (requestedStartTime == null) {
            throw new IllegalArgumentException("Requested start time cannot be null");
        }

        this.userId = userId;
        this.requestedZoneType = requestedZoneType;
        this.hasEvRights = hasEvRights;
        this.hasVipRights = hasVipRights;
        this.requestedStartTime = requestedStartTime;
    }

    public String getUserId() {
        return userId;
    }

    public ZoneType getRequestedZoneType() {
        return requestedZoneType;
    }

    public boolean hasEvRights() {
        return hasEvRights;
    }

    public boolean hasVipRights() {
        return hasVipRights;
    }

    public Instant getRequestedStartTime() {
        return requestedStartTime;
    }
}