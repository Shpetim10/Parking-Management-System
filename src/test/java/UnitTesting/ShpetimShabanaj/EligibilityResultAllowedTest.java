package UnitTesting.ShpetimShabanaj;

import Model.EligibilityResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EligibilityResultAllowedTest {
    @Test
    @DisplayName("TC-01: Should return a new object with allowed=true and reason=null")
    void testAllowedReturningObjectWithAllowedTrueAndReasonNull(){
        EligibilityResult result=EligibilityResult.allowed();

        assertAll("Verify all fields are correctly assigned",
                ()->assertNotNull(result),
                ()-> assertTrue(result.isAllowed()),
                ()-> assertNull(result.getReason())
        );
    }
}
