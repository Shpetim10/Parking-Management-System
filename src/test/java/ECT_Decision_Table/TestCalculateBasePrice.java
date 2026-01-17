package ECT_Decision_Table;

import Model.DynamicPricingConfig;
import Model.Tariff;
import Service.PricingService;
import Service.impl.DefaultPricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import Enum.TimeOfDayBand;
import Enum.DayType;
import Enum.ZoneType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestCalculateBasePrice {
    // Normal valid test cases
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

    // Testing invalid cases
    @Test
    public void testWhenTariffIsNull(){
        DynamicPricingConfig  config=new DynamicPricingConfig(1.5,0.8,1.4);
        PricingService pricingService=new DefaultPricingService();

        assertThrows(NullPointerException.class,()->{
            pricingService.calculateBasePrice(
                    1,DayType.WEEKEND,TimeOfDayBand.OFF_PEAK,0.2,null,config
            );
        });
    }

    @Test
    public void testWhenDurationHoursNotValid(){
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"),new BigDecimal("60.00"));
        DynamicPricingConfig  config=new DynamicPricingConfig(1.5,0.8,1.4);
        PricingService pricingService=new DefaultPricingService();

        assertThrows(IllegalArgumentException.class,()->{
            pricingService.calculateBasePrice(
                    -5,DayType.WEEKEND,TimeOfDayBand.OFF_PEAK,1.2,tariff,config
            );
        });
    }

    // Extra tests not part of Decision Table
    @Test
    public void testWhenConfigIsNull(){
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"),new BigDecimal("60.00"));
        PricingService pricingService=new DefaultPricingService();

        assertThrows(NullPointerException.class,()->{
            pricingService.calculateBasePrice(
                    1,DayType.WEEKEND,TimeOfDayBand.OFF_PEAK,0.2,tariff,null
            );
        });
    }

    @Test
    public void testWhenDayTypeIsNull(){
        DynamicPricingConfig  config=new DynamicPricingConfig(1.5,0.8,1.4);
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"),new BigDecimal("60.00"));
        PricingService pricingService=new DefaultPricingService();

        assertThrows(NullPointerException.class,()->{
            pricingService.calculateBasePrice(
                    1,null,TimeOfDayBand.OFF_PEAK,0.2,tariff,config
            );
        });
    }

    @Test
    public void testWhenBandIsNull(){
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"),new BigDecimal("60.00"));
        DynamicPricingConfig  config=new DynamicPricingConfig(1.5,0.8,1.4);
        PricingService pricingService=new DefaultPricingService();

        assertThrows(NullPointerException.class,()->{
            pricingService.calculateBasePrice(
                    1,DayType.WEEKEND,null,0.2,tariff,config
            );
        });
    }

    @Test
    public void testWhenOccupancyGreaterThanOneNotValid(){
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"),new BigDecimal("60.00"));
        DynamicPricingConfig  config=new DynamicPricingConfig(1.5,0.8,1.4);
        PricingService pricingService=new DefaultPricingService();

        assertThrows(IllegalArgumentException.class,()->{
            pricingService.calculateBasePrice(
                    1,DayType.WEEKEND,TimeOfDayBand.OFF_PEAK,1.2,tariff,config
            );
        });
    }

    @Test
    public void testWhenOccupancyNegativeNotValid() {
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"), new BigDecimal("60.00"));
        DynamicPricingConfig config = new DynamicPricingConfig(1.5, 0.8, 1.4);
        PricingService pricingService = new DefaultPricingService();

        assertThrows(IllegalArgumentException.class, () -> {
            pricingService.calculateBasePrice(
                    1, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, -0.1, tariff, config
            );
        });
    }

    @Test
    public void testWhenOccupancyOnThreshold() {
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"), new BigDecimal("60.00"));
        DynamicPricingConfig config = new DynamicPricingConfig(1.5, 0.8, 1.4);
        PricingService pricingService = new DefaultPricingService();

        assertEquals(new BigDecimal("280.00"), pricingService.calculateBasePrice(
                1, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.8, tariff, config
        ));
    }

    @Test
    public void testDailyCapMultipliesForMoreThanOneDay() {
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"), new BigDecimal("60.00"));
        DynamicPricingConfig config = new DynamicPricingConfig(1.5, 0.8, 1.4);
        PricingService pricingService = new DefaultPricingService();

        BigDecimal price = pricingService.calculateBasePrice(
                25, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.2, tariff, config
        );

        // base = 25*200 = 5000, daily cap = 2000 * ceil(25/24) = 4000 → price should be 4000
        assertEquals(new BigDecimal("4000.00"), price);
    }

    @Test
    public void testWhenDailyCapIsNullPriceNotCapped() {
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), null, new BigDecimal("60.00"));
        DynamicPricingConfig config = new DynamicPricingConfig(1.5, 0.8, 1.4);
        PricingService pricingService = new DefaultPricingService();

        BigDecimal price = pricingService.calculateBasePrice(
                10, DayType.WEEKDAY, TimeOfDayBand.OFF_PEAK, 0.2, tariff, config
        );

        assertEquals(new BigDecimal("2000.00"), price); // 10 * 200
    }

    @Test
    public void testWeekendWithZeroSurchargeDoesNotChangePrice() {
        Tariff tariff = new Tariff(ZoneType.STANDARD, new BigDecimal("200.00"), new BigDecimal("2000.00"), BigDecimal.ZERO);
        DynamicPricingConfig config = new DynamicPricingConfig(1.5, 0.8, 1.4);
        PricingService pricingService = new DefaultPricingService();

        BigDecimal price = pricingService.calculateBasePrice(
                1, DayType.WEEKEND, TimeOfDayBand.OFF_PEAK, 0.2, tariff, config
        );

        assertEquals(new BigDecimal("200.00"), price); // no 60% bump
    }
}
