package Model;

public class EligibilityResult {

    private final boolean allowed;
    private final String reason;

    private EligibilityResult(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }

    public static EligibilityResult allowed() {
        return new EligibilityResult(true, null);
    }

    public static EligibilityResult denied(String reason) {
        return new EligibilityResult(false, reason);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getReason() {
        return reason;
    }
}
