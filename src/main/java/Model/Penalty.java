package Model;

import Model.Enum.PenaltyType;

import java.math.BigDecimal;
import java.time.Instant;

public class Penalty {

    private final PenaltyType type;
    private final BigDecimal amount;
    private final Instant timestamp;

    public Penalty(PenaltyType type, BigDecimal amount, Instant timestamp) {
        if (type == null) {
            throw new IllegalArgumentException("Penalty type cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Penalty amount must be non-negative");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Penalty timestamp cannot be null");
        }

        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public PenaltyType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}