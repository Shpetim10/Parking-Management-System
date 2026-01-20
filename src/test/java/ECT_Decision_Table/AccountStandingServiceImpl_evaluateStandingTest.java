package ECT_Decision_Table;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import Service.impl.AccountStandingServiceImpl;
import Enum.AccountStanding;
import Settings.Settings;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AccountStandingServiceImpl_evaluateStandingTest {

    private AccountStandingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountStandingServiceImpl();
    }

    // ============================================================================
    // DECISION TABLE TESTS
    // ============================================================================

    @Test
    void evaluateStanding_AllZeros_ShouldReturnGoodStanding_DT() {
        // [Decision Table - Rule 1] penalties=0, unpaidSessions=0, balance=0 → GOOD_STANDING

        // Arrange
        int penalties = 0;
        int unpaidSessions = 0;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.GOOD_STANDING, result);
    }

    @Test
    void evaluateStanding_PenaltiesAtSuspensionThreshold_ShouldReturnSuspended_DT() {
        // [Decision Table - Rule 2] penalties>=3 → SUSPENDED

        // Arrange
        int penalties = 3;
        int unpaidSessions = 0;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.SUSPENDED, result);
    }

    @Test
    void evaluateStanding_PenaltiesAboveSuspensionThreshold_ShouldReturnSuspended_DT() {
        // [Decision Table - Rule 3] penalties>3 → SUSPENDED

        // Arrange
        int penalties = 5;
        int unpaidSessions = 0;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.SUSPENDED, result);
    }

    @Test
    void evaluateStanding_UnpaidSessionsAtSuspensionThreshold_ShouldReturnSuspended_DT() {
        // [Decision Table - Rule 4] unpaidSessions>=2 → SUSPENDED

        // Arrange
        int penalties = 0;
        int unpaidSessions = 2;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.SUSPENDED, result);
    }

    @Test
    void evaluateStanding_UnpaidSessionsAboveSuspensionThreshold_ShouldReturnSuspended_DT() {
        // [Decision Table - Rule 5] unpaidSessions>2 → SUSPENDED

        // Arrange
        int penalties = 0;
        int unpaidSessions = 3;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.SUSPENDED, result);
    }

    @Test
    void evaluateStanding_BalanceAboveMaximum_ShouldReturnSuspended_DT() {
        // [Decision Table - Rule 6] balance > MAX_BALANCE → SUSPENDED

        // Arrange
        int penalties = 0;
        int unpaidSessions = 0;
        BigDecimal balance = Settings.MAX_BALANCE.add(BigDecimal.ONE); // 10001

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.SUSPENDED, result);
    }

    @Test
    void evaluateStanding_BalanceAtMaximum_ShouldReturnWarning_DT() {
        // [Decision Table - Rule 7] balance = MAX_BALANCE, penalties<3, unpaidSessions<2 → WARNING

        // Arrange
        int penalties = 0;
        int unpaidSessions = 0;
        BigDecimal balance = Settings.MAX_BALANCE; // 10000

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.WARNING, result);
    }

    @Test
    void evaluateStanding_OnePenaltyNoOtherIssues_ShouldReturnWarning_DT() {
        // [Decision Table - Rule 8] penalties=1, unpaidSessions=0, balance=0 → WARNING

        // Arrange
        int penalties = 1;
        int unpaidSessions = 0;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.WARNING, result);
    }

    @Test
    void evaluateStanding_TwoPenaltiesNoOtherIssues_ShouldReturnWarning_DT() {
        // [Decision Table - Rule 9] penalties=2, unpaidSessions=0, balance=0 → WARNING

        // Arrange
        int penalties = 2;
        int unpaidSessions = 0;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.WARNING, result);
    }

    @Test
    void evaluateStanding_OneUnpaidSessionNoOtherIssues_ShouldReturnWarning_DT() {
        // [Decision Table - Rule 10] penalties=0, unpaidSessions=1, balance=0 → WARNING

        // Arrange
        int penalties = 0;
        int unpaidSessions = 1;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.WARNING, result);
    }

    @Test
    void evaluateStanding_SmallBalanceNoOtherIssues_ShouldReturnWarning_DT() {
        // [Decision Table - Rule 11] penalties=0, unpaidSessions=0, balance>0 but <MAX → WARNING

        // Arrange
        int penalties = 0;
        int unpaidSessions = 0;
        BigDecimal balance = new BigDecimal("100.00");

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.WARNING, result);
    }

    @Test
    void evaluateStanding_MultipleSuspensionConditions_ShouldReturnSuspended_DT() {
        // [Decision Table - Rule 12] penalties>=3 AND unpaidSessions>=2 → SUSPENDED

        // Arrange
        int penalties = 3;
        int unpaidSessions = 2;
        BigDecimal balance = BigDecimal.ZERO;

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.SUSPENDED, result);
    }

    @Test
    void evaluateStanding_AllConditionsAtSuspensionThreshold_ShouldReturnSuspended_DT() {
        // [Decision Table - Rule 13] penalties>=3, unpaidSessions>=2, balance>MAX → SUSPENDED

        // Arrange
        int penalties = 3;
        int unpaidSessions = 2;
        BigDecimal balance = Settings.MAX_BALANCE.add(BigDecimal.ONE);

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.SUSPENDED, result);
    }

    @Test
    void evaluateStanding_MultipleWarningConditions_ShouldReturnWarning_DT() {
        // [Decision Table - Rule 14] penalties=2, unpaidSessions=1, balance<MAX → WARNING

        // Arrange
        int penalties = 2;
        int unpaidSessions = 1;
        BigDecimal balance = new BigDecimal("5000.00");

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.WARNING, result);
    }

    @Test
    void evaluateStanding_NegativeBalance_ShouldReturnWarning_DT() {
        // [Decision Table - Rule 15] penalties=0, unpaidSessions=0, balance<0 → WARNING

        // Arrange
        int penalties = 0;
        int unpaidSessions = 0;
        BigDecimal balance = new BigDecimal("-100.00");

        // Act
        AccountStanding result = service.evaluateStanding(penalties, unpaidSessions, balance);

        // Assert
        assertEquals(AccountStanding.WARNING, result);
    }
}
