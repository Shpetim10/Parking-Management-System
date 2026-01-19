package UnitTesting.ShpetimShabanaj;

import Controller.ParkingZoneController;
import Dto.Zone.ParkingZoneDto;
import Enum.ZoneType;
import Model.ParkingZone;
import Repository.ParkingZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingZoneController - CreateParkingZone Method Tests")
class ParkingZoneControllerCreateParkingZoneTest {

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @InjectMocks
    private ParkingZoneController controller;

    private ParkingZoneDto testZoneDto;

    @BeforeEach
    void setUp() {
        testZoneDto = new ParkingZoneDto("Z1", ZoneType.STANDARD.name(), 0.8);
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should create parking zone successfully when zone does not exist")
    void testCreateParkingZoneHappyPathZoneCreatedSuccessfully() {
        when(parkingZoneRepository.zoneExists("Z1")).thenReturn(false);

        controller.createParkingZone(testZoneDto);

        ArgumentCaptor<ParkingZone> zoneCaptor = ArgumentCaptor.forClass(ParkingZone.class);

        assertAll("Verify zone creation",
                () -> verify(parkingZoneRepository, times(1)).zoneExists("Z1"),
                () -> verify(parkingZoneRepository, times(1)).save(zoneCaptor.capture())
        );

        ParkingZone savedZone = zoneCaptor.getValue();
        assertAll("Verify saved zone properties",
                () -> assertEquals("Z1", savedZone.getZoneId()),
                () -> assertEquals(ZoneType.STANDARD, savedZone.getZoneType()),
                () -> assertEquals(0.80, savedZone.getMaxOccupancyThreshold())
        );

        verifyNoMoreInteractions(parkingZoneRepository);
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should throw IllegalArgumentException when zone already exists")
    void testCreateParkingZone_ZoneExistsThrowsIllegalArgumentException() {
        when(parkingZoneRepository.zoneExists("Z1")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.createParkingZone(testZoneDto));

        assertAll("Verify duplicate zone handling",
                () -> assertEquals("A Parking Zone with this id already exists", exception.getMessage()),
                () -> verify(parkingZoneRepository, times(1)).zoneExists("Z1"),
                () -> verify(parkingZoneRepository, never()).save(any())
        );

        verifyNoMoreInteractions(parkingZoneRepository);
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should throw NullPointerException when parkingZoneDto is null")
    void testCreateParkingZoneNullDtoThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> controller.createParkingZone(null));

        verifyNoInteractions(parkingZoneRepository);
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should create parking zone with correct object construction")
    void testCreateParkingZoneVerifyCorrectObjectConstruction() {
        ParkingZoneDto zoneDto = new ParkingZoneDto("Z2", ZoneType.EV.name(), 0.90);
        when(parkingZoneRepository.zoneExists("Z2")).thenReturn(false);

        controller.createParkingZone(zoneDto);

        ArgumentCaptor<ParkingZone> zoneCaptor = ArgumentCaptor.forClass(ParkingZone.class);
        verify(parkingZoneRepository).save(zoneCaptor.capture());

        ParkingZone savedZone = zoneCaptor.getValue();
        assertAll("Verify zone constructed with correct values",
                () -> assertEquals("Z2", savedZone.getZoneId()),
                () -> assertEquals(ZoneType.EV, savedZone.getZoneType()),
                () -> assertEquals(0.90, savedZone.getMaxOccupancyThreshold())
        );
    }

    // TC-05
    @Test
    @DisplayName("TC-05: Should create parking zone with different zone types")
    void testCreateParkingZoneDifferentZoneTypesCreatedSuccessfully() {
        ParkingZoneDto zoneDto = new ParkingZoneDto("Z3", ZoneType.VIP.name(), 0.75);
        when(parkingZoneRepository.zoneExists("Z3")).thenReturn(false);

        controller.createParkingZone(zoneDto);

        ArgumentCaptor<ParkingZone> zoneCaptor = ArgumentCaptor.forClass(ParkingZone.class);
        verify(parkingZoneRepository).save(zoneCaptor.capture());

        ParkingZone savedZone = zoneCaptor.getValue();
        assertAll(
                () -> assertEquals("Z3", savedZone.getZoneId()),
                () -> assertEquals(ZoneType.VIP, savedZone.getZoneType()),
                () -> assertEquals(0.75, savedZone.getMaxOccupancyThreshold())
        );
    }

    // TC-06
    @Test
    @DisplayName("TC-06: Should create parking zone with minimum occupancy threshold")
    void testCreateParkingZoneMinimumOccupancyThresholdCreatedSuccessfully() {
        ParkingZoneDto zoneDto = new ParkingZoneDto("Z4", ZoneType.STANDARD.name(), 0);
        when(parkingZoneRepository.zoneExists("Z4")).thenReturn(false);

        controller.createParkingZone(zoneDto);

        ArgumentCaptor<ParkingZone> zoneCaptor = ArgumentCaptor.forClass(ParkingZone.class);
        verify(parkingZoneRepository).save(zoneCaptor.capture());

        ParkingZone savedZone = zoneCaptor.getValue();
        assertAll("Verify minimum threshold",
                () -> assertEquals("Z4", savedZone.getZoneId()),
                () -> assertEquals(0, savedZone.getMaxOccupancyThreshold())
        );
    }

    // TC-07
    @Test
    @DisplayName("TC-07: Should create parking zone with maximum occupancy threshold")
    void testCreateParkingZoneMaximumOccupancyThresholdCreatedSuccessfully() {
        ParkingZoneDto zoneDto = new ParkingZoneDto("Z5", ZoneType.STANDARD.name(), 1.00);
        when(parkingZoneRepository.zoneExists("Z5")).thenReturn(false);

        controller.createParkingZone(zoneDto);

        ArgumentCaptor<ParkingZone> zoneCaptor = ArgumentCaptor.forClass(ParkingZone.class);
        verify(parkingZoneRepository).save(zoneCaptor.capture());

        ParkingZone savedZone = zoneCaptor.getValue();
        assertAll("Verify maximum threshold",
                () -> assertEquals("Z5", savedZone.getZoneId()),
                () -> assertEquals(1.00, savedZone.getMaxOccupancyThreshold())
        );
    }

    // TC-08
    @Test
    @DisplayName("TC-08: Should handle empty zoneId string")
    void testCreateParkingZoneEmptyZoneIdProcessedByRepository() {
        ParkingZoneDto zoneDto = new ParkingZoneDto("", ZoneType.STANDARD.name(), 0.80);
        when(parkingZoneRepository.zoneExists("")).thenReturn(false);

        assertAll("Verify empty zoneId processing",
                () -> assertThrows(IllegalArgumentException.class, () -> controller.createParkingZone(zoneDto)),
                () -> verify(parkingZoneRepository, times(1)).zoneExists(""),
                () -> verify(parkingZoneRepository, times(0)).save(any(ParkingZone.class))
        );
    }

    // TC-09
    @Test
    @DisplayName("TC-09: Should handle null zoneId in DTO")
    void testCreateParkingZoneNullZoneIdCallsRepositoryWithNull() {
        ParkingZoneDto zoneDto = new ParkingZoneDto(null, ZoneType.STANDARD.name(), 0.80);
        when(parkingZoneRepository.zoneExists(null)).thenReturn(false);

        assertAll("Verify null zoneId handling",
                () -> assertThrows(IllegalArgumentException.class, () -> controller.createParkingZone(zoneDto)),
                () -> verify(parkingZoneRepository, times(1)).zoneExists(null),
                () -> verify(parkingZoneRepository, times(0)).save(any(ParkingZone.class))
        );

        verifyNoMoreInteractions(parkingZoneRepository);
    }

    // TC-10
    @Test
    @DisplayName("TC-10: Should handle null zoneType in DTO")
    void testCreateParkingZoneNullZoneTypeSavedWithNullType() {
        assertThrows(NullPointerException.class, () -> controller.createParkingZone(new ParkingZoneDto("Z6", null, 0.80)));
    }
}