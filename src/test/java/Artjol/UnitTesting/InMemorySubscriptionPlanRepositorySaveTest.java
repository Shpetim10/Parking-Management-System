package Artjol.UnitTesting;

import Model.SubscriptionPlan;
import Model.DiscountInfo;
import Repository.impl.InMemorySubscriptionPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Optional;

// Unit Tests for M-66: InMemorySubscriptionPlanRepository.save

class InMemorySubscriptionPlanRepositorySaveTest {

    private InMemorySubscriptionPlanRepository repository;
    private DiscountInfo discountInfo;

    @BeforeEach
    void setUp() {
        repository = new InMemorySubscriptionPlanRepository();
        discountInfo = new DiscountInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, 0
        );
    }

    @Test
    @DisplayName("save stores plan successfully")
    void testSave_StoresPlan() {
        SubscriptionPlan plan = new SubscriptionPlan(
                1, 1, 5, 8.0, false, false, false, discountInfo
        );

        repository.save("user-1", plan);

        Optional<SubscriptionPlan> found = repository.getPlanForUser("user-1");
        assertTrue(found.isPresent());
        assertEquals(plan, found.get());
    }

    @Test
    @DisplayName("save throws exception for null userId")
    void testSave_NullUserId() {
        SubscriptionPlan plan = new SubscriptionPlan(
                1, 1, 5, 8.0, false, false, false, discountInfo
        );

        assertThrows(NullPointerException.class, () -> {
            repository.save(null, plan);
        });
    }

    @Test
    @DisplayName("save throws exception for null plan")
    void testSave_NullPlan() {
        assertThrows(NullPointerException.class, () -> {
            repository.save("user-1", null);
        });
    }

    @Test
    @DisplayName("save overwrites existing plan")
    void testSave_OverwritesExisting() {
        SubscriptionPlan plan1 = new SubscriptionPlan(
                1, 1, 5, 8.0, false, false, false, discountInfo
        );
        SubscriptionPlan plan2 = new SubscriptionPlan(
                2, 2, 10, 16.0, true, true, true, discountInfo
        );

        repository.save("user-1", plan1);
        repository.save("user-1", plan2);

        Optional<SubscriptionPlan> found = repository.getPlanForUser("user-1");
        assertTrue(found.isPresent());
        assertEquals(plan2, found.get());
    }

    @Test
    @DisplayName("save multiple users with different plans")
    void testSave_MultipleUsers() {
        SubscriptionPlan plan1 = new SubscriptionPlan(
                1, 1, 5, 8.0, false, false, false, discountInfo
        );
        SubscriptionPlan plan2 = new SubscriptionPlan(
                2, 2, 10, 16.0, true, true, true, discountInfo
        );

        repository.save("user-1", plan1);
        repository.save("user-2", plan2);

        assertEquals(plan1, repository.getPlanForUser("user-1").get());
        assertEquals(plan2, repository.getPlanForUser("user-2").get());
    }
}