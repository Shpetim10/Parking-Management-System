package Controller;

import Model.ParkingSession;
import Repository.ParkingSessionRepository;
import Repository.ParkingZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingSessionController - CloseSession Method Tests")
class ParkingSessionControllerCloseSessionTest {

    @Mock
    private ParkingSessionRepository sessionRepo;

    @Mock
    private ParkingZoneRepository zoneRepo;

    @InjectMocks
    private ParkingSessionController controller;

    private ParkingSession mockSession;
    private LocalDateTime testEndTime;
    private String testSessionId;

    @BeforeEach
    void setUp() {
        testSessionId = "S1";
        testEndTime = LocalDateTime.of(2026, 1, 15, 14, 30);
        mockSession = mock(ParkingSession.class);
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should close session successfully when session exists")
    void testCloseSession_HappyPath_SessionClosedSuccessfully() {
        when(sessionRepo.findById(testSessionId)).thenReturn(Optional.of(mockSession));

        boolean result = controller.closeSession(testSessionId, testEndTime);

        assertAll("Verify successful session closure",
                () -> assertTrue(result),
                () -> verify(sessionRepo, times(1)).findById(testSessionId),
                () -> verify(mockSession, times(1)).close(testEndTime),
                () -> verify(sessionRepo, times(1)).save(mockSession)
        );
        verifyNoMoreInteractions(sessionRepo, mockSession);
        verifyNoInteractions(zoneRepo);
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should return false when session does not exist")
    void testCloseSession_SessionNotFound_ReturnsFalse() {
        when(sessionRepo.findById(testSessionId)).thenReturn(Optional.empty());

        boolean result = controller.closeSession(testSessionId, testEndTime);

        assertAll("Verify session not found behavior",
                () -> assertFalse(result),
                () -> verify(sessionRepo, times(1)).findById(testSessionId),
                () -> verify(sessionRepo, never()).save(any())
        );
        verifyNoMoreInteractions(sessionRepo);
        verifyNoInteractions(zoneRepo);
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should throw NullPointerException when sessionId is null")
    void testCloseSession_NullSessionId_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> controller.closeSession(null, testEndTime));

        verifyNoInteractions(sessionRepo, zoneRepo);
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should throw NullPointerException when endTime is null")
    void testCloseSession_NullEndTime_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> controller.closeSession(testSessionId, null));

        verifyNoInteractions(sessionRepo, zoneRepo);
    }

    // TC-05
    @Test
    @DisplayName("TC-05: Should throw NullPointerException when both sessionId and endTime are null")
    void testCloseSession_BothParametersNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> controller.closeSession(null, null));

        verifyNoInteractions(sessionRepo, zoneRepo);
    }

    // TC-06
    @Test
    @DisplayName("TC-06: Should call session.close() with the exact endTime provided")
    void testCloseSession_VerifyCloseCalledWithCorrectEndTime() {
        LocalDateTime specificEndTime = LocalDateTime.of(2024, 12, 25, 23, 59, 59);
        when(sessionRepo.findById(testSessionId)).thenReturn(Optional.of(mockSession));

        controller.closeSession(testSessionId, specificEndTime);

        assertAll("Verify close called with exact endTime",
                () -> verify(mockSession, times(1)).close(specificEndTime),
                () -> verify(sessionRepo, times(1)).save(mockSession)
        );
    }

    // TC-07
    @Test
    @DisplayName("TC-07: Should save the exact session object that was retrieved")
    void testCloseSession_VerifySaveCalledWithSameSession() {
        when(sessionRepo.findById(testSessionId)).thenReturn(Optional.of(mockSession));

        ArgumentCaptor<ParkingSession> sessionCaptor = ArgumentCaptor.forClass(ParkingSession.class);

        controller.closeSession(testSessionId, testEndTime);

        verify(sessionRepo).save(sessionCaptor.capture());
        assertSame(mockSession, sessionCaptor.getValue());
    }

    // TC-08
    @Test
    @DisplayName("TC-08: Should handle empty sessionId string")
    void testCloseSession_EmptySessionId_ReturnsBasedOnRepositoryBehavior() {
        String emptySessionId = "";
        when(sessionRepo.findById(emptySessionId)).thenReturn(Optional.empty());

        boolean result = controller.closeSession(emptySessionId, testEndTime);

        assertAll("Verify empty sessionId handling",
                () -> assertFalse(result),
                () -> verify(sessionRepo, times(1)).findById(emptySessionId),
                () -> verify(sessionRepo, never()).save(any())
        );
        verifyNoMoreInteractions(sessionRepo);
        verifyNoInteractions(zoneRepo);
    }

    // TC-09
    @Test
    @DisplayName("TC-09: Should handle endTime in the past")
    void testCloseSession_EndTimeInPast_ProcessedSuccessfully() {
        LocalDateTime pastEndTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        when(sessionRepo.findById(testSessionId)).thenReturn(Optional.of(mockSession));

        boolean result = controller.closeSession(testSessionId, pastEndTime);

        assertAll("Verify past endTime processing",
                () -> assertTrue(result),
                () -> verify(sessionRepo, times(1)).findById(testSessionId),
                () -> verify(mockSession, times(1)).close(pastEndTime),
                () -> verify(sessionRepo, times(1)).save(mockSession)
        );
        verifyNoMoreInteractions(sessionRepo, mockSession);
        verifyNoInteractions(zoneRepo);
    }

    // TC-10
    @Test
    @DisplayName("TC-10: Should handle endTime in the future")
    void testCloseSession_EndTimeInFuture_ProcessedSuccessfully() {
        LocalDateTime futureEndTime = LocalDateTime.of(2030, 12, 31, 23, 59);
        when(sessionRepo.findById(testSessionId)).thenReturn(Optional.of(mockSession));

        boolean result = controller.closeSession(testSessionId, futureEndTime);

        assertAll("Verify future endTime processing",
                () -> assertTrue(result),
                () -> verify(sessionRepo, times(1)).findById(testSessionId),
                () -> verify(mockSession, times(1)).close(futureEndTime),
                () -> verify(sessionRepo, times(1)).save(mockSession)
        );
        verifyNoMoreInteractions(sessionRepo, mockSession);
        verifyNoInteractions(zoneRepo);
    }

    // TC-11
    @Test
    @DisplayName("TC-11: Should handle multiple consecutive close calls for same session")
    void testCloseSession_MultipleConsecutiveCalls_EachCallProcessedIndependently() {
        LocalDateTime firstEndTime = LocalDateTime.of(2026, 1, 15, 14, 0);
        LocalDateTime secondEndTime = LocalDateTime.of(2026, 1, 15, 15, 0);

        when(sessionRepo.findById(testSessionId))
                .thenReturn(Optional.of(mockSession))
                .thenReturn(Optional.of(mockSession));

        boolean firstResult = controller.closeSession(testSessionId, firstEndTime);
        boolean secondResult = controller.closeSession(testSessionId, secondEndTime);

        assertAll("Verify multiple consecutive calls",
                () -> assertTrue(firstResult),
                () -> assertTrue(secondResult),
                () -> verify(sessionRepo, times(2)).findById(testSessionId),
                () -> verify(mockSession, times(1)).close(firstEndTime),
                () -> verify(mockSession, times(1)).close(secondEndTime),
                () -> verify(sessionRepo, times(2)).save(mockSession)
        );
        verifyNoMoreInteractions(sessionRepo, mockSession);
        verifyNoInteractions(zoneRepo);
    }

    // TC-12
    @Test
    @DisplayName("TC-12: Should handle different sessions being closed in sequence")
    void testCloseSession_DifferentSessions_EachProcessedIndependently() {
        String sessionId1 = "S1";
        String sessionId2 = "S2";
        ParkingSession session1 = mock(ParkingSession.class);
        ParkingSession session2 = mock(ParkingSession.class);

        when(sessionRepo.findById(sessionId1)).thenReturn(Optional.of(session1));
        when(sessionRepo.findById(sessionId2)).thenReturn(Optional.of(session2));

        boolean result1 = controller.closeSession(sessionId1, testEndTime);
        boolean result2 = controller.closeSession(sessionId2, testEndTime);

        assertAll("Verify different sessions processed independently",
                () -> assertTrue(result1),
                () -> assertTrue(result2),
                () -> verify(sessionRepo, times(1)).findById(sessionId1),
                () -> verify(sessionRepo, times(1)).findById(sessionId2),
                () -> verify(session1, times(1)).close(testEndTime),
                () -> verify(session2, times(1)).close(testEndTime),
                () -> verify(sessionRepo, times(2)).save(any())
        );
        verifyNoMoreInteractions(sessionRepo);
        verifyNoInteractions(zoneRepo);
    }

    // TC-13
    @Test
    @DisplayName("TC-13: Should handle Optional.ofNullable(null) scenario")
    void testCloseSession_FindByIdReturnsNull_ReturnsFalse() {
        when(sessionRepo.findById(testSessionId)).thenReturn(Optional.ofNullable(null));

        boolean result = controller.closeSession(testSessionId, testEndTime);

        assertAll("Verify Optional.ofNullable(null) handling",
                () -> assertFalse(result),
                () -> verify(sessionRepo, times(1)).findById(testSessionId),
                () -> verify(sessionRepo, never()).save(any())
        );
        verifyNoMoreInteractions(sessionRepo);
        verifyNoInteractions(zoneRepo);
    }
}