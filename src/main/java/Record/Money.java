package Record;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        // normalize to 2 decimal places
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(double value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public Money plus(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        return new Money(this.amount.add(other.amount));
    }

    public Money minus(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.signum() < 0) {
            result = BigDecimal.ZERO;
        }
        return new Money(result);
    }

    public Money times(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor must not be null");
        if (factor.signum() < 0) {
            throw new IllegalArgumentException("factor must not be negative");
        }
        return new Money(this.amount.multiply(factor));
    }

    public Money times(double factor) {
        return times(BigDecimal.valueOf(factor));
    }
}
