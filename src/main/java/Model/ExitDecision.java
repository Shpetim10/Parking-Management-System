package Model;

import Enum.ExitFailureReason;

public class ExitDecision {

    private final boolean allowed;
    private final ExitFailureReason reason;

    private ExitDecision(boolean allowed, ExitFailureReason reason) {
        this.allowed = allowed;
        this.reason = reason;
    }

    public static ExitDecision allow() {
        return new ExitDecision(true, ExitFailureReason.NONE);
    }

    public static ExitDecision deny(ExitFailureReason reason) {
        return new ExitDecision(false, reason);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public ExitFailureReason getReason() {
        return reason;
    }
}
