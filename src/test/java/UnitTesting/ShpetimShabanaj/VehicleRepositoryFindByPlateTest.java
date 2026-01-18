package UnitTesting.ShpetimShabanaj;

import Model.Vehicle;
import Repository.VehicleRepository;
import Repository.impl.InMemoryVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VehicleRepositoryFindByPlateTest {
    private InMemoryVehicleRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryVehicleRepository();
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should return an Optional containing the vehicle when the plate exists")
    void testFindByPlateExisting() {
        String plate = "AA111";
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getPlateNumber()).thenReturn(plate);
        repository.save(mockVehicle);

        Optional<Vehicle> result = repository.findByPlate(plate);

        assertTrue(result.isPresent());
        assertEquals(mockVehicle, result.get());
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should return an empty Optional when the plate is not found")
    void testFindByPlateNotFound() {
        Optional<Vehicle> result = repository.findByPlate("BB111");

        assertTrue(result.isEmpty());
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should return an empty Optional when the input plate is null")
    void testFindByPlateWithNull() {
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getPlateNumber()).thenReturn("CC111");
        repository.save(mockVehicle);

        Optional<Vehicle> result = repository.findByPlate(null);

        assertTrue(result.isEmpty());
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should return an empty Optional when the input plate is with lower case, and saved with upper case")
    void testFindByPlateForCaseSensitive() {
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getPlateNumber()).thenReturn("CC111");
        repository.save(mockVehicle);

        Optional<Vehicle> result = repository.findByPlate("cc111");

        assertTrue(result.isEmpty());
    }
}
