package Model;

import java.util.Objects;

public class DynamicPricingConfig {
    private final double peakHourMultiplier;
    private final double offPeakMultiplier;
    private final double highOccupancyThreshold;
    private final double highOccupancyMultiplier;

    public DynamicPricingConfig(double peakHourMultiplier,
                                double offPeakMultiplier,
                                double highOccupancyThreshold,
                                double highOccupancyMultiplier) {

        this.peakHourMultiplier = requirePositive(peakHourMultiplier, "peakHourMultiplier");
        this.offPeakMultiplier = requirePositive(offPeakMultiplier, "offPeakMultiplier");

        if (highOccupancyThreshold < 0.0 || highOccupancyThreshold > 1.0) {
            throw new IllegalArgumentException(
                    "highOccupancyThreshold must be between 0.0 and 1.0 inclusive");
        }
        this.highOccupancyThreshold = highOccupancyThreshold;

        this.highOccupancyMultiplier = requirePositive(highOccupancyMultiplier, "highOccupancyMultiplier");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DynamicPricingConfig that)) return false;
        return Double.compare(peakHourMultiplier, that.peakHourMultiplier) == 0 && Double.compare(offPeakMultiplier, that.offPeakMultiplier) == 0 && Double.compare(highOccupancyThreshold, that.highOccupancyThreshold) == 0 && Double.compare(highOccupancyMultiplier, that.highOccupancyMultiplier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(peakHourMultiplier, offPeakMultiplier, highOccupancyThreshold, highOccupancyMultiplier);
    }

    private static double requirePositive(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be a finite value > 0.0");
        }
        return value;
    }

    public double getPeakHourMultiplier() {
        return peakHourMultiplier;
    }

    public double getOffPeakMultiplier() {
        return offPeakMultiplier;
    }

    public double getHighOccupancyThreshold() {
        return highOccupancyThreshold;
    }

    public double getHighOccupancyMultiplier() {
        return highOccupancyMultiplier;
    }
}
