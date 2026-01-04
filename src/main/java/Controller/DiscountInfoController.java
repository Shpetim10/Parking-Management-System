package Controller;

import Dto.DiscountInfo.DiscountInfoDto;
import Model.DiscountInfo;
import Repository.DiscountPolicyRepository;

import java.util.Objects;

public class DiscountInfoController {
    private final DiscountPolicyRepository discountPolicyRepository;

    public DiscountInfoController(DiscountPolicyRepository discountPolicyRepository) {
        this.discountPolicyRepository = Objects.requireNonNull(discountPolicyRepository);
    }

    public DiscountInfoDto getDiscountForUser(String userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        DiscountInfo info = discountPolicyRepository.findDiscountForUser(userId);
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

        DiscountInfo info = new DiscountInfo(
                dto.subscriptionDiscountPercent(),
                dto.promoDiscountPercent(),
                dto.promoDiscountFixed(),
                dto.subscriptionHasFreeHours(),
                dto.freeHoursPerDay()
        );
        discountPolicyRepository.save(userId, info);
    }
}
