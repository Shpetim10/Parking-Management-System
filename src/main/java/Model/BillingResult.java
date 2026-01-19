package Model;

import java.math.BigDecimal;
import java.util.Objects;

public  class BillingResult {

    private final BigDecimal basePrice;
    private final BigDecimal discountsTotal;
    private final BigDecimal penaltiesTotal;
    private final BigDecimal netPrice;
    private final BigDecimal taxAmount;
    private final BigDecimal finalPrice;

    public BillingResult(BigDecimal basePrice,
                         BigDecimal discountsTotal,
                         BigDecimal penaltiesTotal,
                         BigDecimal netPrice,
                         BigDecimal taxAmount,
                         BigDecimal finalPrice) {
        this.basePrice = Objects.requireNonNull(basePrice);
        this.discountsTotal = Objects.requireNonNull(discountsTotal);
        this.penaltiesTotal = Objects.requireNonNull(penaltiesTotal);
        this.netPrice = Objects.requireNonNull(netPrice);
        this.taxAmount = Objects.requireNonNull(taxAmount);
        this.finalPrice = Objects.requireNonNull(finalPrice);
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getDiscountsTotal() {
        return discountsTotal;
    }

    public BigDecimal getPenaltiesTotal() {
        return penaltiesTotal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getNetPrice() {
        return netPrice;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
}
