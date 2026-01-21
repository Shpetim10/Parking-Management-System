package UnitTesting.ShpetimShabanaj;

import Model.SubscriptionPlan;
import Repository.SubscriptionPlanRepository;
import Repository.impl.InMemorySubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class SubscriptionPlanSaveTest {
    private SubscriptionPlanRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySubscriptionPlanRepository();
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should store subscription plan correctly for a user")
    void testSaveValidSubscription() {
        String userId = "U1";
        SubscriptionPlan mockPlan = mock(SubscriptionPlan.class);

        repository.save(userId, mockPlan);

        Optional<SubscriptionPlan> result = repository.getPlanForUser(userId);
        assertTrue(result.isPresent());
        assertEquals(mockPlan, result.get());
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should throw NullPointerException when userId is null")
    void testSaveNullUserId() {
        SubscriptionPlan mockPlan = mock(SubscriptionPlan.class);

        assertThrows(NullPointerException.class, () ->
                        repository.save(null, mockPlan)
        );
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should throw NullPointerException when plan is null")
    void testSaveNullPlan() {
        assertThrows(NullPointerException.class, () ->
                        repository.save("U1", null)
        );
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should overwrite existing plan for the same user")
    void testSaveOverwritesExisting() {
        String userId = "U1";
        SubscriptionPlan oldPlan = mock(SubscriptionPlan.class);
        SubscriptionPlan newPlan = mock(SubscriptionPlan.class);

        repository.save(userId, oldPlan);
        repository.save(userId, newPlan);

        Optional<SubscriptionPlan> result = repository.getPlanForUser(userId);
        assertEquals(newPlan, result.get());
    }
}
