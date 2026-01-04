package Repository.impl;

import Model.DynamicPricingConfig;
import Repository.DynamicPricingConfigRepository;

import java.util.Objects;

public class InMemoryDynamicPricingConfigRepository implements DynamicPricingConfigRepository {
    private DynamicPricingConfig currentConfig;

    public InMemoryDynamicPricingConfigRepository(DynamicPricingConfig initialConfig) {
        this.currentConfig = Objects.requireNonNull(initialConfig, "initialConfig must not be null");
    }

    @Override
    public DynamicPricingConfig getActiveConfig() {
        return currentConfig;
    }

    @Override
    public void save(DynamicPricingConfig config) {
        this.currentConfig = Objects.requireNonNull(config, "config must not be null");
    }
}
