package IntegrationTesting.NikolaRigo;

import Enum.*;
import Model.*;
import Repository.PenaltyHistoryRepository;
import Repository.impl.InMemoryPenaltyHistoryRepository;
import Service.AccountStandingService;
import Service.impl.AccountStandingServiceImpl;
import Service.impl.MonitoringServiceImpl;
import Settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringServiceIT {

    private MonitoringServiceImpl monitoringService;
    private PenaltyHistoryRepository penaltyHistoryRepository;
    private AccountStandingService accountStandingService;

    @BeforeEach
    void setUp() {
        // 1. Initialize Repositories
        penaltyHistoryRepository = new InMemoryPenaltyHistoryRepository();

        // 2. Initialize Services
        monitoringService = new MonitoringServiceImpl();
        accountStandingService = new AccountStandingServiceImpl();
    }

    // =========================================================================
    // 1. Penalty & Blacklist Logic Tests
    // =========================================================================

    @Test
    void updatePenaltyHistory_whenWithinLimits_shouldReturnNone() {
        // Arrange
        String userId = "user-clean";
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate(userId);

        Penalty newPenalty = new Penalty(
                PenaltyType.OVERSTAY,
                new BigDecimal("10.00"),
                LocalDateTime.now()
        );

        // Act
        BlacklistStatus status = monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                userId, newPenalty, history
        );

        // Assert
        // Total penalties = 1. Limit is 3. 1 > 3 is False.
        assertEquals(BlacklistStatus.NONE, status);
        assertEquals(1, history.getPenaltyCount());

        // Persist change
        penaltyHistoryRepository.save(userId, history);
        assertNotNull(penaltyHistoryRepository.findById(userId));
    }

    @Test
    void updatePenaltyHistory_whenExceedingLimit_shouldFlagCandidate() {
        // Arrange
        String userId = "user-repeat-offender";
        PenaltyHistory history = penaltyHistoryRepository.getOrCreate(userId);

        // Settings.MAX_PENALTIES_ALLOWED is 3.
        // We add 3 existing penalties so the history is already "full" relative to the limit.
        history.addPenalty(new Penalty(PenaltyType.MISUSE, BigDecimal.TEN, LocalDateTime.now()));
        history.addPenalty(new Penalty(PenaltyType.MISUSE, BigDecimal.TEN, LocalDateTime.now()));
        history.addPenalty(new Penalty(PenaltyType.MISUSE, BigDecimal.TEN, LocalDateTime.now()));

        // The 4th penalty (the new one) will push the count to 4.
        // Logic: 4 > 3 == true -> Candidate.
        Penalty newPenalty = new Penalty(
                PenaltyType.LOST_TICKET,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        // Act
        BlacklistStatus status = monitoringService.updatePenaltyHistoryAndCheckBlacklist(
                userId, newPenalty, history
        );

        // Assert
        assertEquals(BlacklistStatus.CANDIDATE_FOR_BLACKLISTING, status);
        assertEquals(4, history.getPenaltyCount());
    }

    // =========================================================================
    // 2. Reporting & Logging Tests
    // =========================================================================

    @Test
    void generatePenaltySummary_shouldAggregateTotalsCorrectly() {
        // Arrange
        // User A: 1 Overstay ($20)
        PenaltyHistory histA = new PenaltyHistory();
        histA.addPenalty(new Penalty(PenaltyType.OVERSTAY, new BigDecimal("20.00"), LocalDateTime.now()));

        // User B: 1 Lost Ticket ($50) + 1 Misuse ($100)
        PenaltyHistory histB = new PenaltyHistory();
        histB.addPenalty(new Penalty(PenaltyType.LOST_TICKET, new BigDecimal("50.00"), LocalDateTime.now()));
        histB.addPenalty(new Penalty(PenaltyType.MISUSE, new BigDecimal("100.00"), LocalDateTime.now()));

        List<PenaltyHistory> allHistories = List.of(histA, histB);

        // Act
        PenaltySummaryReport report = monitoringService.generatePenaltySummary(allHistories);

        // Assert
        assertEquals(new BigDecimal("20.00"), report.getTotalOverstay());
        assertEquals(new BigDecimal("50.00"), report.getTotalLostTicket());
        assertEquals(new BigDecimal("100.00"), report.getTotalMisuse());
    }

    @Test
    void logEvent_shouldStoreLogCorrectly() {
        // Arrange
        LogEvent event = new LogEvent(LocalDateTime.now(), "INFO", "System Started");

        // Act
        monitoringService.logEvent(event);

        // Assert
        // Since 'logs' is public final List in your impl, we can check it directly
        assertFalse(monitoringService.logs.isEmpty());
        assertEquals("INFO", monitoringService.logs.get(0).getType());
    }

    @Test
    void generateZoneReport_shouldValidateOccupancy() {
        // Act
        ZoneOccupancyReport report = monitoringService.generateZoneReport(
                ZoneType.VIP,
                0.85, // Valid occupancy (0.0 - 1.0)
                100,
                5
        );

        // Assert
        assertNotNull(report);
        assertEquals(0.85, report.getAverageOccupancy());
    }

    // =========================================================================
    // 3. Account Standing Service Tests
    // =========================================================================

    @Test
    void evaluateStanding_shouldIdentifySuspendedAccounts() {
        // Arrange
        int highPenalties = 5;
        int zeroUnpaid = 0;
        BigDecimal zeroBalance = BigDecimal.ZERO;

        // Act
        AccountStanding standing = accountStandingService.evaluateStanding(highPenalties, zeroUnpaid, zeroBalance);

        // Assert
        // highPenalties (5) >= 3 -> SUSPENDED
        assertEquals(AccountStanding.SUSPENDED, standing);
    }

    @Test
    void deriveUserStatus_shouldMapStandingToStatus() {
        // Act & Assert
        assertEquals(
                UserStatus.INACTIVE,
                accountStandingService.deriveUserStatus(AccountStanding.SUSPENDED, false)
        );

        assertEquals(
                UserStatus.BLACKLISTED,
                accountStandingService.deriveUserStatus(AccountStanding.GOOD_STANDING, true)
        );
    }
}