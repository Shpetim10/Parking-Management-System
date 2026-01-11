package Service.impl;

import Service.TaxService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class DefaultTaxService implements TaxService {

    private static final int SCALE = 2;

    @Override
    public BigDecimal calculateTax(BigDecimal netAmount, BigDecimal taxRate) {
        Objects.requireNonNull(netAmount, "netAmount must not be null");
        Objects.requireNonNull(taxRate, "taxRate must not be null");

        if (netAmount.signum() < 0) {
            throw new IllegalArgumentException("netAmount must be >= 0");
        }
        if (taxRate.signum() < 0 || taxRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("taxRate must be between 0 and 1");
        }

        BigDecimal tax = netAmount.multiply(taxRate);
        return tax.setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateGross(BigDecimal netAmount, BigDecimal taxRate) {
        BigDecimal tax = calculateTax(netAmount, taxRate);
        return netAmount.add(tax).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
