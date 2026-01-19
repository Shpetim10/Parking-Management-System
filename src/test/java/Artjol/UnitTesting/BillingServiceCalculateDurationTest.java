package Artjol.UnitTesting;

import Record.DurationInfo;
import Service.BillingService;
import Service.impl.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

// Unit Tests for M-78: BillingService.calculateDuration

class BillingServiceCalculateDurationTest {

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new DefaultBillingService(
                new DefaultDurationCalculator(),
                new DefaultPricingService(),
                new DefaultDiscountAndCapService(),
                new DefaultTaxService()
        );
    }

    @Test
    @DisplayName("calculates zero duration for same entry and exit time")
    void testCalculateDuration_ZeroDuration() {
        LocalDateTime time = LocalDateTime.now();

        DurationInfo duration = billingService.calculateDuration(time, time, 24);

        assertNotNull(duration);
        assertEquals(0, duration.hours());
    }

    @Test
    @DisplayName("calculates duration for 1 hour")
    void testCalculateDuration_OneHour() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.plusHours(1);

        DurationInfo duration = billingService.calculateDuration(entry, exit, 24);

        assertEquals(1, duration.hours());
    }

    @Test
    @DisplayName("calculates duration for multiple hours")
    void testCalculateDuration_MultipleHours() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.plusHours(5);

        DurationInfo duration = billingService.calculateDuration(entry, exit, 24);

        assertEquals(5, duration.hours());
    }

    @Test
    @DisplayName("rounds up partial hours")
    void testCalculateDuration_RoundsUpPartialHours() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.plusHours(2).plusMinutes(30);

        DurationInfo duration = billingService.calculateDuration(entry, exit, 24);

        assertEquals(3, duration.hours());
    }

    @Test
    @DisplayName("handles duration with minutes")
    void testCalculateDuration_WithMinutes() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.plusHours(1).plusMinutes(1);

        DurationInfo duration = billingService.calculateDuration(entry, exit, 24);

        assertEquals(2, duration.hours());
    }
}