package Model;

import Enum.ZoneType;

import java.time.LocalDateTime;
import java.util.Objects;

public class BillingRecord {
    private final String sessionId;
    private final String userId;
    private final ZoneType zoneType;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final BillingResult billingResult;

    public BillingRecord(String sessionId,
                         String userId,
                         ZoneType zoneType,
                         LocalDateTime entryTime,
                         LocalDateTime exitTime,
                         BillingResult billingResult) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.zoneType = Objects.requireNonNull(zoneType, "zoneType must not be null");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime must not be null");
        this.exitTime = Objects.requireNonNull(exitTime, "exitTime must not be null");
        this.billingResult = Objects.requireNonNull(billingResult, "billingResult must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BillingRecord that)) return false;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(userId, that.userId) && zoneType == that.zoneType && Objects.equals(entryTime, that.entryTime) && Objects.equals(exitTime, that.exitTime) && Objects.equals(billingResult, that.billingResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, userId, zoneType, entryTime, exitTime, billingResult);
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public BillingResult getBillingResult() {
        return billingResult;
    }
}
