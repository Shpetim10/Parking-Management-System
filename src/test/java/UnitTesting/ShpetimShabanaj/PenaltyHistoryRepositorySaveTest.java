package UnitTesting.ShpetimShabanaj;

import Model.PenaltyHistory;
import Repository.PenaltyHistoryRepository;
import Repository.impl.InMemoryPenaltyHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class PenaltyHistoryRepositorySaveTest {
    private PenaltyHistoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPenaltyHistoryRepository();
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should store PenaltyHistory correctly for a specific user")
    void testSaveValidHistory() {
        String userId = "U1";
        PenaltyHistory history = mock(PenaltyHistory.class);

        repository.save(userId, history);

        assertEquals(history, repository.findById(userId));
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should throw NullPointerException when userId is null")
    void testSaveNullUserId() {
        PenaltyHistory history = mock(PenaltyHistory.class);

        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                repository.save(null, history)
        );
        assertEquals("userId must not be null", ex.getMessage());
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should throw NullPointerException when history is null")
    void testSaveNullHistory() {
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                repository.save("U1", null)
        );
        assertEquals("history must not be null", ex.getMessage());
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should overwrite existing history for the same user ID")
    void testSaveOverwritesExisting() {
        String userId = "U1";
        PenaltyHistory firstHistory = mock(PenaltyHistory.class);
        PenaltyHistory secondHistory = mock(PenaltyHistory.class);

        repository.save(userId, firstHistory);
        repository.save(userId, secondHistory);

        assertEquals(secondHistory, repository.findById(userId));
    }
}
