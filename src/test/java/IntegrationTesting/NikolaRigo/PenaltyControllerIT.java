package IntegrationTesting.NikolaRigo;

import Controller.PenaltyController;
import Dto.Penalty.ApplyPenaltyRequestDto;
import Dto.Penalty.ApplyPenaltyResponseDto;
import Dto.Penalty.PenaltyCalculationRequestDto;
import Dto.Penalty.PenaltyCalculationResponseDto;
import Enum.BlacklistStatus;
import Enum.PenaltyType;
import Model.Penalty;
import Model.PenaltyHistory;
import Repository.PenaltyHistoryRepository;
import Repository.impl.InMemoryPenaltyHistoryRepository;
import Service.MonitoringService;
import Service.PenaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration Tests for PenaltyController
 *
 * Neighbourhood Integration Testing (Radius 1):
 * - STUB: PenaltyService (direct dependency for calculatePenalty)
 * - STUB: MonitoringService (direct dependency for applyPenalty)
 * - REAL: InMemoryPenaltyHistoryRepository (direct dependency)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Integration Tests - PenaltyController")
class PenaltyControllerIT{

    private PenaltyController penaltyController;

    // STUB: Services (direct dependencies)
    @Mock
    private PenaltyService penaltyService;

    @Mock
    private MonitoringService monitoringService;

    // REAL: Repository (direct dependency)
    private PenaltyHistoryRepository penaltyHistoryRepository;

    @BeforeEach
    void setUp() {
        // Initialize real in-memory repository
        penaltyHistoryRepository = new InMemoryPenaltyHistoryRepository();

        // Create controller with stubbed services and real repository
        penaltyController = new PenaltyController(
                penaltyService,
                monitoringService,
                penaltyHistoryRepository
        );
    }

    // =========================================================================
    // CONSTRUCTOR TESTS
    // =========================================================================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should throw NullPointerException when PenaltyService is null")
        void constructor_WhenPenaltyServiceNull_ShouldThrow() {
            assertThrows(NullPointerException.class, () ->
                    new PenaltyController(null, monitoringService, penaltyHistoryRepository)
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when MonitoringService is null")
        void constructor_WhenMonitoringServiceNull_ShouldThrow() {
            assertThrows(NullPointerException.class, () ->
                    new PenaltyController(penaltyService, null, penaltyHistoryRepository)
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when PenaltyHistoryRepository is null")
        void constructor_WhenRepositoryNull_ShouldThrow() {
            assertThrows(NullPointerException.class, () ->
                    new PenaltyController(penaltyService, monitoringService, null)
            );
        }
    }

    // =========================================================================
    // calculatePenalty() INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("calculatePenalty() Integration Tests")
    class CalculatePenaltyTests {

        @Test
        @DisplayName("Should return penalty amount for overstay violation")
        void calculatePenalty_WithOverstay_ShouldReturnPenaltyAmount() {
            // Arrange
            PenaltyCalculationRequestDto request = new PenaltyCalculationRequestDto(
                    true,                          // overstayed
                    2.5,                           // extraHours
                    false,                         // lostTicket
                    false,                         // zoneMisuse
                    new BigDecimal("10.00"),       // baseOverstayRatePerHour
                    new BigDecimal("50.00"),       // overstayCap
                    new BigDecimal("25.00"),       // lostTicketFee
                    new BigDecimal("30.00")        // misuseFee
            );

            BigDecimal expectedPenalty = new BigDecimal("25.00");
            when(penaltyService.calculatePenalty(
                    eq(true), eq(2.5), eq(false), eq(false),
                    any(BigDecimal.class), any(BigDecimal.class),
                    any(BigDecimal.class), any(BigDecimal.class)
            )).thenReturn(expectedPenalty);

            // Act
            PenaltyCalculationResponseDto response = penaltyController.calculatePenalty(request);

            // Assert
            assertNotNull(response);
            assertEquals(expectedPenalty, response.totalPenalty());

            verify(penaltyService).calculatePenalty(
                    true, 2.5, false, false,
                    new BigDecimal("10.00"), new BigDecimal("50.00"),
                    new BigDecimal("25.00"), new BigDecimal("30.00")
            );
        }

        @Test
        @DisplayName("Should return penalty amount for lost ticket")
        void calculatePenalty_WithLostTicket_ShouldReturnPenaltyAmount() {
            // Arrange
            PenaltyCalculationRequestDto request = new PenaltyCalculationRequestDto(
                    false,                         // overstayed
                    0,                             // extraHours
                    true,                          // lostTicket
                    false,                         // zoneMisuse
                    new BigDecimal("10.00"),       // baseOverstayRatePerHour
                    new BigDecimal("50.00"),       // overstayCap
                    new BigDecimal("25.00"),       // lostTicketFee
                    new BigDecimal("30.00")        // misuseFee
            );

            BigDecimal expectedPenalty = new BigDecimal("25.00");
            when(penaltyService.calculatePenalty(
                    anyBoolean(), anyDouble(), anyBoolean(), anyBoolean(),
                    any(BigDecimal.class), any(BigDecimal.class),
                    any(BigDecimal.class), any(BigDecimal.class)
            )).thenReturn(expectedPenalty);

            // Act
            PenaltyCalculationResponseDto response = penaltyController.calculatePenalty(request);

            // Assert
            assertNotNull(response);
            assertEquals(expectedPenalty, response.totalPenalty());
        }

        @Test
        @DisplayName("Should return penalty amount for zone misuse")
        void calculatePenalty_WithZoneMisuse_ShouldReturnPenaltyAmount() {
            // Arrange
            PenaltyCalculationRequestDto request = new PenaltyCalculationRequestDto(
                    false,                         // overstayed
                    0,                             // extraHours
                    false,                         // lostTicket
                    true,                          // zoneMisuse
                    new BigDecimal("10.00"),       // baseOverstayRatePerHour
                    new BigDecimal("50.00"),       // overstayCap
                    new BigDecimal("25.00"),       // lostTicketFee
                    new BigDecimal("30.00")        // misuseFee
            );

            BigDecimal expectedPenalty = new BigDecimal("30.00");
            when(penaltyService.calculatePenalty(
                    anyBoolean(), anyDouble(), anyBoolean(), anyBoolean(),
                    any(BigDecimal.class), any(BigDecimal.class),
                    any(BigDecimal.class), any(BigDecimal.class)
            )).thenReturn(expectedPenalty);

            // Act
            PenaltyCalculationResponseDto response = penaltyController.calculatePenalty(request);

            // Assert
            assertNotNull(response);
            assertEquals(expectedPenalty, response.totalPenalty());
        }

        @Test
        @DisplayName("Should return combined penalty for multiple violations")
        void calculatePenalty_WithMultipleViolations_ShouldReturnCombinedPenalty() {
            // Arrange
            PenaltyCalculationRequestDto request = new PenaltyCalculationRequestDto(
                    true,                          // overstayed
                    3.0,                           // extraHours
                    true,                          // lostTicket
                    true,                          // zoneMisuse
                    new BigDecimal("10.00"),       // baseOverstayRatePerHour
                    new BigDecimal("50.00"),       // overstayCap
                    new BigDecimal("25.00"),       // lostTicketFee
                    new BigDecimal("30.00")        // misuseFee
            );

            BigDecimal expectedPenalty = new BigDecimal("85.00"); // 30 + 25 + 30
            when(penaltyService.calculatePenalty(
                    anyBoolean(), anyDouble(), anyBoolean(), anyBoolean(),
                    any(BigDecimal.class), any(BigDecimal.class),
                    any(BigDecimal.class), any(BigDecimal.class)
            )).thenReturn(expectedPenalty);

            // Act
            PenaltyCalculationResponseDto response = penaltyController.calculatePenalty(request);

            // Assert
            assertNotNull(response);
            assertEquals(expectedPenalty, response.totalPenalty());
        }

        @Test
        @DisplayName("Should return zero penalty when no violations")
        void calculatePenalty_WithNoViolations_ShouldReturnZero() {
            // Arrange
            PenaltyCalculationRequestDto request = new PenaltyCalculationRequestDto(
                    false, 0, false, false,
                    new BigDecimal("10.00"), new BigDecimal("50.00"),
                    new BigDecimal("25.00"), new BigDecimal("30.00")
            );

            when(penaltyService.calculatePenalty(
                    anyBoolean(), anyDouble(), anyBoolean(), anyBoolean(),
                    any(BigDecimal.class), any(BigDecimal.class),
                    any(BigDecimal.class), any(BigDecimal.class)
            )).thenReturn(BigDecimal.ZERO);

            // Act
            PenaltyCalculationResponseDto response = penaltyController.calculatePenalty(request);

            // Assert
            assertNotNull(response);
            assertEquals(BigDecimal.ZERO, response.totalPenalty());
        }

        @Test
        @DisplayName("Should throw NullPointerException when request is null")
        void calculatePenalty_WithNullRequest_ShouldThrow() {
            assertThrows(NullPointerException.class, () ->
                    penaltyController.calculatePenalty(null)
            );
        }
    }

    // =========================================================================
    // applyPenalty() INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("applyPenalty() Integration Tests")
    class ApplyPenaltyTests {

        @Test
        @DisplayName("Should apply first penalty and return NONE blacklist status")
        void applyPenalty_FirstPenalty_ShouldReturnNoneStatus() {
            // Arrange
            String userId = "user-1";
            LocalDateTime timestamp = LocalDateTime.now();

            ApplyPenaltyRequestDto request = new ApplyPenaltyRequestDto(
                    userId,
                    PenaltyType.OVERSTAY,
                    new BigDecimal("25.00"),
                    timestamp
            );

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    eq(userId), any(Penalty.class), any(PenaltyHistory.class)
            )).thenReturn(BlacklistStatus.NONE);

            // Act
            ApplyPenaltyResponseDto response = penaltyController.applyPenalty(request);

            // Assert
            assertNotNull(response);
            assertEquals(userId, response.userId());
            assertEquals(BlacklistStatus.NONE, response.blacklistStatus());

            // Verify repository interaction
            PenaltyHistory savedHistory = penaltyHistoryRepository.findById(userId);
            assertNotNull(savedHistory);
        }

        @Test
        @DisplayName("Should accumulate penalties for same user")
        void applyPenalty_MultiplePenalties_ShouldAccumulate() {
            // Arrange
            String userId = "user-2";
            LocalDateTime timestamp = LocalDateTime.now();

            // First penalty
            ApplyPenaltyRequestDto request1 = new ApplyPenaltyRequestDto(
                    userId,
                    PenaltyType.OVERSTAY,
                    new BigDecimal("25.00"),
                    timestamp
            );

            // Second penalty
            ApplyPenaltyRequestDto request2 = new ApplyPenaltyRequestDto(
                    userId,
                    PenaltyType.LOST_TICKET,
                    new BigDecimal("30.00"),
                    timestamp.plusHours(1)
            );

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    eq(userId), any(Penalty.class), any(PenaltyHistory.class)
            )).thenAnswer(invocation -> {
                Penalty penalty = invocation.getArgument(1);
                PenaltyHistory history = invocation.getArgument(2);
                history.addPenalty(penalty);
                return BlacklistStatus.NONE;
            });

            // Act
            penaltyController.applyPenalty(request1);
            ApplyPenaltyResponseDto response2 = penaltyController.applyPenalty(request2);

            // Assert
            assertNotNull(response2);
            assertEquals(userId, response2.userId());
            assertEquals(2, response2.penaltyCount());
            assertEquals(new BigDecimal("55.00"), response2.newTotalPenaltyAmount());
        }

        @Test
        @DisplayName("Should return CANDIDATE_FOR_BLACKLISTING when threshold approached")
        void applyPenalty_ApproachingThreshold_ShouldReturnCandidateStatus() {
            // Arrange
            String userId = "user-3";
            LocalDateTime timestamp = LocalDateTime.now();

            ApplyPenaltyRequestDto request = new ApplyPenaltyRequestDto(
                    userId,
                    PenaltyType.MISUSE,
                    new BigDecimal("100.00"),
                    timestamp
            );

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    eq(userId), any(Penalty.class), any(PenaltyHistory.class)
            )).thenReturn(BlacklistStatus.CANDIDATE_FOR_BLACKLISTING);

            // Act
            ApplyPenaltyResponseDto response = penaltyController.applyPenalty(request);

            // Assert
            assertNotNull(response);
            assertEquals(BlacklistStatus.CANDIDATE_FOR_BLACKLISTING, response.blacklistStatus());
        }

        @Test
        @DisplayName("Should return BLACKLISTED when threshold exceeded")
        void applyPenalty_ThresholdExceeded_ShouldReturnBlacklistedStatus() {
            // Arrange
            String userId = "user-4";
            LocalDateTime timestamp = LocalDateTime.now();

            ApplyPenaltyRequestDto request = new ApplyPenaltyRequestDto(
                    userId,
                    PenaltyType.MISUSE,
                    new BigDecimal("500.00"),
                    timestamp
            );

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    eq(userId), any(Penalty.class), any(PenaltyHistory.class)
            )).thenReturn(BlacklistStatus.BLACKLISTED);

            // Act
            ApplyPenaltyResponseDto response = penaltyController.applyPenalty(request);

            // Assert
            assertNotNull(response);
            assertEquals(BlacklistStatus.BLACKLISTED, response.blacklistStatus());
        }

        @Test
        @DisplayName("Should throw NullPointerException when request is null")
        void applyPenalty_WithNullRequest_ShouldThrow() {
            assertThrows(NullPointerException.class, () ->
                    penaltyController.applyPenalty(null)
            );
        }

        @Test
        @DisplayName("Should persist penalty history to repository")
        void applyPenalty_ShouldPersistToRepository() {
            // Arrange
            String userId = "user-persist";
            LocalDateTime timestamp = LocalDateTime.now();

            ApplyPenaltyRequestDto request = new ApplyPenaltyRequestDto(
                    userId,
                    PenaltyType.OVERSTAY,
                    new BigDecimal("15.00"),
                    timestamp
            );

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    eq(userId), any(Penalty.class), any(PenaltyHistory.class)
            )).thenAnswer(invocation -> {
                Penalty penalty = invocation.getArgument(1);
                PenaltyHistory history = invocation.getArgument(2);
                history.addPenalty(penalty);
                return BlacklistStatus.NONE;
            });

            // Act
            penaltyController.applyPenalty(request);

            // Assert - Verify data is persisted in repository
            PenaltyHistory persistedHistory = penaltyHistoryRepository.findById(userId);
            assertNotNull(persistedHistory);
            assertEquals(1, persistedHistory.getPenaltyCount());
            assertEquals(new BigDecimal("15.00"), persistedHistory.getTotalPenaltyAmount());
        }

        @Test
        @DisplayName("Should handle different penalty types correctly")
        void applyPenalty_DifferentTypes_ShouldHandleCorrectly() {
            // Arrange
            String userId = "user-types";
            LocalDateTime timestamp = LocalDateTime.now();

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    eq(userId), any(Penalty.class), any(PenaltyHistory.class)
            )).thenAnswer(invocation -> {
                Penalty penalty = invocation.getArgument(1);
                PenaltyHistory history = invocation.getArgument(2);
                history.addPenalty(penalty);
                return BlacklistStatus.NONE;
            });

            // Apply OVERSTAY
            penaltyController.applyPenalty(new ApplyPenaltyRequestDto(
                    userId, PenaltyType.OVERSTAY, new BigDecimal("10.00"), timestamp
            ));

            // Apply LOST_TICKET
            penaltyController.applyPenalty(new ApplyPenaltyRequestDto(
                    userId, PenaltyType.LOST_TICKET, new BigDecimal("25.00"), timestamp.plusMinutes(30)
            ));

            // Apply MISUSE
            ApplyPenaltyResponseDto finalResponse = penaltyController.applyPenalty(new ApplyPenaltyRequestDto(
                    userId, PenaltyType.MISUSE, new BigDecimal("30.00"), timestamp.plusHours(1)
            ));

            // Assert
            assertEquals(3, finalResponse.penaltyCount());
            assertEquals(new BigDecimal("65.00"), finalResponse.newTotalPenaltyAmount());
        }
    }

    // =========================================================================
    // REPOSITORY INTEGRATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("Repository Integration Tests")
    class RepositoryIntegrationTests {

        @Test
        @DisplayName("Should create new history for new user")
        void applyPenalty_NewUser_ShouldCreateHistory() {
            // Arrange
            String userId = "new-user";
            assertNull(penaltyHistoryRepository.findById(userId));

            ApplyPenaltyRequestDto request = new ApplyPenaltyRequestDto(
                    userId,
                    PenaltyType.OVERSTAY,
                    new BigDecimal("20.00"),
                    LocalDateTime.now()
            );

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    anyString(), any(Penalty.class), any(PenaltyHistory.class)
            )).thenReturn(BlacklistStatus.NONE);

            // Act
            penaltyController.applyPenalty(request);

            // Assert
            PenaltyHistory history = penaltyHistoryRepository.findById(userId);
            assertNotNull(history);
        }

        @Test
        @DisplayName("Should reuse existing history for existing user")
        void applyPenalty_ExistingUser_ShouldReuseHistory() {
            // Arrange
            String userId = "existing-user";

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    eq(userId), any(Penalty.class), any(PenaltyHistory.class)
            )).thenAnswer(invocation -> {
                Penalty penalty = invocation.getArgument(1);
                PenaltyHistory history = invocation.getArgument(2);
                history.addPenalty(penalty);
                return BlacklistStatus.NONE;
            });

            // Apply first penalty
            penaltyController.applyPenalty(new ApplyPenaltyRequestDto(
                    userId, PenaltyType.OVERSTAY, new BigDecimal("10.00"), LocalDateTime.now()
            ));

            // Apply second penalty
            penaltyController.applyPenalty(new ApplyPenaltyRequestDto(
                    userId, PenaltyType.LOST_TICKET, new BigDecimal("25.00"), LocalDateTime.now()
            ));

            // Assert - Should be same history object with accumulated penalties
            PenaltyHistory history = penaltyHistoryRepository.findById(userId);
            assertNotNull(history);
            assertEquals(2, history.getPenaltyCount());
        }

        @Test
        @DisplayName("Should isolate penalty history between different users")
        void applyPenalty_DifferentUsers_ShouldIsolateHistories() {
            // Arrange
            String user1 = "user-A";
            String user2 = "user-B";

            when(monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                    anyString(), any(Penalty.class), any(PenaltyHistory.class)
            )).thenAnswer(invocation -> {
                Penalty penalty = invocation.getArgument(1);
                PenaltyHistory history = invocation.getArgument(2);
                history.addPenalty(penalty);
                return BlacklistStatus.NONE;
            });

            // Apply penalty to user1
            penaltyController.applyPenalty(new ApplyPenaltyRequestDto(
                    user1, PenaltyType.OVERSTAY, new BigDecimal("50.00"), LocalDateTime.now()
            ));

            // Apply penalty to user2
            penaltyController.applyPenalty(new ApplyPenaltyRequestDto(
                    user2, PenaltyType.MISUSE, new BigDecimal("30.00"), LocalDateTime.now()
            ));

            // Assert - Each user has separate history
            PenaltyHistory history1 = penaltyHistoryRepository.findById(user1);
            PenaltyHistory history2 = penaltyHistoryRepository.findById(user2);

            assertNotNull(history1);
            assertNotNull(history2);
            assertEquals(1, history1.getPenaltyCount());
            assertEquals(1, history2.getPenaltyCount());
            assertEquals(new BigDecimal("50.00"), history1.getTotalPenaltyAmount());
            assertEquals(new BigDecimal("30.00"), history2.getTotalPenaltyAmount());
        }
    }
}
