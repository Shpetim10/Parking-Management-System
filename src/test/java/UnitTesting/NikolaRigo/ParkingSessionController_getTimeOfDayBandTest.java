package UnitTesting.NikolaRigo;


import Controller.ParkingSessionController;
import Dto.Session.StartSessionRequestDto;
import Enum.TimeOfDayBand;
import Model.ParkingSession;
import Model.ParkingZone;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import Settings.Settings; // Ensure this is imported to access static fields
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ParkingSessionController_getTimeOfDayBandTest {

    @Mock
    private ParkingSessionRepository sessionRepo;

    @Mock
    private ParkingZoneRepository zoneRepo;

    private ParkingSessionController controller;

    @BeforeEach
    void setUp() {
        controller = new ParkingSessionController(sessionRepo, zoneRepo);
    }

    // -------------------------------------------------------------------
    // Tests for getTimeOfDayBand
    // Logic: Peak if time is >= START and < END
    // -------------------------------------------------------------------

    @Test
    @DisplayName("getTimeOfDayBand: Should return NULL when start time is null")
    void getTimeOfDayBand_NullInput() {
        assertNull(controller.getTimeOfDayBand(null));
    }

    @Test
    @DisplayName("getTimeOfDayBand: Should return PEAK when time is exactly at Start boundary (Inclusive)")
    void getTimeOfDayBand_StartBoundary() {
        // Arrange: Use the exact start time from Settings
        LocalTime peakStart = Settings.START_PEAK_TIME;
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 10, peakStart.getHour(), peakStart.getMinute());

        // Act
        TimeOfDayBand result = controller.getTimeOfDayBand(dateTime);

        // Assert
        assertEquals(TimeOfDayBand.PEAK, result, "Start time should be inclusive for PEAK band");
    }

    @Test
    @DisplayName("getTimeOfDayBand: Should return PEAK when time is in the middle of the range")
    void getTimeOfDayBand_MiddleOfPeak() {
        // Arrange: Add 1 minute to start time to be safely inside
        LocalTime safePeakTime = Settings.START_PEAK_TIME.plusMinutes(1);
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 10, safePeakTime.getHour(), safePeakTime.getMinute());

        // Act
        TimeOfDayBand result = controller.getTimeOfDayBand(dateTime);

        // Assert
        assertEquals(TimeOfDayBand.PEAK, result);
    }

    @Test
    @DisplayName("getTimeOfDayBand: Should return OFF_PEAK when time is exactly at End boundary (Exclusive)")
    void getTimeOfDayBand_EndBoundary() {
        // Arrange: Use the exact end time from Settings
        LocalTime peakEnd = Settings.END_PEAK_TIME;
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 10, peakEnd.getHour(), peakEnd.getMinute());

        // Act
        TimeOfDayBand result = controller.getTimeOfDayBand(dateTime);

        // Assert
        assertEquals(TimeOfDayBand.OFF_PEAK, result, "End time should be exclusive (OFF_PEAK)");
    }

    @Test
    @DisplayName("getTimeOfDayBand: Should return OFF_PEAK when time is clearly before start")
    void getTimeOfDayBand_BeforePeak() {
        // Arrange: Subtract 1 minute from start time
        LocalTime earlyTime = Settings.START_PEAK_TIME.minusMinutes(1);
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 10, earlyTime.getHour(), earlyTime.getMinute());

        // Act
        TimeOfDayBand result = controller.getTimeOfDayBand(dateTime);

        // Assert
        assertEquals(TimeOfDayBand.OFF_PEAK, result);
    }

    @Test
    @DisplayName("getTimeOfDayBand: Should return OFF_PEAK when time is clearly after end")
    void getTimeOfDayBand_AfterPeak() {
        // Arrange: Add 1 minute to end time
        LocalTime lateTime = Settings.END_PEAK_TIME.plusMinutes(1);
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 10, lateTime.getHour(), lateTime.getMinute());

        // Act
        TimeOfDayBand result = controller.getTimeOfDayBand(dateTime);

        // Assert
        assertEquals(TimeOfDayBand.OFF_PEAK, result);
    }
}
