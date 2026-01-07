package Dto.DynamicPricingConfig;

public record DynamicPricingConfigDto(
        double peakHourMultiplier,
        double offPeakMultiplier,
        double highOccupancyThreshold,
        double highOccupancyMultiplier
) { }