package Dto.Billing;

import java.math.BigDecimal;

public record BillingResponse(
        String sessionId,
        String userId,
        BigDecimal basePrice,
        BigDecimal discountsTotal,
        BigDecimal penaltiesTotal,
        BigDecimal netPrice,
        BigDecimal taxAmount,
        BigDecimal finalPrice
) { }