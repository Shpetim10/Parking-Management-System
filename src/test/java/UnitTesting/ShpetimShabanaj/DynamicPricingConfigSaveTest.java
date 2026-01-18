package UnitTesting.ShpetimShabanaj;

import Model.DynamicPricingConfig;
import Repository.DynamicPricingConfigRepository;
import Repository.impl.InMemoryDynamicPricingConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class DynamicPricingConfigSaveTest {
    DynamicPricingConfigRepository dynamicPricingConfigRepository;
    DynamicPricingConfig dynamicPricingConfig;

    @BeforeEach
    void setUp() {
        dynamicPricingConfig=mock();
        dynamicPricingConfigRepository=new InMemoryDynamicPricingConfigRepository(dynamicPricingConfig);
    }

    //TC-01: Verify successful save of new config
    @Test
    @DisplayName("TC-01: Verify successful save of new config")
    void testSavingConfigSuccessfully(){
        DynamicPricingConfig config=mock(DynamicPricingConfig.class);

        dynamicPricingConfigRepository.save(config);

        assertEquals(config, dynamicPricingConfigRepository.getActiveConfig());
    }

    //TC-02: Verify fail when config is null
    @Test
    @DisplayName("TC-02: Verify fail when config is null")
    void testSavingConfigWhenConfigIsNull(){
        assertThrows(NullPointerException.class,
                () -> dynamicPricingConfigRepository.save(null));
    }

    //TC-03: Verify that it saves the latest config
    @Test
    @DisplayName("TC-03: Verify that it saves the latest config")
    void testSavingConfigTwoTimesAndItKeepsTheLatestConfig(){
        DynamicPricingConfig config1=mock(DynamicPricingConfig.class);
        DynamicPricingConfig config2=mock(DynamicPricingConfig.class);
        dynamicPricingConfigRepository.save(config1);
        dynamicPricingConfigRepository.save(config2);

        assertEquals(config2, dynamicPricingConfigRepository.getActiveConfig());
    }

}
