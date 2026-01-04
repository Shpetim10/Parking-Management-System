package Model;

import java.math.BigDecimal;
import java.util.Objects;

public class BillingResult {
    private final BigDecimal basePrice;
    private final BigDecimal discountsTotal;
    private final BigDecimal penaltiesTotal;
    private final BigDecimal finalPriceRounded;

    public BillingResult(BigDecimal basePrice,
                         BigDecimal discountsTotal,
                         BigDecimal penaltiesTotal,
                         BigDecimal finalPriceRounded) {

        this.basePrice = requireNonNegative(basePrice, "basePrice");
        this.discountsTotal = requireNonNegative(discountsTotal, "discountsTotal");
        this.penaltiesTotal = requireNonNegative(penaltiesTotal, "penaltiesTotal");
        this.finalPriceRounded = requireNonNegative(finalPriceRounded, "finalPriceRounded");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BillingResult that)) return false;
        return Objects.equals(basePrice, that.basePrice) && Objects.equals(discountsTotal, that.discountsTotal) && Objects.equals(penaltiesTotal, that.penaltiesTotal) && Objects.equals(finalPriceRounded, that.finalPriceRounded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePrice, discountsTotal, penaltiesTotal, finalPriceRounded);
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        // we do not enforce scale here; DiscountAndCapService is responsible
        // for rounding finalPriceRounded to 2 decimal places.
        return value;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getDiscountsTotal() {
        return discountsTotal;
    }

    public BigDecimal getPenaltiesTotal() {
        return penaltiesTotal;
    }

    public BigDecimal getFinalPriceRounded() {
        return finalPriceRounded;
    }
}
