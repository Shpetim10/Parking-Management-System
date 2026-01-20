package UnitTesting.NikolaRigo;

import Controller.ParkingZoneController;
import Dto.Zone.ParkingSpotDto;
import Model.ParkingSpot;
import Model.ParkingZone;
import Repository.ParkingZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingZoneControllerTest {

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @InjectMocks
    private ParkingZoneController parkingZoneController;

    @Test
    @DisplayName("addSpot: Should successfully add spot when inputs are valid")
    void addSpot_Success() {
        // Arrange
        String zoneId = "zone-A";
        String spotId = "spot-101";

        // Assume DTO has a constructor or is a Record
        ParkingSpotDto dto = new ParkingSpotDto(spotId, zoneId);

        // Mock the Zone model so we can verify the 'addSpot' method is called on it
        ParkingZone mockZone = mock(ParkingZone.class);

        // Define Repository behavior
        when(parkingZoneRepository.spotExists(spotId)).thenReturn(false);
        when(parkingZoneRepository.findZoneById(zoneId)).thenReturn(mockZone);

        // Act
        parkingZoneController.addSpot(dto);

        // Assert
        // 1. Verify we checked if spot exists
        verify(parkingZoneRepository).spotExists(spotId);

        // 2. Verify we fetched the zone
        verify(parkingZoneRepository).findZoneById(zoneId);

        // 3. Verify the spot was actually added to the zone object
        verify(mockZone).addSpot(any(ParkingSpot.class));
    }

    @Test
    @DisplayName("addSpot: Should throw IllegalArgumentException when spot ID already exists")
    void addSpot_SpotAlreadyExists() {
        // Arrange
        String zoneId = "zone-B";
        String existingSpotId = "spot-999";

        ParkingSpotDto dto = new ParkingSpotDto(existingSpotId, zoneId);

        // Mock repo to say "Yes, this spot exists"
        when(parkingZoneRepository.spotExists(existingSpotId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parkingZoneController.addSpot(dto);
        });

        assertEquals("A Parking Spot with this id already exists", exception.getMessage());

        // Verify that we never tried to look up the zone or add the spot
        verify(parkingZoneRepository, never()).findZoneById(anyString());
    }
}
