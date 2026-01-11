package Service;

import java.math.BigDecimal;

public interface TaxService {
    BigDecimal calculateTax(BigDecimal netAmount, BigDecimal taxRate);
    BigDecimal calculateGross(BigDecimal netAmount, BigDecimal taxRate);
}
