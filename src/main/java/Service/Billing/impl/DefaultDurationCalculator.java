package Service.Billing.impl;

import Service.Billing.DurationCalculator;

import Record.DurationInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class DefaultDurationCalculator implements DurationCalculator {
    @Override
    public DurationInfo calculateDuration(LocalDateTime entryTime,
                                          LocalDateTime exitTime,
                                          int maxDurationHours) {

        Objects.requireNonNull(entryTime, "entryTime must not be null");
        Objects.requireNonNull(exitTime, "exitTime must not be null");

        if (maxDurationHours <= 0) {
            throw new IllegalArgumentException("maxDurationHours must be > 0");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("exitTime must not be before entryTime");
        }

        long minutes = Duration.between(entryTime, exitTime).toMinutes();

        // No time parked -> 0 hours, no overstay.
        if (minutes == 0) {
            return new DurationInfo(0, false);
        }

        // Ceiling to whole hours: (minutes + 59) / 60
        int hours = (int) ((minutes + 59) / 60);

        boolean exceededMax = hours > maxDurationHours;

        return new DurationInfo(hours, exceededMax);
    }
}
