package BVT;

import Service.TaxService;
import Service.impl.DefaultTaxService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CalculateTaxBVTest {
    // Test valid cases here
    @ParameterizedTest
    @CsvSource({
        "0.00, 0.50 , 0.00",
        "0.01, 0.50 , 0.01",
        "999999.99, 0.50 , 500000.00",
        "1000000.00, 0.50 , 500000.00",
        "500000.00, 0.00 , 0.00",
        "500000.00, 0.01 , 5000.00",
        "500000.00, 0.99 , 495000.00",
        "500000.00, 1.00 , 500000.00",
        "500000.00, 0.50 , 250000.00"
    })
    public void testCalculateTax(BigDecimal netAmount, BigDecimal taxRatio, BigDecimal expected){
        TaxService taxService=new DefaultTaxService();

        assertEquals(expected,taxService.calculateTax(netAmount, taxRatio));
    }

    // Text invalid cases here
    @Test
    public void testCalculateTaxWhenNetAmountNegative(){
        TaxService taxService=new DefaultTaxService();

        assertThrows(IllegalArgumentException.class, ()->{
            taxService.calculateTax(new BigDecimal("-0.01"), new BigDecimal("0.5"));
        });
    }

    @Test
    public void testCalculateTaxWhenTaxRatioNegative(){
        TaxService taxService=new DefaultTaxService();

        assertThrows(IllegalArgumentException.class, ()->{
            taxService.calculateTax(new BigDecimal("500000.00"), new BigDecimal("-0.01"));
        });
    }

    @Test
    public void testCalculateTaxWhenTaxRatioMoreThan1(){
        TaxService taxService=new DefaultTaxService();

        assertThrows(IllegalArgumentException.class, ()->{
            taxService.calculateTax(new BigDecimal("500000.00"), new BigDecimal("1.01"));
        });
    }

    // Since I am using Object numbers, test also for null as part of unit testing
    @Test
    public void testCalculateTaxWhenNetAmountIsNUll(){
        TaxService taxService=new DefaultTaxService();

        assertThrows(NullPointerException.class, ()->{
            taxService.calculateTax(null, new BigDecimal("1.01"));
        });
    }

    @Test
    public void testCalculateTaxWhenTaxRatioIsNUll(){
        TaxService taxService=new DefaultTaxService();

        assertThrows(NullPointerException.class, ()->{
            taxService.calculateTax(new BigDecimal("500000.00"), null);
        });
    }

}
