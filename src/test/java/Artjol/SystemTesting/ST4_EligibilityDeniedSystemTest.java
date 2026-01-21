package Artjol.SystemTesting;


import Dto.Eligibility.*;
import Enum.*;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ST4_EligibilityDeniedSystemTest {

    private SystemTestFixture system;

    @BeforeEach
    void setup() {
        system = new SystemTestFixture();
        system.seedBaseData();
    }

    @Test
    @DisplayName("ST-4 Eligibility denied due to unpaid/active session")
    void eligibilityDenied() {

        EligibilityResponseDto response =
                system.eligibilityController.checkEligibility(
                        new EligibilityRequestDto(
                                "U1",
                                "AA123BB",
                                1,  // active sessions for vehicle
                                1,  // active sessions for user
                                1,
                                3,
                                true, // unpaid session
                                LocalDateTime.now()
                        )
                );

        assertFalse(response.allowed());
        assertNotNull(response.reason());
    }
}
