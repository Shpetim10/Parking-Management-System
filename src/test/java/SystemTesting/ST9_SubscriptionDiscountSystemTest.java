package SystemTesting;

import Dto.Billing.*;
import Dto.Zone.*;
import Dto.Session.*;
import Enum.*;
import Model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-9: Subscription Discount System Test
 *
 * Covers:
 * FR-10 Discount application
 * FR-9  Billing calculation
 * FR-1  Subscription-based benefits
 */
class ST9_SubscriptionDiscountSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();

        // Ensure user has an active subscription with discount
        system.subscriptionRepo.save(
                "U1",
                SubscriptionPlan.defaultPlan()
        );
    }

    @Test
    @DisplayName("ST-9 Subscription discount is applied to billing")
    void subscriptionDiscountApplied() {

        // ===================== GIVEN =====================
        SpotAssignmentResponseDto spot =
                system.zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                "U1",
                                ZoneType.STANDARD,
                                LocalDateTime.now()
                        )
                );

        assertNotNull(spot, "Spot must be assigned");

        StartSessionResponseDto session =
                system.sessionController.startSession(
                        new StartSessionRequestDto(
                                "U1",
                                "AA123BB",
                                spot.zoneId(),
                                spot.spotId(),
                                spot.zoneType(),
                                false,
                                LocalDateTime.now()
                        )
                );

        // ===================== WHEN =====================
        BillingResponse bill =
                system.billingController.calculateBill(
                        new BillingRequest(
                                session.sessionId(),
                                ZoneType.STANDARD,
                                DayType.WEEKDAY,
                                TimeOfDayBand.OFF_PEAK,
                                0.5,
                                LocalDateTime.now().plusHours(2),
                                BigDecimal.ZERO,
                                24
                        )
                );

        // ===================== THEN =====================
        assertTrue(
                bill.discountsTotal().compareTo(BigDecimal.ZERO) > 0,
                "Subscription discount must be applied"
        );

// Final price must reflect discount (not equal to raw base + tax only)
        BigDecimal expectedWithoutDiscount =
                bill.basePrice().add(bill.taxAmount()).add(bill.penaltiesTotal());

        assertTrue(
                bill.finalPrice().compareTo(expectedWithoutDiscount) < 0,
                "Final price must reflect subscription discount"
        );
    }
}