package UnitTesting.ShpetimShabanaj;

import Model.ParkingSpot;
import Model.ParkingZone;
import Enum.SpotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ParkingSpotReleaseTest {
    String spotId="S1";
    ParkingZone parkingZone= mock(ParkingZone.class);
    ParkingSpot parkingSpot=new  ParkingSpot(spotId,parkingZone);

    //TC-01
    @Test
    @DisplayName("TC-01: Verify successful release of an occupied spot")
    void testWhenStatusIsOccupied(){
        parkingSpot.setState(SpotState.OCCUPIED);

        assertDoesNotThrow(()->parkingSpot.release());

        assertAll("The status should be FREE",
                ()->assertEquals(SpotState.FREE, parkingSpot.getState()),
                ()->assertFalse(parkingSpot.isOccupied()),
                ()->assertTrue(parkingSpot.isFree()));
    }

    //TC-02 & TC-03
    @ParameterizedTest
    @CsvSource({"RESERVED","FREE"})
    @DisplayName("TC-02 and 03: Should fail when status not OCCUPIED")
    void testWhenStatusIsNotOccupied(String reason){
        SpotState state=SpotState.valueOf(reason);
        parkingSpot.setState(state);

        IllegalStateException ex= assertThrows(IllegalStateException.class, ()->parkingSpot.release());

        assertEquals("Spot can only be released if it is OCCUPIED",ex.getMessage());
    }



}
