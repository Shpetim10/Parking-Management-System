package UnitTesting.ShpetimShabanaj;

import Model.DynamicPricingConfig;
import Service.impl.DefaultPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PricingServiceHighOccupancySurgeTest {
    private DefaultPricingService pricingService;
    private DynamicPricingConfig mockConfig;

    @BeforeEach
    void setUp() {
        pricingService = new DefaultPricingService();
        mockConfig = mock(DynamicPricingConfig.class);

        // Define common threshold and multiplier for testing
        when(mockConfig.getHighOccupancyThreshold()).thenReturn(0.80);
        when(mockConfig.getHighOccupancyMultiplier()).thenReturn(1.5);
    }

    @Test
    @DisplayName("TC-01: No surge applied when occupancy (0.50) < threshold (0.80)")
    void testBelowThreshold() {
        BigDecimal basePrice = new BigDecimal("10.00");
        BigDecimal result = pricingService.applyHighOccupancySurge(basePrice, 0.50, mockConfig);

        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    @DisplayName("TC-02: Surge applied when occupancy (0.80) == threshold (0.80)")
    void testAtThreshold() {
        BigDecimal basePrice = new BigDecimal("10.00");
        BigDecimal result = pricingService.applyHighOccupancySurge(basePrice, 0.80, mockConfig);

        // 10.00 * 1.5 = 15.00
        assertEquals( new BigDecimal("15.00"),result);
    }

    @Test
    @DisplayName("TC-03: Surge applied when occupancy (0.90) > threshold (0.80)")
    void testAboveThreshold() {
        BigDecimal basePrice = new BigDecimal("20.00");
        BigDecimal result = pricingService.applyHighOccupancySurge(basePrice, 0.90, mockConfig);

        // 20.00 * 1.5 = 30.00
        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    @DisplayName("TC-04: Graceful handling of null price input")
    void testNullPrice() {
        BigDecimal result = pricingService.applyHighOccupancySurge(null, 0.90, mockConfig);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(2), result);
        assertEquals(2, result.scale());
    }

    @Test
    @DisplayName("TC-05: Handle invalid occupancy ratio (> 1.0) by returning zero")
    void testInvalidOccupancy() {
        BigDecimal basePrice = new BigDecimal("10.00");
        BigDecimal result = pricingService.applyHighOccupancySurge(basePrice, 1.1, mockConfig);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(2), result);
        assertEquals(2, result.scale());
    }

    @Test
    @DisplayName("TC-06: Handle negative ratio")
    void testNegativeOccupancy() {
        BigDecimal basePrice=new BigDecimal("10.00");
        BigDecimal result= pricingService.applyHighOccupancySurge(basePrice,-0.5, mockConfig);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(2), result);
        assertEquals(2, result.scale());
    }

    @Test
    @DisplayName("TC-07: Handle Null price configuration")
    void testNullPriceConfig() {
        BigDecimal basePrice=new BigDecimal("10.00");
        BigDecimal result= pricingService.applyHighOccupancySurge(basePrice,0.6, null);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(2), result);
        assertEquals(2, result.scale());
    }
}
