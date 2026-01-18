package UnitTesting.ShpetimShabanaj;

import Model.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleConstructorTest {
    // TC-01
    @Test
    @DisplayName("TC-01: Should initialize correctly with valid plate and user ID")
    void testConstructorValid() {
        String expectedPlate = "01-123-AB";
        String expectedUser = "USER-77";

        Vehicle vehicle = new Vehicle(expectedPlate, expectedUser);

        assertAll("Verify Vehicle properties",
                () -> assertEquals(expectedPlate, vehicle.getPlateNumber()),
                () -> assertEquals(expectedUser, vehicle.getUserId())
        );
    }

    // TC-02 & TC-03
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideNullInputs")
    @DisplayName("TC-02 & TC-03: Should throw NullPointerException for null fields")
    void testConstructorNullChecks(String testName, String plate, String user) {
        assertThrows(NullPointerException.class, () ->
                new Vehicle(plate, user)
        );
    }

    private static Stream<Arguments> provideNullInputs() {
        return Stream.of(
                Arguments.of("TC-02: Null Plate Number", null, "U101"),
                Arguments.of("TC-03: Null User ID", "AB-123", null)
        );
    }
}