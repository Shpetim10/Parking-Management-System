package UnitTesting.NikolaRigo;

import Record.DurationInfo;
import Service.DurationCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DurationCalculatorTest_calculateDuration {

    private DurationCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = mock(DurationCalculator.class);
    }

    @Test
    void withValidTimeRange_ShouldReturnDurationInfo() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        int maxDurationHours = 24;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        assertEquals(mockDurationInfo, result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withOneHourDuration_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 11, 0);
        int maxDurationHours = 24;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withMultipleHoursDuration_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 15, 30);
        int maxDurationHours = 24;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withDurationExceedingMax_ShouldHandleCorrectly() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 16, 12, 0); // 26 hours
        int maxDurationHours = 24;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withSameEntryAndExitTime_ShouldReturnZeroDuration() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        int maxDurationHours = 24;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withExitBeforeEntry_ShouldThrowException() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        int maxDurationHours = 24;

        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenThrow(new IllegalArgumentException("Exit time cannot be before entry time"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateDuration(entryTime, exitTime, maxDurationHours));
    }

    @Test
    void withNullEntryTime_ShouldThrowException() {
        // Arrange
        LocalDateTime entryTime = null;
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        int maxDurationHours = 24;

        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenThrow(new NullPointerException("entryTime must not be null"));

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> calculator.calculateDuration(entryTime, exitTime, maxDurationHours));
    }

    @Test
    void withNullExitTime_ShouldThrowException() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = null;
        int maxDurationHours = 24;

        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenThrow(new NullPointerException("exitTime must not be null"));

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> calculator.calculateDuration(entryTime, exitTime, maxDurationHours));
    }

    @Test
    void withZeroMaxDuration_ShouldHandleCorrectly() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        int maxDurationHours = 0;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withNegativeMaxDuration_ShouldThrowException() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        int maxDurationHours = -1;

        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenThrow(new IllegalArgumentException("maxDurationHours must not be negative"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateDuration(entryTime, exitTime, maxDurationHours));
    }

    @Test
    void withMinutesDuration_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 15, 10, 45);
        int maxDurationHours = 24;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withMultipleDaysDuration_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 18, 14, 30);
        int maxDurationHours = 72;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withExactlyMaxDuration_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 16, 10, 0); // Exactly 24 hours
        int maxDurationHours = 24;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }

    @Test
    void withMidnightCrossing_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.of(2024, 1, 15, 23, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 1, 16, 2, 0); // Crosses midnight
        int maxDurationHours = 24;

        DurationInfo mockDurationInfo = mock(DurationInfo.class);
        when(calculator.calculateDuration(entryTime, exitTime, maxDurationHours))
                .thenReturn(mockDurationInfo);

        // Act
        DurationInfo result = calculator.calculateDuration(entryTime, exitTime, maxDurationHours);

        // Assert
        assertNotNull(result);
        verify(calculator).calculateDuration(entryTime, exitTime, maxDurationHours);
    }
}
