package Controller;

import Dto.DiscountInfo.DiscountInfoDto;
import Model.DiscountInfo;
import Model.SubscriptionPlan;
import Repository.DiscountPolicyRepository;
import Repository.SubscriptionPlanRepository;

import java.util.Objects;

public class DiscountInfoController {
    private final DiscountPolicyRepository discountPolicyRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public DiscountInfoController(DiscountPolicyRepository discountPolicyRepository, SubscriptionPlanRepository subscriptionPlanRepository) {
        this.discountPolicyRepository = Objects.requireNonNull(discountPolicyRepository);
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public DiscountInfoDto getDiscountForUser(String userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        SubscriptionPlan plan=subscriptionPlanRepository.getPlanForUser(userId).orElseThrow(()->new IllegalArgumentException("User not subscribed to any plan!"));

        DiscountInfo info = plan.discountInfo;
        Objects.requireNonNull(info, "discountInfo must not be null");
        return new DiscountInfoDto(
                info.getSubscriptionDiscountPercent(),
                info.getPromoDiscountPercent(),
                info.getPromoDiscountFixed(),
                info.isSubscriptionHasFreeHours(),
                info.getFreeHoursPerDay()
        );
    }

    public void saveDiscountForUser(String userId, DiscountInfoDto dto) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(dto, "dto must not be null");

        SubscriptionPlan subscriptionPlan=subscriptionPlanRepository.getPlanForUser(userId).orElseThrow(()->new IllegalArgumentException("User not subscribed to any plan!"));

        DiscountInfo info = new DiscountInfo(
                dto.subscriptionDiscountPercent(),
                dto.promoDiscountPercent(),
                dto.promoDiscountFixed(),
                dto.subscriptionHasFreeHours(),
                dto.freeHoursPerDay()
        );

        subscriptionPlan.discountInfo=info;
        subscriptionPlanRepository.save(userId, subscriptionPlan);
    }
}
