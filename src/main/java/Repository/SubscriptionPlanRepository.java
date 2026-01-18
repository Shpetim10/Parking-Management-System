package Repository;

import Model.SubscriptionPlan;

import java.util.Optional;

public interface SubscriptionPlanRepository {
    void save(String userId, SubscriptionPlan subscriptionPlan);
    Optional<SubscriptionPlan> getPlanForUser(String userId);
}