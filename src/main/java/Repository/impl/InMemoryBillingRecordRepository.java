package Repository.impl;

import Model.BillingRecord;
import Repository.BillingRecordRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryBillingRecordRepository implements BillingRecordRepository {
    private final Map<String, BillingRecord> storage = new HashMap<>();

    @Override
    public void save(BillingRecord record) {
        storage.put(record.getSessionId(), record);
    }

    @Override
    public Optional<BillingRecord> findBySessionId(String sessionId) {
        return Optional.ofNullable(storage.get(sessionId));
    }
}
