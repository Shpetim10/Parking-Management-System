package IntegrationTesting.NikolaRigo;

import Controller.VehicleController;
import Dto.Creation.CreateVehicleDto;
import Enum.UserStatus;
import Model.User;
import Model.Vehicle;
import Repository.UserRepository;
import Repository.VehicleRepository;
import Repository.impl.InMemoryUserRepository;
import Repository.impl.InMemoryVehicleRepository;
import Service.VehicleService;
import Service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VehicleControllerIT {

    private VehicleController vehicleController;
    private VehicleRepository vehicleRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 1. Initialize Repositories
        vehicleRepository = new InMemoryVehicleRepository();
        userRepository = new InMemoryUserRepository();

        // 2. Initialize Service
        VehicleService vehicleService = new VehicleServiceImpl(vehicleRepository, userRepository);

        // 3. Initialize Controller
        vehicleController = new VehicleController(vehicleService);

        // 4. Seed Data: Create a user with a valid status
        // Ensure you import Enum.UserStatus
        User user = new User("user-1", UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Test
    void createVehicle_whenValidRequest_shouldPersistVehicle() {
        // Arrange
        String userId = "user-1";
        String plateNumber = "ABC-123";

        // Act
        vehicleController.createVehicle(userId, plateNumber);

        // Assert
        Optional<Vehicle> savedVehicle = vehicleRepository.findByPlate(plateNumber);

        assertTrue(savedVehicle.isPresent(), "Vehicle should be saved in the repository");
        assertEquals(userId, savedVehicle.get().getUserId());
        assertEquals(plateNumber, savedVehicle.get().getPlateNumber());
    }

    @Test
    void createVehicle_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        String nonExistentUser = "user-ghost";
        String plateNumber = "GHO-000";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleController.createVehicle(nonExistentUser, plateNumber);
        });

        assertEquals("User with this id does not exist!", exception.getMessage());

        // Verify nothing was saved
        assertFalse(vehicleRepository.findByPlate(plateNumber).isPresent());
    }

    @Test
    void createVehicle_whenPlateAlreadyExists_shouldThrowException() {
        // Arrange
        String userId = "user-1";
        String plateNumber = "XYZ-999";

        // Create the first time successfully
        vehicleController.createVehicle(userId, plateNumber);

        // Act & Assert
        // Attempt to create the same vehicle again
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleController.createVehicle(userId, plateNumber);
        });

        assertEquals("A vehicle with this plate number already exists!", exception.getMessage());
    }
    @Test
    void createVehicle_whenSameUserAddsMultipleVehicles_shouldSucceed() {
        // Arrange
        String userId = "user-1";
        String plate1 = "CAR-001";
        String plate2 = "CAR-002";

        // Act
        vehicleController.createVehicle(userId, plate1);
        vehicleController.createVehicle(userId, plate2);

        // Assert
        assertTrue(vehicleRepository.exists(plate1), "First car should exist");
        assertTrue(vehicleRepository.exists(plate2), "Second car should exist");

        // Verify ownership
        assertEquals(userId, vehicleRepository.findByPlate(plate1).get().getUserId());
        assertEquals(userId, vehicleRepository.findByPlate(plate2).get().getUserId());
    }

    @Test
    void createVehicle_whenPlateDiffersOnlyByCase_shouldBeTreatedAsUnique() {
        // Arrange
        String userId = "user-1";
        String plateLower = "abc-123";
        String plateUpper = "ABC-123";

        // Act
        vehicleController.createVehicle(userId, plateLower);
        vehicleController.createVehicle(userId, plateUpper);

        // Assert
        assertTrue(vehicleRepository.exists(plateLower));
        assertTrue(vehicleRepository.exists(plateUpper));

        // They should be distinct objects
        assertNotEquals(
                vehicleRepository.findByPlate(plateLower).get(),
                vehicleRepository.findByPlate(plateUpper).get()
        );
    }

    @Test
    void createVehicle_whenPlateIsNull_shouldThrowNullPointerException() {
        // Arrange
        String userId = "user-1";

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            vehicleController.createVehicle(userId, null);
        });
    }

    @Test
    void createVehicle_whenUserIdIsNull_shouldThrowUserNotFound() {
        // Arrange
        String plateNumber = "ABC-123";

        // Act & Assert
        // Fails because 'null' user does not exist in the repository
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleController.createVehicle(null, plateNumber);
        });

        assertEquals("User with this id does not exist!", exception.getMessage());
    }
}