package UnitTesting.NikolaRigo;

import Model.Vehicle;
import Repository.impl.InMemoryVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryVehicleRepositoryTest_save {

    private InMemoryVehicleRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryVehicleRepository();
    }

    @Test
    void withValidVehicle_ShouldSaveSuccessfully() {
        // Arrange
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getPlateNumber()).thenReturn("ABC-1234");

        // Act
        repository.save(mockVehicle);

        // Assert
        Optional<Vehicle> result = repository.findByPlate("ABC-1234");
        assertTrue(result.isPresent());
        assertSame(mockVehicle, result.get());
        verify(mockVehicle, atLeastOnce()).getPlateNumber();
    }

    @Test
    void afterSaving_ShouldBeAbleToRetrieve() {
        // Arrange
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getPlateNumber()).thenReturn("XYZ-5678");

        // Act
        repository.save(mockVehicle);
        Optional<Vehicle> result = repository.findByPlate("XYZ-5678");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockVehicle, result.get());
    }

    @Test
    void withMultipleVehicles_ShouldSaveAll() {
        // Arrange
        Vehicle mockVehicle1 = mock(Vehicle.class);
        when(mockVehicle1.getPlateNumber()).thenReturn("ABC-1111");

        Vehicle mockVehicle2 = mock(Vehicle.class);
        when(mockVehicle2.getPlateNumber()).thenReturn("ABC-2222");

        Vehicle mockVehicle3 = mock(Vehicle.class);
        when(mockVehicle3.getPlateNumber()).thenReturn("ABC-3333");

        // Act
        repository.save(mockVehicle1);
        repository.save(mockVehicle2);
        repository.save(mockVehicle3);

        // Assert
        Optional<Vehicle> result1 = repository.findByPlate("ABC-1111");
        Optional<Vehicle> result2 = repository.findByPlate("ABC-2222");
        Optional<Vehicle> result3 = repository.findByPlate("ABC-3333");

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertSame(mockVehicle1, result1.get());
        assertSame(mockVehicle2, result2.get());
        assertSame(mockVehicle3, result3.get());
    }

    @Test
    void withNullVehicle_ShouldThrowNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> repository.save(null));
    }

    @Test
    void withVehicleHavingNullPlateNumber_ShouldStoreWithNullKey() {
        // Arrange
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getPlateNumber()).thenReturn(null);

        // Act
        repository.save(mockVehicle);

        // Assert
        Optional<Vehicle> result = repository.findByPlate(null);
        assertTrue(result.isPresent());
        assertSame(mockVehicle, result.get());
    }

    @Test
    void withEmptyStringPlateNumber_ShouldSaveSuccessfully() {
        // Arrange
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getPlateNumber()).thenReturn("");

        // Act
        repository.save(mockVehicle);

        // Assert
        Optional<Vehicle> result = repository.findByPlate("");
        assertTrue(result.isPresent());
        assertSame(mockVehicle, result.get());
    }

    @Test
    void shouldStoreExactSameInstance() {
        // Arrange
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getPlateNumber()).thenReturn("ABC-1234");

        // Act
        repository.save(mockVehicle);
        Optional<Vehicle> result1 = repository.findByPlate("ABC-1234");
        Optional<Vehicle> result2 = repository.findByPlate("ABC-1234");

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertSame(mockVehicle, result1.get());
        assertSame(result1.get(), result2.get());
    }

    @Test
    void withDifferentPlateNumbers_ShouldNotInterfere() {
        // Arrange
        Vehicle mockVehicle1 = mock(Vehicle.class);
        when(mockVehicle1.getPlateNumber()).thenReturn("ABC-1234");

        Vehicle mockVehicle2 = mock(Vehicle.class);
        when(mockVehicle2.getPlateNumber()).thenReturn("XYZ-5678");

        // Act
        repository.save(mockVehicle1);
        repository.save(mockVehicle2);

        // Assert
        Optional<Vehicle> result1 = repository.findByPlate("ABC-1234");
        Optional<Vehicle> result2 = repository.findByPlate("XYZ-5678");

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertSame(mockVehicle1, result1.get());
        assertSame(mockVehicle2, result2.get());
        assertNotSame(result1.get(), result2.get());
    }
}