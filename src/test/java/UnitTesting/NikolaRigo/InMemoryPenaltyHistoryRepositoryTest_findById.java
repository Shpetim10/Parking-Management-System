package UnitTesting.NikolaRigo;

import Model.PenaltyHistory;
import Repository.impl.InMemoryPenaltyHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryPenaltyHistoryRepositoryTest_findById {

    private InMemoryPenaltyHistoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPenaltyHistoryRepository();
    }

    @Test
    void withExistingUserId_ShouldReturnPenaltyHistory() {
        // Arrange
        PenaltyHistory mockHistory = mock(PenaltyHistory.class);
        repository.save("user-1", mockHistory);

        // Act
        PenaltyHistory result = repository.findById("user-1");

        // Assert
        assertNotNull(result);
        assertEquals(mockHistory, result);
        assertSame(mockHistory, result);
    }

    @Test
    void withNonExistentUserId_ShouldReturnNull() {
        // Arrange
        PenaltyHistory mockHistory = mock(PenaltyHistory.class);
        repository.save("user-1", mockHistory);

        // Act
        PenaltyHistory result = repository.findById("user-999");

        // Assert
        assertNull(result);
    }

    @Test
    void withEmptyRepository_ShouldReturnNull() {
        // Act
        PenaltyHistory result = repository.findById("user-1");

        // Assert
        assertNull(result);
    }

    @Test
    void withNullUserId_ShouldReturnNull() {
        // Arrange
        PenaltyHistory mockHistory = mock(PenaltyHistory.class);
        repository.save("user-1", mockHistory);

        // Act
        PenaltyHistory result = repository.findById(null);

        // Assert
        assertNull(result);
    }

    @Test
    void withMultipleUsers_ShouldReturnCorrectHistory() {
        // Arrange
        PenaltyHistory mockHistory1 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory2 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory3 = mock(PenaltyHistory.class);

        repository.save("user-1", mockHistory1);
        repository.save("user-2", mockHistory2);
        repository.save("user-3", mockHistory3);

        // Act
        PenaltyHistory result1 = repository.findById("user-1");
        PenaltyHistory result2 = repository.findById("user-2");
        PenaltyHistory result3 = repository.findById("user-3");

        // Assert
        assertSame(mockHistory1, result1);
        assertSame(mockHistory2, result2);
        assertSame(mockHistory3, result3);
    }

    @Test
    void afterSavingHistory_ShouldBeAbleToFindIt() {
        // Arrange
        PenaltyHistory mockHistory = mock(PenaltyHistory.class);

        // Act
        repository.save("user-1", mockHistory);
        PenaltyHistory result = repository.findById("user-1");

        // Assert
        assertNotNull(result);
        assertSame(mockHistory, result);
    }

    @Test
    void afterUpdatingHistory_ShouldReturnUpdatedVersion() {
        // Arrange
        PenaltyHistory mockHistory1 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory2 = mock(PenaltyHistory.class);

        repository.save("user-1", mockHistory1);

        // Act
        repository.save("user-1", mockHistory2); // Update
        PenaltyHistory result = repository.findById("user-1");

        // Assert
        assertNotNull(result);
        assertSame(mockHistory2, result);
        assertNotSame(mockHistory1, result);
    }

    @Test
    void withEmptyStringUserId_ShouldReturnNull() {
        // Arrange
        PenaltyHistory mockHistory = mock(PenaltyHistory.class);
        repository.save("user-1", mockHistory);

        // Act
        PenaltyHistory result = repository.findById("");

        // Assert
        assertNull(result);
    }
}
