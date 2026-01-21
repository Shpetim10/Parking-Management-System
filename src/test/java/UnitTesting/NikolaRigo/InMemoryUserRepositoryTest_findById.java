package UnitTesting.NikolaRigo;

import Model.User;
import Repository.impl.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryUserRepositoryTest_findById {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void withExistingUserId_ShouldReturnOptionalWithUser() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-1");

        repository.save(mockUser);

        // Act
        Optional<User> result = repository.findById("user-1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
        assertSame(mockUser, result.get());
        verify(mockUser, atLeastOnce()).getId();
    }

    @Test
    void withNonExistentUserId_ShouldReturnEmptyOptional() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-1");

        repository.save(mockUser);

        // Act
        Optional<User> result = repository.findById("user-999");

        // Assert
        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    void withEmptyRepository_ShouldReturnEmptyOptional() {
        // Act
        Optional<User> result = repository.findById("user-1");

        // Assert
        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    void withNullUserId_ShouldReturnEmptyOptional() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-1");

        repository.save(mockUser);

        // Act
        Optional<User> result = repository.findById(null);

        // Assert
        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    void withMultipleUsers_ShouldReturnCorrectUser() {
        // Arrange
        User mockUser1 = mock(User.class);
        when(mockUser1.getId()).thenReturn("user-1");

        User mockUser2 = mock(User.class);
        when(mockUser2.getId()).thenReturn("user-2");

        User mockUser3 = mock(User.class);
        when(mockUser3.getId()).thenReturn("user-3");

        repository.save(mockUser1);
        repository.save(mockUser2);
        repository.save(mockUser3);

        // Act
        Optional<User> result1 = repository.findById("user-1");
        Optional<User> result2 = repository.findById("user-2");
        Optional<User> result3 = repository.findById("user-3");

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertSame(mockUser1, result1.get());
        assertSame(mockUser2, result2.get());
        assertSame(mockUser3, result3.get());
    }

    @Test
    void afterSavingUser_ShouldBeAbleToFindIt() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-1");

        // Act
        repository.save(mockUser);
        Optional<User> result = repository.findById("user-1");

        // Assert
        assertTrue(result.isPresent());
        assertSame(mockUser, result.get());
    }

    @Test
    void afterUpdatingUser_ShouldReturnUpdatedVersion() {
        // Arrange
        User mockUser1 = mock(User.class);
        when(mockUser1.getId()).thenReturn("user-1");

        User mockUser2 = mock(User.class);
        when(mockUser2.getId()).thenReturn("user-1");

        repository.save(mockUser1);

        // Act
        repository.save(mockUser2); // Update with same ID
        Optional<User> result = repository.findById("user-1");

        // Assert
        assertTrue(result.isPresent());
        assertSame(mockUser2, result.get());
        assertNotSame(mockUser1, result.get());
    }

    @Test
    void withEmptyStringUserId_ShouldReturnEmptyOptional() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-1");

        repository.save(mockUser);

        // Act
        Optional<User> result = repository.findById("");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnOptionalNotNull() {
        // Act
        Optional<User> result = repository.findById("non-existent");

        // Assert
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void withValidUserId_OptionalShouldContainExactSameInstance() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-1");

        repository.save(mockUser);

        // Act
        Optional<User> result1 = repository.findById("user-1");
        Optional<User> result2 = repository.findById("user-1");

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertSame(result1.get(), result2.get()); // Same instance
    }
}