package UnitTesting.ShpetimShabanaj;

import Model.SubscriptionPlan;
import Repository.SubscriptionPlanRepository;
import Repository.impl.InMemorySubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class SubscriptionPlanRepositoryFetPlanForUserTest {
    private SubscriptionPlanRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySubscriptionPlanRepository();
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should return Optional with plan for valid userId")
    void testGetPlanForUserExisting() {
        String userId = "U1";
        SubscriptionPlan mockPlan = mock(SubscriptionPlan.class);
        repository.save(userId, mockPlan);

        Optional<SubscriptionPlan> result = repository.getPlanForUser(userId);

        assertTrue(result.isPresent());
        assertEquals(mockPlan, result.get());
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should return empty Optional for non-existent userId")
    void testGetPlanForUserNotExisting() {
        Optional<SubscriptionPlan> result = repository.getPlanForUser("U2");

        assertTrue(result.isEmpty());
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should return the most recently saved plan for a user")
    void testGetPlanAfterUpdate() {
        String userId = "U1";
        SubscriptionPlan oldPlan = mock(SubscriptionPlan.class);
        SubscriptionPlan newPlan = mock(SubscriptionPlan.class);

        repository.save(userId, oldPlan);
        repository.save(userId, newPlan);

        Optional<SubscriptionPlan> result = repository.getPlanForUser(userId);

        assertEquals(newPlan, result.get());
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should handle null userId by returning empty Optional")
    void testGetPlanForNullUser() {
        Optional<SubscriptionPlan> result = repository.getPlanForUser(null);

        assertTrue(result.isEmpty());
    }
}
