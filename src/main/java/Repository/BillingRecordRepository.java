package Repository;

import Model.BillingRecord;
import Model.BillingResult;

import java.util.Optional;

public interface BillingRecordRepository {
    void save(BillingRecord record);
    Optional<BillingRecord> findBySessionId(String sessionId);
}
