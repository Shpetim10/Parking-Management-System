package Repository.impl;

import Model.PenaltyHistory;
import Repository.PenaltyHistoryRepository;

import java.util.*;

public class InMemoryPenaltyHistoryRepository implements PenaltyHistoryRepository {

    private final Map<String, PenaltyHistory> byUser = new HashMap<>();

    @Override
    public PenaltyHistory getOrCreate(String userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return byUser.computeIfAbsent(userId, id -> new PenaltyHistory());
    }

    @Override
    public void save(String userId, PenaltyHistory history) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(history, "history must not be null");
        byUser.put(userId, history);
    }

    @Override
    public List<PenaltyHistory> findAll() {
        return new ArrayList<>(byUser.values());
    }
}