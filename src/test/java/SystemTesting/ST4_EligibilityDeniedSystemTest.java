package SystemTesting;

import Dto.Eligibility.*;
import Enum.UserStatus;
import Model.Vehicle;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-4: Eligibility Denied System Test
 *
 * Covers:
 * FR-7 Eligibility checks
 * FR-1 User account enforcement
 */
class ST4_EligibilityDeniedSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-4 Eligibility is denied for inactive user")
    void eligibilityDeniedForInactiveUser() {

        // ===================== GIVEN =====================
        String userId = "U1";
        String plate = "AA-111";

        system.vehicleRepo.save(new Vehicle(plate, userId));

        system.userRepo.findById(userId)
                .ifPresent(user -> user.setStatus(UserStatus.INACTIVE));

        // ===================== WHEN =====================
        EligibilityResponseDto response =
                system.eligibilityController.checkEligibility(
                        new EligibilityRequestDto(
                                userId,
                                plate,
                                0,
                                0,
                                0,
                                0,
                                false,
                                LocalDateTime.now()
                        )
                );

        // ===================== THEN =====================
        assertFalse(
                response.allowed(),
                "Eligibility must be denied for inactive user"
        );

        // Reason exists but wording is implementation-specific
        assertNotNull(
                response.reason(),
                "Denial reason must be provided"
        );
    }
}