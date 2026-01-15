package Dto.DynamicPricingConfig;

public record DynamicPricingConfigDto(
        double peakHourMultiplier,
        double highOccupancyThreshold,
        double highOccupancyMultiplier
) { }