package SystemTesting;

import Dto.Billing.*;
import Dto.Session.*;
import Dto.Zone.SpotAssignmentRequestDto;
import Dto.Zone.SpotAssignmentResponseDto;
import Enum.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ST6_DynamicPricingSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-6 Dynamic pricing applied correctly")
    void dynamicPricingApplied() {

        SpotAssignmentResponseDto spot =
                system.zoneController.assignSpot(
                        new SpotAssignmentRequestDto(
                                "U1",
                                ZoneType.STANDARD,
                                LocalDateTime.now()
                        )
                );

        StartSessionResponseDto session =
                system.sessionController.startSession(
                        new StartSessionRequestDto(
                                "U1",
                                "AA123BB",
                                spot.zoneId(),
                                spot.spotId(),
                                ZoneType.STANDARD,
                                false,
                                LocalDateTime.now().minusHours(2)
                        )
                );

        BillingResponse bill =
                system.billingController.calculateBill(
                        new BillingRequest(
                                session.sessionId(),
                                ZoneType.STANDARD,
                                DayType.WEEKDAY,
                                TimeOfDayBand.PEAK,
                                0.95, // surge
                                LocalDateTime.now(),
                                BigDecimal.ZERO,
                                24
                        )
                );

        assertTrue(bill.finalPrice().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(bill.basePrice().compareTo(BigDecimal.ZERO) > 0);
    }
}
