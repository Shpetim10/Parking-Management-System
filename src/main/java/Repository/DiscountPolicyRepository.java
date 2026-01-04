package Repository;

import Model.DiscountInfo;

public interface DiscountPolicyRepository {
    DiscountInfo findDiscountForUser(String userId);
    void save(String userId, DiscountInfo discountInfo);
}
