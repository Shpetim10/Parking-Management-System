package SystemTesting;

import Dto.Eligibility.EligibilityRequestDto;
import Dto.Eligibility.EligibilityResponseDto;
import Enum.UserStatus;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST-10: Eligibility denied when user is inactive
 *
 * Covers:
 * FR-1  User status management
 * FR-7  Eligibility validation
 */
class ST10_EligibilityDeniedInactiveUserSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();

        // Force user to INACTIVE
        system.userRepo.findById("U1")
                .orElseThrow()
                .setStatus(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("ST-10 Inactive user cannot start parking session")
    void eligibilityDeniedForInactiveUser() {

        // ===================== GIVEN =====================
        EligibilityRequestDto request =
                new EligibilityRequestDto(
                        "U1",
                        "AA123BB",
                        0,
                        0,
                        0,
                        0,
                        false,
                        LocalDateTime.now()
                );

        // ===================== WHEN =====================
        EligibilityResponseDto response =
                system.eligibilityController.checkEligibility(request);

        // ===================== THEN =====================
        // ===================== THEN =====================
        assertFalse(
                response.allowed(),
                "An inactive user must not be allowed to start a parking session"
        );

        assertNotNull(
                response.reason(),
                "When eligibility is denied, the system must provide a reason"
        );
    }
}