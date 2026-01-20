package Artjol.UnitTesting;

import Model.PenaltyHistory;
import Repository.impl.InMemoryPenaltyHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

// Unit Tests for M-63: InMemoryPenaltyHistoryRepository.getOrCreate

class InMemoryPenaltyHistoryRepositoryGetOrCreateTest {

    private InMemoryPenaltyHistoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPenaltyHistoryRepository();
    }

    @Test
    @DisplayName("creates new history for new user")
    void testGetOrCreate_NewUser() {
        PenaltyHistory history = repository.getOrCreate("user-1");

        assertNotNull(history);
        assertTrue(history.isEmpty());
        assertEquals(0, history.getPenaltyCount());
    }

    @Test
    @DisplayName("returns existing history for existing user")
    void testGetOrCreate_ExistingUser() {
        PenaltyHistory first = repository.getOrCreate("user-1");
        PenaltyHistory second = repository.getOrCreate("user-1");

        assertSame(first, second);
    }

    @Test
    @DisplayName("throws exception for null userId")
    void testGetOrCreate_NullUserId() {
        assertThrows(NullPointerException.class, () -> {
            repository.getOrCreate(null);
        });
    }


    @Test
    @DisplayName("retrieves history after explicit save")
    void testGetOrCreate_AfterSave() {
        PenaltyHistory history = new PenaltyHistory();
        repository.save("user-1", history);

        PenaltyHistory retrieved = repository.getOrCreate("user-1");

        assertSame(history, retrieved);
    }


    @Test
    @DisplayName("handles multiple users")
    void testGetOrCreate_MultipleUsers() {
        PenaltyHistory h1 = repository.getOrCreate("user-1");
        PenaltyHistory h2 = repository.getOrCreate("user-2");
        PenaltyHistory h3 = repository.getOrCreate("user-3");

        assertNotNull(h1);
        assertNotNull(h2);
        assertNotNull(h3);
        assertNotSame(h1, h2);
        assertNotSame(h2, h3);
    }

}