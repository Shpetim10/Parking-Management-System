package Service;

import Model.DiscountInfo;

import java.math.BigDecimal;

public interface DiscountAndCapService {
    BigDecimal applyDiscountAndCaps(BigDecimal basePrice,
                                   DiscountInfo discountInfo,
                                   BigDecimal penalties,
                                   BigDecimal maxPriceCap);
}
