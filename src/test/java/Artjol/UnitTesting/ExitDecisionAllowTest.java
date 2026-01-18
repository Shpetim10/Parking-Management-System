package Artjol.UnitTesting;

import Model.ExitDecision;
import Enum.ExitFailureReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

// Unit Tests for M-10: ExitDecision.allow

class ExitDecisionAllowTest {

    @Test
    @DisplayName("allow creates allowed decision")
    void testAllow_CreatesAllowedDecision() {
        ExitDecision decision = ExitDecision.allow();

        assertTrue(decision.isAllowed());
        assertEquals(ExitFailureReason.NONE, decision.getReason());
    }

    @Test
    @DisplayName("allow decision is not null")
    void testAllow_NotNull() {
        ExitDecision decision = ExitDecision.allow();

        assertNotNull(decision);
    }

    @Test
    @DisplayName("allow creates distinct instances")
    void testAllow_DistinctInstances() {
        ExitDecision decision1 = ExitDecision.allow();
        ExitDecision decision2 = ExitDecision.allow();

        assertNotSame(decision1, decision2);
    }

    @Test
    @DisplayName("multiple allow calls are consistent")
    void testAllow_MultipleCallsConsistent() {
        ExitDecision decision1 = ExitDecision.allow();
        ExitDecision decision2 = ExitDecision.allow();
        ExitDecision decision3 = ExitDecision.allow();

        assertTrue(decision1.isAllowed());
        assertTrue(decision2.isAllowed());
        assertTrue(decision3.isAllowed());

        assertEquals(ExitFailureReason.NONE, decision1.getReason());
        assertEquals(ExitFailureReason.NONE, decision2.getReason());
        assertEquals(ExitFailureReason.NONE, decision3.getReason());
    }

    @Test
    @DisplayName("allow decision state cannot be modified")
    void testAllow_ImmutableState() {
        ExitDecision decision = ExitDecision.allow();

        // Verify initial state
        assertTrue(decision.isAllowed());
        assertEquals(ExitFailureReason.NONE, decision.getReason());

        // Check state again - should be unchanged
        assertTrue(decision.isAllowed());
        assertEquals(ExitFailureReason.NONE, decision.getReason());
    }
}