package UnitTesting.ShpetimShabanaj;

import Enum.ExitFailureReason;
import Model.ExitDecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class ExitDecisionDenyTest {
    @Test
    @DisplayName("TC-01: Should return An object with allowed false and reason none")
    void testWhenReasonIsNull(){
        ExitDecision exitDecision = ExitDecision.deny(null);

        assertAll("Method should return an object with allowed false and reason none.",
                ()-> assertNotNull(exitDecision),
                ()-> assertFalse(exitDecision.isAllowed()),
                ()-> assertEquals(ExitFailureReason.NONE, exitDecision.getReason()));
    }
    @ParameterizedTest
    @CsvSource({
            "USER_INACTIVE", "SESSION_NOT_PAID"
                    ,"ALREADY_CLOSED", "VEHICLE_MISMATCH"
    })
    @DisplayName("TC-02: The method should return an object with allowed false and The specified reason")
    void testWhenReasonIsNotNull(String reason){
        ExitDecision exitDecision = ExitDecision.deny(ExitFailureReason.valueOf(reason));

        assertAll("Should return and object with allowed false and the specified reasom",
                ()->assertNotNull(exitDecision),
                ()->assertFalse(exitDecision.isAllowed()),
                ()->assertEquals(ExitFailureReason.valueOf(reason), exitDecision.getReason()));
    }
}
