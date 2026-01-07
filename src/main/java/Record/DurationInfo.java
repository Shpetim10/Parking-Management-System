package Record;

public record DurationInfo(int hours, boolean exceededMax) {
    public DurationInfo {
        if (hours < 0) {
            throw new IllegalArgumentException("hours must not be negative");
        }
    }
}
