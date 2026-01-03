package Model;

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
