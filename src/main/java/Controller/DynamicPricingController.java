package Controller;

import Dto.DynamicPricingConfig.DynamicPricingConfigDto;
import Model.DynamicPricingConfig;
import Repository.DynamicPricingConfigRepository;

import java.util.Objects;

public class DynamicPricingController {
    private final DynamicPricingConfigRepository configRepository;

    public DynamicPricingController(DynamicPricingConfigRepository configRepository) {
        this.configRepository = Objects.requireNonNull(configRepository);
    }

    public DynamicPricingConfigDto getActiveConfig() {
        DynamicPricingConfig config = configRepository.getActiveConfig();
        return new DynamicPricingConfigDto(
                config.getPeakHourMultiplier(),
                config.getOffPeakMultiplier(),
                config.getHighOccupancyThreshold(),
                config.getHighOccupancyMultiplier()
        );
    }

    public void saveConfig(DynamicPricingConfigDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");
        DynamicPricingConfig config = new DynamicPricingConfig(
                dto.peakHourMultiplier(),
                dto.offPeakMultiplier(),
                dto.highOccupancyThreshold(),
                dto.highOccupancyMultiplier()
        );
        configRepository.save(config);
    }
}
