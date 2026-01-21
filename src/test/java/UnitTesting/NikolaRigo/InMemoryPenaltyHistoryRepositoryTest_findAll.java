package UnitTesting.NikolaRigo;

import Model.PenaltyHistory;
import Repository.impl.InMemoryPenaltyHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryPenaltyHistoryRepositoryTest_findAll {

    private InMemoryPenaltyHistoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPenaltyHistoryRepository();
    }

    @Test
    void withEmptyRepository_ShouldReturnEmptyList() {
        // Act
        List<PenaltyHistory> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void withSingleHistory_ShouldReturnListWithOneElement() {
        // Arrange
        PenaltyHistory mockHistory = mock(PenaltyHistory.class);
        repository.save("user-1", mockHistory);

        // Act
        List<PenaltyHistory> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(mockHistory));
    }

    @Test
    void withMultipleHistories_ShouldReturnAllHistories() {
        // Arrange
        PenaltyHistory mockHistory1 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory2 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory3 = mock(PenaltyHistory.class);

        repository.save("user-1", mockHistory1);
        repository.save("user-2", mockHistory2);
        repository.save("user-3", mockHistory3);

        // Act
        List<PenaltyHistory> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(mockHistory1));
        assertTrue(result.contains(mockHistory2));
        assertTrue(result.contains(mockHistory3));
    }

    @Test
    void shouldReturnNewListInstance() {
        // Arrange
        PenaltyHistory mockHistory = mock(PenaltyHistory.class);
        repository.save("user-1", mockHistory);

        // Act
        List<PenaltyHistory> result1 = repository.findAll();
        List<PenaltyHistory> result2 = repository.findAll();

        // Assert
        assertNotSame(result1, result2); // Different list instances
        assertEquals(result1, result2);   // But same content
    }

    @Test
    void modifyingReturnedList_ShouldNotAffectRepository() {
        // Arrange
        PenaltyHistory mockHistory1 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory2 = mock(PenaltyHistory.class);

        repository.save("user-1", mockHistory1);

        // Act
        List<PenaltyHistory> result = repository.findAll();
        result.add(mockHistory2); // Modify returned list

        List<PenaltyHistory> newResult = repository.findAll();

        // Assert
        assertEquals(2, result.size());      // Modified list has 2 elements
        assertEquals(1, newResult.size());   // Repository still has 1 element
        assertTrue(newResult.contains(mockHistory1));
        assertFalse(newResult.contains(mockHistory2));
    }

    @Test
    void afterUpdatingHistory_ShouldReturnUpdatedVersion() {
        // Arrange
        PenaltyHistory mockHistory1 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory2 = mock(PenaltyHistory.class);

        repository.save("user-1", mockHistory1);
        List<PenaltyHistory> before = repository.findAll();

        // Act
        repository.save("user-1", mockHistory2); // Update
        List<PenaltyHistory> after = repository.findAll();

        // Assert
        assertEquals(1, before.size());
        assertTrue(before.contains(mockHistory1));

        assertEquals(1, after.size());
        assertTrue(after.contains(mockHistory2));
        assertFalse(after.contains(mockHistory1));
    }

    @Test
    void afterSavingNewHistory_ShouldIncreaseListSize() {
        // Arrange
        PenaltyHistory mockHistory1 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory2 = mock(PenaltyHistory.class);

        repository.save("user-1", mockHistory1);
        List<PenaltyHistory> beforeAdd = repository.findAll();

        // Act
        repository.save("user-2", mockHistory2);
        List<PenaltyHistory> afterAdd = repository.findAll();

        // Assert
        assertEquals(1, beforeAdd.size());
        assertEquals(2, afterAdd.size());
        assertTrue(afterAdd.contains(mockHistory1));
        assertTrue(afterAdd.contains(mockHistory2));
    }

    @Test
    void withDuplicateUserIdUpdates_ShouldOnlyContainLatestHistory() {
        // Arrange
        PenaltyHistory mockHistory1 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory2 = mock(PenaltyHistory.class);
        PenaltyHistory mockHistory3 = mock(PenaltyHistory.class);

        repository.save("user-1", mockHistory1);
        repository.save("user-1", mockHistory2); // Update
        repository.save("user-1", mockHistory3); // Update again

        // Act
        List<PenaltyHistory> result = repository.findAll();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(mockHistory3));
        assertFalse(result.contains(mockHistory1));
        assertFalse(result.contains(mockHistory2));
    }
}
