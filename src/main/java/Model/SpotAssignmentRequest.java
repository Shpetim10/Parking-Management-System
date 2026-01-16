package Model;

import Enum.ZoneType;
import java.time.LocalDateTime;

public class SpotAssignmentRequest {

    private final String userId;
    private final ZoneType requestedZoneType;
    private final SubscriptionPlan subscriptionPlan;
    private final LocalDateTime requestedStartTime;

    public SpotAssignmentRequest(
            String userId,
            ZoneType requestedZoneType,
            SubscriptionPlan subscriptionPlan,
            LocalDateTime requestedStartTime
    ) {
        this.userId = userId;
        this.requestedZoneType = requestedZoneType;
        this.subscriptionPlan = subscriptionPlan;
        this.requestedStartTime = requestedStartTime;
    }

    public String getUserId() { return userId; }
    public ZoneType getRequestedZoneType() { return requestedZoneType; }
    public SubscriptionPlan getSubscriptionPlan() { return subscriptionPlan; }
    public LocalDateTime getRequestedStartTime() { return requestedStartTime; }
}