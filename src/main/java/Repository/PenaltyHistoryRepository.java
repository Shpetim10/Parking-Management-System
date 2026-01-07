package Repository;

import Model.PenaltyHistory;

import java.util.List;

public interface PenaltyHistoryRepository {

    PenaltyHistory getOrCreate(String userId);

    void save(String userId, PenaltyHistory history);

    List<PenaltyHistory> findAll();
}