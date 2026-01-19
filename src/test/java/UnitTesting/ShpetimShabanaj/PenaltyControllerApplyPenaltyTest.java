package UnitTesting.ShpetimShabanaj;

import Controller.PenaltyController;
import Dto.Penalty.ApplyPenaltyRequestDto;
import Dto.Penalty.ApplyPenaltyResponseDto;
import Enum.BlacklistStatus;
import Enum.PenaltyType;
import Model.Penalty;
import Model.PenaltyHistory;
import Repository.PenaltyHistoryRepository;
import Service.MonitoringService;
import Service.PenaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenaltyControllerApplyPenaltyTest {

    @Mock
    private PenaltyService penaltyService;

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private PenaltyHistoryRepository penaltyHistoryRepository;

    @InjectMocks
    private PenaltyController controller;

    @Mock
    private PenaltyHistory mockHistory;

    private ApplyPenaltyRequestDto testDto;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = LocalDateTime.of(2026, 1, 15, 10, 30);
        testDto = new ApplyPenaltyRequestDto(
                "U1",
                PenaltyType.OVERSTAY,
                BigDecimal.valueOf(50.00),
                testTimestamp
        );
    }

    // TC-01
    @Test
    @DisplayName("TC-01: Should apply penalty successfully with all services called")
    void testApplyPenaltyHappyPathPenaltyAppliedSuccessfully() {
        when(penaltyHistoryRepository.getOrCreate("U1")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(150.00));
        when(mockHistory.getPenaltyCount()).thenReturn(3);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U1"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyResponseDto response = controller.applyPenalty(testDto);

        assertAll("Verify penalty application",
                () -> assertEquals("U1", response.userId()),
                () -> assertEquals(BigDecimal.valueOf(150.00), response.newTotalPenaltyAmount()),
                () -> assertEquals(3, response.penaltyCount()),
                () -> assertEquals(BlacklistStatus.NONE, response.blacklistStatus()),
                () -> verify(penaltyHistoryRepository, times(1)).getOrCreate("U1"),
                () -> verify(monitoringService, times(1)).updatePenaltyHistoryAndCheckBlacklist(
                        eq("U1"), any(Penalty.class), eq(mockHistory)
                ),
                () -> verify(penaltyHistoryRepository, times(1)).save("U1", mockHistory)
        );

        verifyNoMoreInteractions(penaltyHistoryRepository, monitoringService);
        verifyNoInteractions(penaltyService);
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should throw NullPointerException when dto is null")
    void testApplyPenaltyNullDtoThrowsNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> controller.applyPenalty(null));

        assertEquals("dto must not be null", exception.getMessage());
        verifyNoInteractions(penaltyService, monitoringService, penaltyHistoryRepository);
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should create Penalty object with correct values")
    void testApplyPenaltyVerifyPenaltyObjectConstruction() {
        LocalDateTime specificTimestamp = LocalDateTime.of(2026, 1, 15, 10, 30);
        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U1",
                PenaltyType.LOST_TICKET,
                BigDecimal.valueOf(100.00),
                specificTimestamp
        );

        when(penaltyHistoryRepository.getOrCreate("U1")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(100.00));
        when(mockHistory.getPenaltyCount()).thenReturn(1);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U1"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        controller.applyPenalty(dto);

        ArgumentCaptor<Penalty> penaltyCaptor = ArgumentCaptor.forClass(Penalty.class);
        verify(monitoringService).updatePenaltyHistoryAndCheckBlacklist(
                eq("U1"), penaltyCaptor.capture(), eq(mockHistory)
        );

        Penalty capturedPenalty = penaltyCaptor.getValue();
        assertAll("Verify penalty construction",
                () -> assertEquals(PenaltyType.LOST_TICKET, capturedPenalty.getType()),
                () -> assertEquals(BigDecimal.valueOf(100.00), capturedPenalty.getAmount()),
                () -> assertEquals(specificTimestamp, capturedPenalty.getTimestamp())
        );
    }

    // TC-04
    @Test
    @DisplayName("TC-04: Should retrieve and use existing penalty history")
    void testApplyPenaltyExistingUserUsesExistingHistory() {
        when(penaltyHistoryRepository.getOrCreate("U2")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(200.00));
        when(mockHistory.getPenaltyCount()).thenReturn(3);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U2"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U2", PenaltyType.OVERSTAY, BigDecimal.valueOf(50.00), testTimestamp
        );

        controller.applyPenalty(dto);

        assertAll("Verify existing history handling",
                () -> verify(penaltyHistoryRepository, times(1)).getOrCreate("U2"),
                () -> verify(monitoringService, times(1)).updatePenaltyHistoryAndCheckBlacklist(
                        eq("U2"), any(Penalty.class), eq(mockHistory)
                ),
                () -> verify(penaltyHistoryRepository, times(1)).save("U2", mockHistory)
        );

        verifyNoMoreInteractions(penaltyHistoryRepository, monitoringService);
    }

    // TC-05
    @Test
    @DisplayName("TC-05: Should create new penalty history for new user")
    void testApplyPenaltyNewUserCreatesNewHistory() {
        PenaltyHistory newHistory = mock(PenaltyHistory.class);

        when(penaltyHistoryRepository.getOrCreate("U3")).thenReturn(newHistory);
        when(newHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(50.00));
        when(newHistory.getPenaltyCount()).thenReturn(1);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U3"), any(Penalty.class), eq(newHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U3", PenaltyType.OVERSTAY, BigDecimal.valueOf(50.00), testTimestamp
        );

        controller.applyPenalty(dto);

        assertAll("Verify new history creation",
                () -> verify(penaltyHistoryRepository, times(1)).getOrCreate("U3"),
                () -> verify(monitoringService, times(1)).updatePenaltyHistoryAndCheckBlacklist(
                        eq("U3"), any(Penalty.class), eq(newHistory)
                ),
                () -> verify(penaltyHistoryRepository, times(1)).save("U3", newHistory)
        );

        verifyNoMoreInteractions(penaltyHistoryRepository, monitoringService);
    }

    // TC-06
    @Test
    @DisplayName("TC-06: Should return CANDIDATE_FOR_BLACKLISTING blacklist status correctly")
    void testApplyPenaltyCANDIDATE_FOR_BLACKLISTINGStatusReturnedCorrectly() {
        when(penaltyHistoryRepository.getOrCreate("U4")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(250.00));
        when(mockHistory.getPenaltyCount()).thenReturn(4);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U4"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.CANDIDATE_FOR_BLACKLISTING);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U4", PenaltyType.OVERSTAY, BigDecimal.valueOf(50.00), testTimestamp
        );

        ApplyPenaltyResponseDto response = controller.applyPenalty(dto);

        assertAll("Verify CANDIDATE_FOR_BLACKLISTING status",
                () -> assertEquals(BlacklistStatus.CANDIDATE_FOR_BLACKLISTING, response.blacklistStatus()),
                () -> verify(monitoringService, times(1)).updatePenaltyHistoryAndCheckBlacklist(
                        eq("U4"), any(Penalty.class), eq(mockHistory)
                )
        );
    }

    // TC-07
    @Test
    @DisplayName("TC-07: Should map response DTO correctly from history and service")
    void testApplyPenaltyResponseDtoMappingMappedCorrectly() {
        when(penaltyHistoryRepository.getOrCreate("U6")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(150.50));
        when(mockHistory.getPenaltyCount()).thenReturn(5);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U6"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U6", PenaltyType.OVERSTAY, BigDecimal.valueOf(30.50), testTimestamp
        );

        ApplyPenaltyResponseDto response = controller.applyPenalty(dto);

        assertAll("Verify response DTO mapping",
                () -> assertEquals("U6", response.userId()),
                () -> assertEquals(BigDecimal.valueOf(150.50), response.newTotalPenaltyAmount()),
                () -> assertEquals(5, response.penaltyCount()),
                () -> assertEquals(BlacklistStatus.NONE, response.blacklistStatus())
        );
    }

    // TC-08
    @Test
    @DisplayName("TC-08: Should call monitoring service with correct parameters in order")
    void testApplyPenaltyMonitoringServiceCalledWithCorrectParameters() {
        when(penaltyHistoryRepository.getOrCreate("U7")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(100.00));
        when(mockHistory.getPenaltyCount()).thenReturn(2);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U7"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U7", PenaltyType.OVERSTAY, BigDecimal.valueOf(50.00), testTimestamp
        );

        controller.applyPenalty(dto);

        ArgumentCaptor<Penalty> penaltyCaptor = ArgumentCaptor.forClass(Penalty.class);
        verify(monitoringService).updatePenaltyHistoryAndCheckBlacklist(
                eq("U7"), penaltyCaptor.capture(), eq(mockHistory)
        );

        Penalty capturedPenalty = penaltyCaptor.getValue();
        assertAll("Verify monitoring service parameters",
                () -> assertEquals(PenaltyType.OVERSTAY, capturedPenalty.getType()),
                () -> assertEquals(BigDecimal.valueOf(50.00), capturedPenalty.getAmount()),
                () -> assertEquals(testTimestamp, capturedPenalty.getTimestamp())
        );
    }

    // TC-9
    @Test
    @DisplayName("TC-9: Should save the same history object retrieved from repository")
    void testApplyPenaltyRepositorySaveCalledWithSameHistory() {
        when(penaltyHistoryRepository.getOrCreate("U8")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(75.00));
        when(mockHistory.getPenaltyCount()).thenReturn(2);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U8"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U8", PenaltyType.OVERSTAY, BigDecimal.valueOf(25.00), testTimestamp
        );

        controller.applyPenalty(dto);

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PenaltyHistory> historyCaptor = ArgumentCaptor.forClass(PenaltyHistory.class);

        verify(penaltyHistoryRepository).save(userIdCaptor.capture(), historyCaptor.capture());

        assertAll("Verify save parameters",
                () -> assertEquals("U8", userIdCaptor.getValue()),
                () -> assertSame(mockHistory, historyCaptor.getValue())
        );
    }

    // TC-10
    @Test
    @DisplayName("TC-10: Should process different penalty types correctly")
    void testApplyPenaltyDifferentPenaltyTypesProcessedCorrectly() {
        when(penaltyHistoryRepository.getOrCreate("U9")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(75.00));
        when(mockHistory.getPenaltyCount()).thenReturn(1);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U9"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U9", PenaltyType.MISUSE, BigDecimal.valueOf(75.00), testTimestamp
        );

        controller.applyPenalty(dto);

        ArgumentCaptor<Penalty> penaltyCaptor = ArgumentCaptor.forClass(Penalty.class);
        verify(monitoringService).updatePenaltyHistoryAndCheckBlacklist(
                eq("U9"), penaltyCaptor.capture(), eq(mockHistory)
        );

        assertEquals(PenaltyType.MISUSE, penaltyCaptor.getValue().getType());
    }

    // TC-11
    @Test
    @DisplayName("TC-11: Should handle zero penalty amount")
    void testApplyPenaltyZeroPenaltyAmountProcessedCorrectly() {
        when(penaltyHistoryRepository.getOrCreate("U10")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.ZERO);
        when(mockHistory.getPenaltyCount()).thenReturn(1);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U10"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U10", PenaltyType.OVERSTAY, BigDecimal.ZERO, testTimestamp
        );

        ApplyPenaltyResponseDto response = controller.applyPenalty(dto);

        assertAll("Verify zero amount handling",
                () -> assertEquals(BigDecimal.ZERO, response.newTotalPenaltyAmount()),
                () -> verify(monitoringService, times(1)).updatePenaltyHistoryAndCheckBlacklist(
                        eq("U10"), any(Penalty.class), eq(mockHistory)
                )
        );
    }

    // TC-12
    @Test
    @DisplayName("TC-12: Should handle large penalty amount")
    void testApplyPenaltyLargePenaltyAmountProcessedCorrectly() {
        when(penaltyHistoryRepository.getOrCreate("U11")).thenReturn(mockHistory);
        when(mockHistory.getTotalPenaltyAmount()).thenReturn(BigDecimal.valueOf(9999999.99));
        when(mockHistory.getPenaltyCount()).thenReturn(1);
        when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                eq("U11"), any(Penalty.class), eq(mockHistory)
        )).thenReturn(BlacklistStatus.NONE);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                "U11", PenaltyType.LOST_TICKET, BigDecimal.valueOf(9999999.99), testTimestamp
        );

        ApplyPenaltyResponseDto response = controller.applyPenalty(dto);

        assertAll("Verify large amount handling",
                () -> assertEquals(BigDecimal.valueOf(9999999.99), response.newTotalPenaltyAmount()),
                () -> verify(monitoringService, times(1)).updatePenaltyHistoryAndCheckBlacklist(
                        eq("U11"), any(Penalty.class), eq(mockHistory)
                )
        );
    }

    // TC-13
    @Test
    @DisplayName("TC-13: Should handle null userId in DTO")
    void testApplyPenaltyNullUserIdProcessedByRepository() {
        when(penaltyHistoryRepository.getOrCreate(null)).thenThrow(NullPointerException.class);

        ApplyPenaltyRequestDto dto = new ApplyPenaltyRequestDto(
                null, PenaltyType.OVERSTAY, BigDecimal.valueOf(50.00), testTimestamp
        );


        assertAll("Verify null userId handling",
                () -> assertThrows(NullPointerException.class, () -> controller.applyPenalty(dto)),
                () -> verify(penaltyHistoryRepository, times(1)).getOrCreate(null),
                () -> verify(monitoringService, times(0)).updatePenaltyHistoryAndCheckBlacklist(
                        eq(null), any(Penalty.class), eq(mockHistory)
                ),
                () -> verify(penaltyHistoryRepository, times(0)).save(null, mockHistory)
        );
    }
}