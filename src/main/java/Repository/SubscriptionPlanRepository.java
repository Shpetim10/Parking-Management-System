package Repository;

import Model.SubscriptionPlan;

import java.util.Optional;

public interface SubscriptionPlanRepository {

    Optional<SubscriptionPlan> getPlanForUser(String userId);
}