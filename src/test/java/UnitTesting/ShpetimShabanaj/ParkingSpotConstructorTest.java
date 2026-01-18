package UnitTesting.ShpetimShabanaj;

import Enum.SpotState;
import Model.ParkingSpot;
import Model.ParkingZone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ParkingSpotConstructorTest {

    private final ParkingZone mockZone = mock(ParkingZone.class);

    // TC-01
    @Test
    @DisplayName("TC-01: Should initialize correctly with valid parameters")
    void testConstructorValid() {
        String spotId = "P-101";

        ParkingSpot spot = new ParkingSpot(spotId, mockZone);

        assertAll("Verify initial state",
                () -> assertEquals(spotId, spot.getSpotId()),
                () -> assertEquals(mockZone, spot.getParkingZone()),
                () -> assertEquals(SpotState.FREE, spot.getState(), "New spot must default to FREE"),
                () -> assertTrue(spot.isFree())
        );
    }

    // TC-02 & TC-03
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("TC-02 & TC-03: Should throw exception for null, empty, or blank Spot ID")
    void testConstructorInvalidId(String invalidId) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ParkingSpot(invalidId, mockZone)
        );
        assertEquals("Spot ID cannot be null or empty", ex.getMessage());
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should throw exception when ParkingZone is null")
    void testConstructorNullZone() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ParkingSpot("P1", null)
        );
        assertEquals("Zone type cannot be null", ex.getMessage());
    }
}