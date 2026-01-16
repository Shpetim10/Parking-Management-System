package ECT_Decision_Table;

import Model.DynamicPricingConfig;
import Model.Tariff;
import Service.PricingService;
import Service.impl.DefaultPricingService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import Enum.TimeOfDayBand;
import Enum.DayType;
import Enum.ZoneType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCalculateBasePrice {
    @ParameterizedTest
    @CsvSource({
        "0, 0.5, OFF_PEAK, WEEKDAY, 0.00",
        "1, 0.2, PEAK, WEEKDAY, 300.00",
        "1, 0.9, OFF_PEAK, WEEKDAY, 280.00",
        "1, 0.2, OFF_PEAK, WEEKEND, 320.00",
        "1, 0.9, PEAK, WEEKDAY, 420.00",
        "1, 0.2, PEAK, HOLIDAY, 480.00",
        "3, 0.9, PEAK, WEEKEND, 2000.00",
    })
    public void testCalculateBasePriceValidRulesForPriceLessThanDailyCap(
            int duration, double occupancy, TimeOfDayBand band, DayType dayType, BigDecimal expected
    ){
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"),new BigDecimal("60.00"));
        DynamicPricingConfig config=new DynamicPricingConfig(1.5,0.8,1.4);
        PricingService pricingService=new DefaultPricingService();

        assertEquals(expected, pricingService.calculateBasePrice(
                duration,
                dayType,
                band,
                occupancy,
                tariff,
                config
        ));
    }
}
