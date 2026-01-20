package Artjol.UnitTesting;

import Controller.MonitoringController;
import Dto.Monitoring.LogEventDto;
import Model.LogEvent;
import Repository.PenaltyHistoryRepository;
import Repository.ParkingZoneRepository;
import Service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;

// Unit Tests for M-108: MonitoringController.logEvent

class MonitoringControllerLogEventTest {

    private MonitoringController controller;

    @Mock
    private MonitoringService mockMonitoringService;

    @Mock
    private PenaltyHistoryRepository mockPenaltyRepo;

    @Mock
    private ParkingZoneRepository mockZoneRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new MonitoringController(
                mockMonitoringService,
                mockPenaltyRepo,
                mockZoneRepo
        );
    }

    @Test
    @DisplayName("logs event successfully")
    void testLogEvent_Success() {
        LocalDateTime now = LocalDateTime.now();
        LogEventDto dto = new LogEventDto(now, "SESSION_START", "Session started for user-1");

        controller.logEvent(dto);

        verify(mockMonitoringService).logEvent(any(LogEvent.class));
    }

    @Test
    @DisplayName("throws exception for null dto")
    void testLogEvent_NullDto() {
        assertThrows(NullPointerException.class, () -> {
            controller.logEvent(null);
        });
    }

    @Test
    @DisplayName("creates log event with correct values")
    void testLogEvent_CorrectValues() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 15, 10, 30);
        LogEventDto dto = new LogEventDto(timestamp, "PAYMENT_SUCCESS", "Payment completed");
        ArgumentCaptor<LogEvent> captor = ArgumentCaptor.forClass(LogEvent.class);

        controller.logEvent(dto);

        verify(mockMonitoringService).logEvent(captor.capture());
        LogEvent logged = captor.getValue();
        assertEquals(timestamp, logged.getTimestamp());
        assertEquals("PAYMENT_SUCCESS", logged.getType());
        assertEquals("Payment completed", logged.getDetails());
    }

    @Test
    @DisplayName("logs event with empty details")
    void testLogEvent_EmptyDetails() {
        LogEventDto dto = new LogEventDto(LocalDateTime.now(), "TEST_EVENT", "");

        controller.logEvent(dto);

        verify(mockMonitoringService).logEvent(any(LogEvent.class));
    }


    @Test
    @DisplayName("logs multiple events")
    void testLogEvent_MultipleEvents() {
        LogEventDto dto1 = new LogEventDto(LocalDateTime.now(), "EVENT1", "Details1");
        LogEventDto dto2 = new LogEventDto(LocalDateTime.now(), "EVENT2", "Details2");

        controller.logEvent(dto1);
        controller.logEvent(dto2);

        verify(mockMonitoringService, times(2)).logEvent(any(LogEvent.class));
    }
}