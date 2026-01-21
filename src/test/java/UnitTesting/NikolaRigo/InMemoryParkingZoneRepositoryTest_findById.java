package UnitTesting.NikolaRigo;

import Model.ParkingZone;
import Repository.impl.InMemoryParkingZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryParkingZoneRepositoryTest_findById {

    private InMemoryParkingZoneRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingZoneRepository();
    }

    @Test
    void withValidZoneId_ShouldReturnZone() {
        // Arrange
        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneId()).thenReturn("zone-1");

        repository.save(mockZone);

        // Act
        ParkingZone result = repository.findById("zone-1");

        // Assert
        assertNotNull(result);
        assertEquals(mockZone, result);
        verify(mockZone, atLeastOnce()).getZoneId();
    }

    @Test
    void withNonExistentZoneId_ShouldThrowIllegalArgumentException() {
        // Arrange
        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneId()).thenReturn("zone-1");

        repository.save(mockZone);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.findById("non-existent-zone")
        );

        assertEquals("No parking zone found with id: non-existent-zone", exception.getMessage());
    }

    @Test
    void withNullZoneId_ShouldThrowNullPointerException() {
        // Act & Assert
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> repository.findById(null)
        );

        assertEquals("zoneId must not be null", exception.getMessage());
    }

    @Test
    void withMultipleZones_ShouldReturnCorrectZone() {
        // Arrange
        ParkingZone mockZone1 = mock(ParkingZone.class);
        when(mockZone1.getZoneId()).thenReturn("zone-1");

        ParkingZone mockZone2 = mock(ParkingZone.class);
        when(mockZone2.getZoneId()).thenReturn("zone-2");

        ParkingZone mockZone3 = mock(ParkingZone.class);
        when(mockZone3.getZoneId()).thenReturn("zone-3");

        repository.save(mockZone1);
        repository.save(mockZone2);
        repository.save(mockZone3);

        // Act
        ParkingZone result = repository.findById("zone-2");

        // Assert
        assertNotNull(result);
        assertEquals(mockZone2, result);
    }

    @Test
    void withEmptyRepository_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.findById("zone-1")
        );

        assertEquals("No parking zone found with id: zone-1", exception.getMessage());
    }

    @Test
    void afterSavingZone_ShouldBeAbleToFindIt() {
        // Arrange
        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneId()).thenReturn("zone-1");

        // Act
        repository.save(mockZone);
        ParkingZone result = repository.findById("zone-1");

        // Assert
        assertNotNull(result);
        assertSame(mockZone, result);
    }
}