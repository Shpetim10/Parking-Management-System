package UnitTesting.ArtjolZaimi;

import Controller.ParkingSessionController;
import Enum.DayType;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

// Unit Tests for M-114: ParkingSessionController.getDayType

class ParkingSessionControllerGetDayTypeTest {

    private ParkingSessionController controller;

    @Mock
    private ParkingSessionRepository mockSessionRepo;

    @Mock
    private ParkingZoneRepository mockZoneRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ParkingSessionController(mockSessionRepo, mockZoneRepo);
    }

    //I am testing only this one once cause Friday and Monday belong to the same equivalence class
    @Test
    @DisplayName("returns WEEKDAY for Monday")
    void testGetDayType_Monday() {
        LocalDateTime monday = LocalDateTime.of(2025, 1, 20, 10, 0);

        DayType dayType = controller.getDayType(monday, false);

        assertEquals(DayType.WEEKDAY, dayType);
    }

    //The same logic here, I am testing only Saturday cause it already proves weekend logic
    @Test
    @DisplayName("returns WEEKEND for Saturday")
    void testGetDayType_Saturday() {
        LocalDateTime saturday = LocalDateTime.of(2025, 1, 18, 10, 0);

        DayType dayType = controller.getDayType(saturday, false);

        assertEquals(DayType.WEEKEND, dayType);
    }


    @Test
    @DisplayName("returns HOLIDAY when isHoliday is true")
    void testGetDayType_Holiday() {
        LocalDateTime monday = LocalDateTime.of(2025, 1, 20, 10, 0);

        DayType dayType = controller.getDayType(monday, true);

        assertEquals(DayType.HOLIDAY, dayType);
    }

    @Test
    @DisplayName("returns HOLIDAY even on weekend when isHoliday is true")
    void testGetDayType_HolidayOnWeekend() {
        LocalDateTime saturday = LocalDateTime.of(2025, 1, 18, 10, 0);

        DayType dayType = controller.getDayType(saturday, true);

        assertEquals(DayType.HOLIDAY, dayType);
    }

    @Test
    @DisplayName("returns null for null startTime")
    void testGetDayType_NullStartTime() {
        DayType dayType = controller.getDayType(null, false);

        assertNull(dayType);
    }

}