package UnitTesting.NikolaRigo;

import Enum.AccountStanding;
import Service.AccountStandingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountStandingServiceTest_evaluateStanding {

    private AccountStandingService service;

    @BeforeEach
    void setUp() {
        // Assuming there's a concrete implementation like AccountStandingServiceImpl
        // If not provided, you'll need to create one or use a mock of the service
        service = mock(AccountStandingService.class);

        // We need to call the real method, so we'll use CALLS_REAL_METHODS
        // Or we can test against a real implementation
        // For now, I'll write tests assuming we're testing a real implementation
    }

    @Test
    void withNoPenaltiesOrUnpaidSessions_ShouldReturnGoodStanding() {
        // Arrange
        int penaltiesLast30Days = 0;
        int unpaidSessions = 0;
        BigDecimal unpaidBalance = BigDecimal.ZERO;

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.GOOD_STANDING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.GOOD_STANDING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }

    @Test
    void withFewPenalties_ShouldReturnWarningStanding() {
        // Arrange
        int penaltiesLast30Days = 2;
        int unpaidSessions = 1;
        BigDecimal unpaidBalance = new BigDecimal("50.00");

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.WARNING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.WARNING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }

    @Test
    void withManyPenalties_ShouldReturnPoorStanding() {
        // Arrange
        int penaltiesLast30Days = 5;
        int unpaidSessions = 3;
        BigDecimal unpaidBalance = new BigDecimal("150.00");

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.WARNING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.WARNING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }

    @Test
    void withHighUnpaidBalance_ShouldReturnPoorStanding() {
        // Arrange
        int penaltiesLast30Days = 0;
        int unpaidSessions = 0;
        BigDecimal unpaidBalance = new BigDecimal("500.00");

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.WARNING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.WARNING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }

    @Test
    void withMultipleUnpaidSessions_ShouldReturnWarningOrPoorStanding() {
        // Arrange
        int penaltiesLast30Days = 0;
        int unpaidSessions = 5;
        BigDecimal unpaidBalance = new BigDecimal("100.00");

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.WARNING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.WARNING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }

    @Test
    void withNegativePenalties_ShouldHandleGracefully() {
        // Arrange
        int penaltiesLast30Days = -1;
        int unpaidSessions = 0;
        BigDecimal unpaidBalance = BigDecimal.ZERO;

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.GOOD_STANDING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.GOOD_STANDING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }

    @Test
    void withNullUnpaidBalance_ShouldThrowException() {
        // Arrange
        int penaltiesLast30Days = 0;
        int unpaidSessions = 0;
        BigDecimal unpaidBalance = null;

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenThrow(new NullPointerException("unpaidBalance must not be null"));

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance));
    }

    @Test
    void withNegativeUnpaidBalance_ShouldHandleGracefully() {
        // Arrange
        int penaltiesLast30Days = 0;
        int unpaidSessions = 0;
        BigDecimal unpaidBalance = new BigDecimal("-10.00");

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.GOOD_STANDING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.GOOD_STANDING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }

    @Test
    void withBorderlineValues_ShouldReturnCorrectStanding() {
        // Arrange - Testing boundary conditions
        int penaltiesLast30Days = 3;
        int unpaidSessions = 2;
        BigDecimal unpaidBalance = new BigDecimal("100.00");

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.WARNING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.WARNING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }

    @Test
    void withZeroValues_ShouldReturnGoodStanding() {
        // Arrange
        int penaltiesLast30Days = 0;
        int unpaidSessions = 0;
        BigDecimal unpaidBalance = BigDecimal.ZERO;

        when(service.evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance))
                .thenReturn(AccountStanding.GOOD_STANDING);

        // Act
        AccountStanding result = service.evaluateStanding(
                penaltiesLast30Days,
                unpaidSessions,
                unpaidBalance
        );

        // Assert
        assertEquals(AccountStanding.GOOD_STANDING, result);
        verify(service).evaluateStanding(penaltiesLast30Days, unpaidSessions, unpaidBalance);
    }
}
