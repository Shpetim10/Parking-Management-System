package Service;

import Model.*;
import Enum.*;
import Record.DurationInfo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BillingService {
    BillingResult calculateBill(LocalDateTime entryTime,
                                LocalDateTime exitTime,
                                ZoneType zoneType,
                                DayType dayType,
                                TimeOfDayBand timeOfDayBand,
                                double occupancyRatio,
                                Tariff tariff,
                                DynamicPricingConfig dynamicConfig,
                                DiscountInfo discountInfo,
                                BigDecimal penalties,
                                int maxDurationHours,
                                BigDecimal maxPriceCap,
                                BigDecimal taxRate
                                );

    DurationInfo calculateDuration(LocalDateTime entryTime,
                                   LocalDateTime exitTime,
                                   int maxDurationHours);
}
