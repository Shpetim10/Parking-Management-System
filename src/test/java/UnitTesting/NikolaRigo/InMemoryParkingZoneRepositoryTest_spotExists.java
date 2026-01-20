package UnitTesting.NikolaRigo;

import Model.ParkingSpot;
import Model.ParkingZone;
import Repository.impl.InMemoryParkingZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InMemoryParkingZoneRepositoryTest_spotExists {

    private InMemoryParkingZoneRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingZoneRepository();
    }

    @Test
    void withExistingSpot_ShouldReturnTrue() {
        // Arrange
        ParkingSpot mockSpot = mock(ParkingSpot.class);
        when(mockSpot.getSpotId()).thenReturn("spot-1");

        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneId()).thenReturn("zone-1");
        when(mockZone.getSpots()).thenReturn(Collections.singletonList(mockSpot));

        repository.save(mockZone);

        // Act
        boolean exists = repository.spotExists("spot-1");

        // Assert
        assertTrue(exists);
        verify(mockZone, atLeastOnce()).getSpots();
        verify(mockSpot, atLeastOnce()).getSpotId();
    }

    @Test
    void withNonExistentSpot_ShouldReturnFalse() {
        // Arrange
        ParkingSpot mockSpot = mock(ParkingSpot.class);
        when(mockSpot.getSpotId()).thenReturn("spot-1");

        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneId()).thenReturn("zone-1");
        when(mockZone.getSpots()).thenReturn(Collections.singletonList(mockSpot));

        repository.save(mockZone);

        // Act
        boolean exists = repository.spotExists("spot-999");

        // Assert
        assertFalse(exists);
    }

    @Test
    void withEmptyRepository_ShouldReturnFalse() {
        // Act
        boolean exists = repository.spotExists("spot-1");

        // Assert
        assertFalse(exists);
    }

    @Test
    void withMultipleSpotsInSameZone_ShouldFindCorrectSpot() {
        // Arrange
        ParkingSpot mockSpot1 = mock(ParkingSpot.class);
        when(mockSpot1.getSpotId()).thenReturn("spot-1");

        ParkingSpot mockSpot2 = mock(ParkingSpot.class);
        when(mockSpot2.getSpotId()).thenReturn("spot-2");

        ParkingSpot mockSpot3 = mock(ParkingSpot.class);
        when(mockSpot3.getSpotId()).thenReturn("spot-3");

        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneId()).thenReturn("zone-1");
        when(mockZone.getSpots()).thenReturn(Arrays.asList(mockSpot1, mockSpot2, mockSpot3));

        repository.save(mockZone);

        // Act
        boolean exists = repository.spotExists("spot-2");

        // Assert
        assertTrue(exists);
    }

    @Test
    void withMultipleZones_ShouldSearchAcrossAllZones() {
        // Arrange
        ParkingSpot mockSpot1 = mock(ParkingSpot.class);
        when(mockSpot1.getSpotId()).thenReturn("spot-1");

        ParkingSpot mockSpot2 = mock(ParkingSpot.class);
        when(mockSpot2.getSpotId()).thenReturn("spot-2");

        ParkingZone mockZone1 = mock(ParkingZone.class);
        when(mockZone1.getZoneId()).thenReturn("zone-1");
        when(mockZone1.getSpots()).thenReturn(Collections.singletonList(mockSpot1));

        ParkingZone mockZone2 = mock(ParkingZone.class);
        when(mockZone2.getZoneId()).thenReturn("zone-2");
        when(mockZone2.getSpots()).thenReturn(Collections.singletonList(mockSpot2));

        repository.save(mockZone1);
        repository.save(mockZone2);

        // Act
        boolean existsInZone1 = repository.spotExists("spot-1");
        boolean existsInZone2 = repository.spotExists("spot-2");

        // Assert
        assertTrue(existsInZone1);
        assertTrue(existsInZone2);
    }

    @Test
    void withZoneHavingNoSpots_ShouldReturnFalse() {
        // Arrange
        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneId()).thenReturn("zone-1");
        when(mockZone.getSpots()).thenReturn(Collections.emptyList());

        repository.save(mockZone);

        // Act
        boolean exists = repository.spotExists("spot-1");

        // Assert
        assertFalse(exists);
    }

    @Test
    void withNullSpotId_ShouldReturnFalse() {
        // Arrange
        ParkingSpot mockSpot = mock(ParkingSpot.class);
        when(mockSpot.getSpotId()).thenReturn("spot-1");

        ParkingZone mockZone = mock(ParkingZone.class);
        when(mockZone.getZoneId()).thenReturn("zone-1");
        when(mockZone.getSpots()).thenReturn(Collections.singletonList(mockSpot));

        repository.save(mockZone);

        // Act
        boolean exists = repository.spotExists(null);

        // Assert
        assertFalse(exists);
    }

    @Test
    void withSpotInLastZone_ShouldStillFindIt() {
        // Arrange
        ParkingSpot mockSpot1 = mock(ParkingSpot.class);
        when(mockSpot1.getSpotId()).thenReturn("spot-1");

        ParkingSpot mockSpot2 = mock(ParkingSpot.class);
        when(mockSpot2.getSpotId()).thenReturn("spot-2");

        ParkingSpot mockSpot3 = mock(ParkingSpot.class);
        when(mockSpot3.getSpotId()).thenReturn("spot-3");

        ParkingZone mockZone1 = mock(ParkingZone.class);
        when(mockZone1.getZoneId()).thenReturn("zone-1");
        when(mockZone1.getSpots()).thenReturn(Collections.singletonList(mockSpot1));

        ParkingZone mockZone2 = mock(ParkingZone.class);
        when(mockZone2.getZoneId()).thenReturn("zone-2");
        when(mockZone2.getSpots()).thenReturn(Collections.singletonList(mockSpot2));

        ParkingZone mockZone3 = mock(ParkingZone.class);
        when(mockZone3.getZoneId()).thenReturn("zone-3");
        when(mockZone3.getSpots()).thenReturn(Collections.singletonList(mockSpot3));

        repository.save(mockZone1);
        repository.save(mockZone2);
        repository.save(mockZone3);

        // Act
        boolean exists = repository.spotExists("spot-3");

        // Assert
        assertTrue(exists);
    }
}
