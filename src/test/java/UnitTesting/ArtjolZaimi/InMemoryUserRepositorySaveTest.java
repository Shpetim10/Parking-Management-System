package UnitTesting.ArtjolZaimi;
import Model.User;
import Enum.UserStatus;
import Repository.impl.InMemoryUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

// Unit Tests for M-72: InMemoryUserRepository.save


class InMemoryUserRepositorySaveTest {

    private InMemoryUserRepository repository;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new InMemoryUserRepository();
    }

    @Test
    @DisplayName("save stores user successfully")
    void testSave_StoresUser() {
        when(mockUser.getId()).thenReturn("user-1");

        repository.save(mockUser);

        Optional<User> found = repository.findById("user-1");
        assertTrue(found.isPresent());
        assertEquals(mockUser, found.get());
    }

    @Test
    @DisplayName("save overwrites existing user")
    void testSave_OverwritesExisting() {
        User user1 = new User("user-1", UserStatus.ACTIVE);
        User user2 = new User("user-1", UserStatus.BLACKLISTED);

        repository.save(user1);
        repository.save(user2);

        Optional<User> found = repository.findById("user-1");
        assertTrue(found.isPresent());
        assertEquals(UserStatus.BLACKLISTED, found.get().getStatus());
    }

    @Test
    @DisplayName("save multiple users")
    void testSave_MultipleUsers() {
        User user1 = new User("user-1", UserStatus.ACTIVE);
        User user2 = new User("user-2", UserStatus.INACTIVE);

        repository.save(user1);
        repository.save(user2);

        assertTrue(repository.findById("user-1").isPresent());
        assertTrue(repository.findById("user-2").isPresent());
    }



    @Test
    @DisplayName("save preserves user identity")
    void testSave_PreservesIdentity() {
        User user = new User("user-1", UserStatus.ACTIVE);
        repository.save(user);

        Optional<User> found = repository.findById("user-1");
        assertSame(user, found.get());
    }

}