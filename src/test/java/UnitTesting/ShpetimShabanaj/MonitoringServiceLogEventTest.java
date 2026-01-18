package UnitTesting.ShpetimShabanaj;

import Model.LogEvent;
import Service.impl.MonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class MonitoringServiceLogEventTest {
    private MonitoringServiceImpl monitoringService;

    @BeforeEach
    void setUp() {
        monitoringService = new MonitoringServiceImpl();
    }

    @Test
    @DisplayName("TC-01: Should add a valid LogEvent to the public logs list")
    void testLogValidEvent() {
        LogEvent mockEvent = mock(LogEvent.class);

        monitoringService.logEvent(mockEvent);

        assertEquals(1, monitoringService.logs.size());
        assertEquals(mockEvent, monitoringService.logs.get(0));
    }

    @Test
    @DisplayName("TC-02: Should not add anything when the event is null")
    void testLogNullEvent() {
        monitoringService.logEvent(null);

        assertTrue(monitoringService.logs.isEmpty());
    }

    @Test
    @DisplayName("TC-03: Should store multiple events in the order they were logged")
    void testLogMultipleEvents() {
        LogEvent firstEvent = mock(LogEvent.class);
        LogEvent secondEvent = mock(LogEvent.class);

        monitoringService.logEvent(firstEvent);
        monitoringService.logEvent(secondEvent);

        assertAll("Verify logs content and order",
                () -> assertEquals(2, monitoringService.logs.size()),
                () -> assertEquals(firstEvent, monitoringService.logs.get(0)),
                () -> assertEquals(secondEvent, monitoringService.logs.get(1))
        );
    }
}
