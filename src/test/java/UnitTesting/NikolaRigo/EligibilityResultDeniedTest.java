package UnitTesting.NikolaRigo;

import Model.EligibilityResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EligibilityResultDeniedTest {

    @Test
    void denied_WithValidReason_ShouldReturnEligibilityResultWithFalseStatus() {
        // Arrange
        String reason = "Insufficient balance";

        // Act
        EligibilityResult result = EligibilityResult.denied(reason);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAllowed());
    }

    @Test
    void denied_WithValidReason_ShouldReturnEligibilityResultWithGivenReason() {
        // Arrange
        String reason = "Insufficient balance";

        // Act
        EligibilityResult result = EligibilityResult.denied(reason);

        // Assert
        assertEquals(reason, result.getReason());
    }

    @Test
    void denied_WithNullReason_ShouldReturnEligibilityResultWithNullReason() {
        // Act
        EligibilityResult result = EligibilityResult.denied(null);

        // Assert
        assertNull(result.getReason());
    }

    @Test
    void denied_WithNullReason_ShouldReturnEligibilityResultWithFalseStatus() {
        // Act
        EligibilityResult result = EligibilityResult.denied(null);

        // Assert
        assertFalse(result.isAllowed());
    }

    @Test
    void denied_WithEmptyReason_ShouldReturnEligibilityResultWithEmptyReason() {
        // Arrange
        String reason = "";

        // Act
        EligibilityResult result = EligibilityResult.denied(reason);

        // Assert
        assertEquals("", result.getReason());
        assertFalse(result.isAllowed());
    }

    @Test
    void denied_WithDifferentReasons_ShouldReturnCorrectReason() {
        // Arrange
        String reason1 = "Account suspended";
        String reason2 = "Vehicle not found";

        // Act
        EligibilityResult result1 = EligibilityResult.denied(reason1);
        EligibilityResult result2 = EligibilityResult.denied(reason2);

        // Assert
        assertEquals(reason1, result1.getReason());
        assertEquals(reason2, result2.getReason());
    }

    @Test
    void denied_ShouldReturnNewInstanceEachTime() {
        // Arrange
        String reason = "Account suspended";

        // Act
        EligibilityResult result1 = EligibilityResult.denied(reason);
        EligibilityResult result2 = EligibilityResult.denied(reason);

        // Assert
        assertNotSame(result1, result2);
    }

    @Test
    void denied_WithLongReason_ShouldReturnEligibilityResultWithFullReason() {
        // Arrange
        String reason = "User has exceeded maximum allowed parking duration and must pay penalty fees before exit";

        // Act
        EligibilityResult result = EligibilityResult.denied(reason);

        // Assert
        assertEquals(reason, result.getReason());
        assertFalse(result.isAllowed());
    }

    @Test
    void denied_WithSpecialCharacters_ShouldReturnEligibilityResultWithSpecialCharacters() {
        // Arrange
        String reason = "Error: $100.50 balance required! @parking_lot #123";

        // Act
        EligibilityResult result = EligibilityResult.denied(reason);

        // Assert
        assertEquals(reason, result.getReason());
    }

    @Test
    void denied_ShouldAlwaysReturnFalseStatus() {
        // Arrange
        String[] reasons = {
                "Insufficient balance",
                "Account suspended",
                "",
                null,
                "   ",
                "Special chars: !@#$%"
        };

        // Act & Assert
        for (String reason : reasons) {
            EligibilityResult result = EligibilityResult.denied(reason);
            assertFalse(result.isAllowed(),
                    "Expected false status for reason: " + reason);
        }
    }

    @Test
    void denied_WithWhitespaceReason_ShouldReturnEligibilityResultWithWhitespace() {
        // Arrange
        String reason = "   ";

        // Act
        EligibilityResult result = EligibilityResult.denied(reason);

        // Assert
        assertEquals("   ", result.getReason());
        assertFalse(result.isAllowed());
    }
}
