package Repository;

import Model.SubscriptionPlan;

public interface SubscriptionPlanRepository {

    SubscriptionPlan getPlanForUser(String userId);
}