package Service;

import java.time.LocalDateTime;
import Record.DurationInfo;

public interface DurationCalculator {
    DurationInfo calculateDuration(
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            int maxDurationHours
    );
}
