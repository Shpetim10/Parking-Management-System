package IntegrationTesting.ShpëtimShabanaj;

import Dto.Creation.CreateUserDto;
import Enum.UserStatus;
import Model.SubscriptionPlan;
import Model.User;
import Repository.SubscriptionPlanRepository;
import Repository.UserRepository;
import Repository.impl.InMemoryUserRepository;
import Service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pairwise Integration: UserService + UserRepository")
class UserServiceUserRepositoryIntegrationTesting {
    private UserRepository userRepository;
    private SubscriptionPlanRepository stubPlanRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        stubPlanRepository = new StubSubscriptionPlanRepository();
        userService = new UserServiceImpl(userRepository, stubPlanRepository);
    }

    // IT-01: Happy path – user is created as ACTIVE
    @Test
    @DisplayName("IT-01: create ACTIVE user when id does not exist")
    void createUser_persistsActiveUser() {
        CreateUserDto dto = new CreateUserDto("U1");

        User created = userService.createUser(dto);

        // Assert: returned user
        assertNotNull(created);
        assertEquals("U1", created.getId());
        assertEquals(UserStatus.ACTIVE, created.getStatus());

        // Assert: saved in repo
        assertTrue(userRepository.exists("U1"));
        User stored = userRepository.findById("U1").orElseThrow();
        assertEquals(UserStatus.ACTIVE, stored.getStatus());
    }

    // IT-02: Duplicate user id -> IllegalArgumentException
    @Test
    @DisplayName("IT-02: createUser fails when user with same id already exists")
    void createUser_duplicateId_throws() {
        userService.createUser(new CreateUserDto("U1"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(new CreateUserDto("U1"))
        );
        assertEquals("User with this id already exists!", ex.getMessage());
    }

    // IT-03: updateUserStatus happy path
    @Test
    @DisplayName("IT-03: updateUserStatus updates status and saves user")
    void updateUserStatus_updatesAndPersists() {
        // Arrange
        userService.createUser(new CreateUserDto("U2"));

        // Act
        userService.updateUserStatus("U2", "blacklisted");

        // Assert
        User updated = userRepository.findById("U2").orElseThrow();
        assertEquals(UserStatus.BLACKLISTED, updated.getStatus());
    }

    // IT-04: updateUserStatus – user not found
    @Test
    @DisplayName("IT-04: updateUserStatus throws when user does not exist")
    void updateUserStatus_missingUser_throws() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.updateUserStatus("U?", "ACTIVE")
        );
        assertEquals("User not found!", ex.getMessage());
    }

    // IT-05: updateUserStatus with invalid status string
    @Test
    @DisplayName("IT-05: updateUserStatus throws IllegalArgumentException for invalid status")
    void updateUserStatus_invalidStatus_throws() {
        userService.createUser(new CreateUserDto("U3"));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserStatus("U3", "status")
        );
    }

    // Stub
    private static class StubSubscriptionPlanRepository implements SubscriptionPlanRepository {

        @Override
        public void save(String userId, SubscriptionPlan subscriptionPlan) {
            //
        }

        @Override
        public Optional<SubscriptionPlan> getPlanForUser(String userId) {
            return Optional.empty();
        }
    }
}
