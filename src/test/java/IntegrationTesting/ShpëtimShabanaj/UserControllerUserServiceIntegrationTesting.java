package IntegrationTesting.ShpëtimShabanaj;

import Controller.UserController;
import Enum.UserStatus;
import Model.SubscriptionPlan;
import Model.User;
import Repository.SubscriptionPlanRepository;
import Repository.UserRepository;
import Service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@DisplayName("Integration Testing: UserController + UserService")
class UserControllerUserServiceIntegrationTesting {

    private UserRepository userRepository;
    private SubscriptionPlanRepository planRepository;
    private UserServiceImpl userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        planRepository = mock(SubscriptionPlanRepository.class);

        userService = new UserServiceImpl(userRepository, planRepository);
        userController = new UserController(userService);
    }

    // IT-01: Happy path create
    @Test
    @DisplayName("IT-01: createrUser creates ACTIVE user and default subscription when no plan exists")
    void createrUser_createsUserAndDefaultPlan() {
        String userId = "U1";
        when(userRepository.exists(userId)).thenReturn(false);
        when(planRepository.getPlanForUser(userId)).thenReturn(Optional.empty());

        userController.createrUser(userId);

        verify(userRepository).exists(userId);
        verify(planRepository).getPlanForUser(userId);

        ArgumentCaptor<SubscriptionPlan> planCaptor =
                ArgumentCaptor.forClass(SubscriptionPlan.class);
        verify(planRepository).save(eq(userId), planCaptor.capture());

        SubscriptionPlan savedPlan = planCaptor.getValue();
        assertNotNull(savedPlan);
        assertNotNull(savedPlan.discountInfo);
        assertEquals(new BigDecimal("0.1"),
                savedPlan.discountInfo.getSubscriptionDiscountPercent());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(userId, savedUser.getId());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
    }

    // IT-02: createUser with existing id
    @Test
    @DisplayName("IT-02: createrUser propagates IllegalArgumentException when user already exists")
    void createrUser_duplicateId_propagatesException() {
        String userId = "U1";
        when(userRepository.exists(userId)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userController.createrUser(userId)
        );
        assertEquals("User with this id already exists!", ex.getMessage());

        verify(userRepository, never()).save(any());
        verify(planRepository, never()).save(anyString(), any());
    }

    // IT-03: updateUser happy path – status updated and saved
    @Test
    @DisplayName("IT-03: updateUser updates user status via service and persists it")
    void updateUser_updatesStatus() {
        String userId = "U2";
        User existing = new User(userId, UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

        userController.updateUser(userId, "blacklisted");

        verify(userRepository).findById(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertEquals(userId, saved.getId());
        assertEquals(UserStatus.BLACKLISTED, saved.getStatus());
    }

    // IT-04: updateUser with invalid status – IllegalArgumentException from service
    @Test
    @DisplayName("IT-04: updateUser propagates IllegalArgumentException for invalid status string")
    void updateUser_invalidStatus_propagatesException() {
        String userId = "U3";
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User(userId, UserStatus.ACTIVE)));

        assertThrows(
                IllegalArgumentException.class,
                () -> userController.updateUser(userId, "status")
        );

        verify(userRepository, never()).save(any());
    }

    // IT-05: updateUser for missing user
    @Test
    @DisplayName("IT-05: updateUser propagates RuntimeException when user not found")
    void updateUser_missingUser_propagatesException() {
        String userId = "U?";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userController.updateUser(userId, "ACTIVE")
        );
        assertEquals("User not found!", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    // IT-06: createrUser with null id
    @Test
    @DisplayName("IT-06: createrUser with null id propagates NullPointerException from service")
    void createrUser_nullId_propagatesNpe() {
        assertThrows(
                NullPointerException.class,
                () -> userController.createrUser(null)
        );

        verifyNoInteractions(userRepository, planRepository);
    }
}
