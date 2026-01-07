package Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PenaltyHistory {

    private final List<Penalty> penalties = new ArrayList<>();

    public void addPenalty(Penalty penalty) {
        if (penalty == null) {
            throw new IllegalArgumentException("Penalty cannot be null");
        }
        penalties.add(penalty);
    }

    public List<Penalty> getPenalties() {
        return Collections.unmodifiableList(penalties);
    }

    public BigDecimal getTotalPenaltyAmount() {
        return penalties.stream()
                .map(Penalty::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return penalties.isEmpty();
    }

    public int getPenaltyCount() {
        return penalties.size();
    }
}