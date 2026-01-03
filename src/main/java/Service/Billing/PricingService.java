package Service.Billing;

import Model.DynamicPricingConfig;
import Model.Tariff;
import Enum.*;

import java.math.BigDecimal;

public interface PricingService {
    BigDecimal calculateBasePrice(double durationHours,
                                  ZoneType zoneType,
                                  DayType dayType,
                                  TimeOfDayBand timeOfDayBand,
                                  double occupancyRatio,   // 0.0–1.0
                                  Tariff tariff,
                                  DynamicPricingConfig config);
}
