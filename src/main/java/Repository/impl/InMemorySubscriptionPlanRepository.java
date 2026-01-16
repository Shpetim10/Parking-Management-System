package Repository.impl;

import Model.SubscriptionPlan;
import Repository.SubscriptionPlanRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemorySubscriptionPlanRepository implements SubscriptionPlanRepository {
    private final Map<String, SubscriptionPlan> subscriptions = new HashMap<>();
    private final Map<String, SubscriptionPlan> plansByUser = new HashMap<>();

    public void save(String userId, SubscriptionPlan plan) {
        plansByUser.put(userId, plan);
    }

    @Override
    public SubscriptionPlan getPlanForUser(String userId) {
        return plansByUser.get(userId);
    }
}
