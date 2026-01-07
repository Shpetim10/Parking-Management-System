package Repository;

import Model.DynamicPricingConfig;

public interface DynamicPricingConfigRepository {
    DynamicPricingConfig getActiveConfig();
    void save(DynamicPricingConfig config);
}
