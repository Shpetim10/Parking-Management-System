package Repository.impl;

import Model.DiscountInfo;
import Repository.DiscountPolicyRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InMemoryDiscountPolicyRepository implements DiscountPolicyRepository {
    private final Map<String, DiscountInfo> byUser = new HashMap<>();
    private final DiscountInfo defaultDiscountInfo;

    public InMemoryDiscountPolicyRepository(DiscountInfo defaultDiscountInfo) {
        this.defaultDiscountInfo = Objects.requireNonNull(defaultDiscountInfo, "defaultDiscountInfo must not be null");
    }

    @Override
    public DiscountInfo findDiscountForUser(String userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return byUser.getOrDefault(userId, defaultDiscountInfo);
    }

    @Override
    public void save(String userId, DiscountInfo discountInfo) {
        byUser.put(
                Objects.requireNonNull(userId, "userId must not be null"),
                Objects.requireNonNull(discountInfo, "discountInfo must not be null")
        );
    }
}
