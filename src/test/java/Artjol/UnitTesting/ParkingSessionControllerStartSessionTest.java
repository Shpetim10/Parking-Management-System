package Artjol.UnitTesting;

import Controller.ParkingSessionController;
import Dto.Session.StartSessionRequestDto;
import Dto.Session.StartSessionResponseDto;
import Enum.*;
import Model.*;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;

// Unit Tests for M-111: ParkingSessionController.startSession

class ParkingSessionControllerStartSessionTest {

    private ParkingSessionController controller;
    private ParkingSessionRepository mockSessionRepo;
    private ParkingZoneRepository mockZoneRepo;

    @BeforeEach
    void setUp() {
        mockSessionRepo = mock(ParkingSessionRepository.class);
        mockZoneRepo = mock(ParkingZoneRepository.class);
        controller = new ParkingSessionController(mockSessionRepo, mockZoneRepo);
    }

    @Test
    @DisplayName("starts session successfully")
    void testStartSession_Success() {
        LocalDateTime startTime = LocalDateTime.of(
                2025,   // year
                1,      // month
                20,     // day
                10,     // hour
                30      // minute
        );

        StartSessionRequestDto dto = new StartSessionRequestDto(
                "user-1",
                "ABC123",
                "zone-1",
                "spot-1",
                ZoneType.STANDARD,
                false,
                startTime
        );

        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("spot-1", zone);
        zone.addSpot(spot);

        when(mockZoneRepo.findById("zone-1")).thenReturn(zone);

        StartSessionResponseDto response = controller.startSession(dto);

        assertNotNull(response);
        assertNotNull(response.sessionId());
        assertEquals(SessionState.OPEN, response.state());
        verify(mockSessionRepo).save(any(ParkingSession.class));
    }

    @Test
    @DisplayName("throws exception for null dto")
    void testStartSession_NullDto() {
        assertThrows(NullPointerException.class, () -> {
            controller.startSession(null);
        });
    }

    @Test
    @DisplayName("occupies spot")
    void testStartSession_OccupiesSpot() {
        LocalDateTime now = LocalDateTime.now();

        StartSessionRequestDto dto = new StartSessionRequestDto(
                "user-1",
                "ABC123",
                "zone-1",
                "spot-1",
                ZoneType.STANDARD,
                false,  // isHoliday
                now     // startTime
        );

        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("spot-1", zone);
        zone.addSpot(spot);

        when(mockZoneRepo.findById("zone-1")).thenReturn(zone);

        controller.startSession(dto);

        assertTrue(spot.isOccupied());
    }

    @Test
    @DisplayName("determines correct day type for weekday")
    void testStartSession_WeekdayDayType() {
        LocalDateTime weekday = LocalDateTime.of(2025, 1, 20, 10, 0); // Monday

        StartSessionRequestDto dto = new StartSessionRequestDto(
                "user-1",
                "ABC123",
                "zone-1",
                "spot-1",
                ZoneType.STANDARD,
                false,      // isHoliday
                weekday     // startTime
        );

        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("spot-1", zone);
        zone.addSpot(spot);

        when(mockZoneRepo.findById("zone-1")).thenReturn(zone);

        StartSessionResponseDto response = controller.startSession(dto);

        assertEquals(DayType.WEEKDAY, response.dayType());
    }

    @Test
    @DisplayName("determines correct day type for holiday")
    void testStartSession_HolidayDayType() {
        LocalDateTime now = LocalDateTime.now();

        StartSessionRequestDto dto = new StartSessionRequestDto(
                "user-1",
                "ABC123",
                "zone-1",
                "spot-1",
                ZoneType.STANDARD,
                true,   // isHoliday
                now     // startTime
        );

        ParkingZone zone = new ParkingZone("zone-1", ZoneType.STANDARD, 0.8);
        ParkingSpot spot = new ParkingSpot("spot-1", zone);
        zone.addSpot(spot);

        when(mockZoneRepo.findById("zone-1")).thenReturn(zone);

        StartSessionResponseDto response = controller.startSession(dto);

        assertEquals(DayType.HOLIDAY, response.dayType());
    }
}