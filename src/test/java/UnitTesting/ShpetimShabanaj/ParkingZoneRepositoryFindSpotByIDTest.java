package UnitTesting.ShpetimShabanaj;

import Model.ParkingSpot;
import Model.ParkingZone;
import Repository.ParkingZoneRepository;
import Repository.impl.InMemoryParkingZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParkingZoneRepositoryFindSpotByIDTest {
    private ParkingZoneRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingZoneRepository();
    }

    // TC-01 & TC-02
    @Test
    @DisplayName("TC-01 & TC-02: Should find a spot regardless of which zone it belongs to")
    void testFindSpotInDifferentZones() {
        // Arrange: Zone 1 with Spot 1
        String id1 = "S1";
        ParkingSpot spotA = mock(ParkingSpot.class);
        when(spotA.getSpotId()).thenReturn(id1);

        ParkingZone zone1 = mock(ParkingZone.class);
        when(zone1.getZoneId()).thenReturn("Z1");
        when(zone1.getSpots()).thenReturn(List.of(spotA));

        // Arrange: Zone 2 with Spot 2
        String id2 = "S2";
        ParkingSpot spotB = mock(ParkingSpot.class);
        when(spotB.getSpotId()).thenReturn(id2);

        ParkingZone zone2 = mock(ParkingZone.class);
        when(zone2.getZoneId()).thenReturn("Z2");
        when(zone2.getSpots()).thenReturn(List.of(spotB));

        repository.save(zone1);
        repository.save(zone2);

        // Act & Assert
        assertEquals(spotA, repository.findSpotById(id1));
        assertEquals(spotB, repository.findSpotById(id2));
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should return null if the spot ID is not found in any zone")
    void testFindSpotNotFound() {
        ParkingZone zone = mock(ParkingZone.class);
        when(zone.getZoneId()).thenReturn("Z1");

        ParkingSpot spot1 = mock(ParkingSpot.class);
        when(spot1.getSpotId()).thenReturn("S1");

        when(zone.getSpots()).thenReturn(List.of(spot1));
        repository.save(zone);

        ParkingSpot result = repository.findSpotById("S2");

        assertNull(result);
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should return null when repository has no zones")
    void testFindSpotInEmptyRepository() {
        ParkingSpot result = repository.findSpotById("S1");

        assertNull(result);
    }

    // TC-05
    @Test
    @DisplayName("TC-05: Should return null when there are zones but no spots")
    void testFindZoneWithExistingZoneButEmptySpots(){
        ParkingZone zone1 = mock(ParkingZone.class);
        when(zone1.getZoneId()).thenReturn("Z1");
        when(zone1.getSpots()).thenReturn(List.of());
        repository.save(zone1);

        ParkingZone zone2 = mock(ParkingZone.class);
        when(zone2.getZoneId()).thenReturn("Z2");
        when(zone2.getSpots()).thenReturn(List.of());
        repository.save(zone2);

        assertNull(repository.findSpotById("S1"));
    }


    //TC-06
    @Test
    @DisplayName("TC-06: Should return null when spot id is null")
    void testWithIdNull(){
        // Arrange: Zone 1 with Spot 1
        String id1 = "S1";
        ParkingSpot spotA = mock(ParkingSpot.class);
        when(spotA.getSpotId()).thenReturn(id1);

        ParkingZone zone1 = mock(ParkingZone.class);
        when(zone1.getZoneId()).thenReturn("Z1");
        when(zone1.getSpots()).thenReturn(List.of(spotA));

        // Arrange: Zone 2 with Spot 2
        String id2 = "S2";
        ParkingSpot spotB = mock(ParkingSpot.class);
        when(spotB.getSpotId()).thenReturn(id2);

        ParkingZone zone2 = mock(ParkingZone.class);
        when(zone2.getZoneId()).thenReturn("Z2");
        when(zone2.getSpots()).thenReturn(List.of(spotB));

        repository.save(zone1);
        repository.save(zone2);

        assertNull(repository.findSpotById(null));
    }
}
