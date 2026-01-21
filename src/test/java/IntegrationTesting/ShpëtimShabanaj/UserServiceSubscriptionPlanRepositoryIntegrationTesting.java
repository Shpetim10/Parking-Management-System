package IntegrationTesting.ShpëtimShabanaj;

import Dto.Creation.CreateUserDto;
import Enum.UserStatus;
import Model.DiscountInfo;
import Model.SubscriptionPlan;
import Model.User;
import Repository.SubscriptionPlanRepository;
import Repository.UserRepository;
import Repository.impl.InMemorySubscriptionPlanRepository;
import Service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pairwise Integration: UserServiceImpl + InMemorySubscriptionPlanRepository")
class UserServiceSubscriptionPlanRepositoryIntegrationTesting {

    private UserRepository stubUserRepository;
    private SubscriptionPlanRepository planRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        stubUserRepository = new StubUserRepository();
        planRepository = new InMemorySubscriptionPlanRepository();
        userService = new UserServiceImpl(stubUserRepository, planRepository);
    }

    // IT-01: Default plan is created when none exists
    @Test
    @DisplayName("IT-01: createUser creates default subscription plan when none exists")
    void createUser_createsDefaultPlanIfMissing() {
        String userId = "user-100";
        CreateUserDto dto = new CreateUserDto(userId);

        User created = userService.createUser(dto);

        assertNotNull(created);
        assertEquals(userId, created.getId());
        assertEquals(UserStatus.ACTIVE, created.getStatus());

        SubscriptionPlan plan = planRepository.getPlanForUser(userId)
                .orElseThrow(() -> new AssertionError("Expected default plan to be created"));

        DiscountInfo discount = plan.discountInfo;
        assertNotNull(discount, "Default plan should have DiscountInfo");
        assertEquals(new BigDecimal("0.1"), discount.getSubscriptionDiscountPercent());
    }

    // IT-02: Existing plan must not be overridden
    @Test
    @DisplayName("IT-02: createUser does NOT override existing subscription plan")
    void createUser_doesNotOverrideExistingPlan() {
        String userId = "U2";

        SubscriptionPlan existingPlan = new SubscriptionPlan(
                2, 2, 10, 12,
                true, false, true,
                new DiscountInfo(
                        new BigDecimal("0.25"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false,
                        0
                )
        );
        planRepository.save(userId, existingPlan);

        User created = userService.createUser(new CreateUserDto(userId));

        assertNotNull(created);
        assertEquals(userId, created.getId());

        SubscriptionPlan after = planRepository.getPlanForUser(userId).orElseThrow();
        assertSame(existingPlan, after, "Existing plan should not be overridden");
    }

    // Stub
    private static class StubUserRepository implements UserRepository {

        private final Map<String, User> store = new HashMap<>();

        @Override
        public Optional<User> findById(String userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public void save(User user) {
            store.put(user.getId(), user);
        }

        @Override
        public boolean exists(String userId) {
            return store.containsKey(userId);
        }
    }
}
