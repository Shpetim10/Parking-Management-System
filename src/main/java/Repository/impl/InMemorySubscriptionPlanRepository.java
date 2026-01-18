package Repository.impl;

import Model.SubscriptionPlan;
import Repository.SubscriptionPlanRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class InMemorySubscriptionPlanRepository implements SubscriptionPlanRepository {
    private final Map<String, SubscriptionPlan> subscriptions = new HashMap<>();
    private final Map<String, SubscriptionPlan> plansByUser = new HashMap<>();

    @Override
    public void save(String userId, SubscriptionPlan subscriptionPlan) {
        plansByUser.put(
                Objects.requireNonNull(userId),
                Objects.requireNonNull(subscriptionPlan));
    }

    @Override
    public Optional<SubscriptionPlan> getPlanForUser(String userId) {
        return Optional.ofNullable(plansByUser.get(userId));
    }
}
