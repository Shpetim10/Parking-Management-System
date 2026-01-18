package UnitTesting.ShpetimShabanaj;

import Enum.PenaltyType;
import Model.Penalty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PenaltyConstructorTest {

    // TC-01
    @Test
    @DisplayName("TC-01: Should initialize correctly with valid parameters")
    void testConstructorValid() {
        PenaltyType type = PenaltyType.OVERSTAY;
        BigDecimal amount = new BigDecimal("50.00");
        LocalDateTime now = LocalDateTime.now();

        Penalty penalty = new Penalty(type, amount, now);

        assertAll("Verify properties",
                () -> assertEquals(type, penalty.getType()),
                () -> assertEquals(amount, penalty.getAmount()),
                () -> assertEquals(now, penalty.getTimestamp())
        );
    }

    // TC-02
    @Test
    @DisplayName("TC-02: Should throw exception when PenaltyType is null")
    void testConstructorNullType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Penalty(null, BigDecimal.TEN, LocalDateTime.now())
        );
        assertEquals("Penalty type cannot be null", ex.getMessage());
    }

    // TC-03
    @Test
    @DisplayName("TC-03: Should throw exception when amount is null")
    void testConstructorNullAmount() {
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () ->
                new Penalty(PenaltyType.OVERSTAY, null, LocalDateTime.now())
        );
        assertEquals("Penalty amount must be non-negative", exNull.getMessage());
    }

    // TC-04
    @ParameterizedTest
    @ValueSource(strings = {"-1.0", "-0.01"}) // Testing negative values
    @DisplayName("TC-04: Should throw exception when amount is negative")
    void testConstructorNegativeAmount(String amountStr) {
        BigDecimal amount = new BigDecimal(amountStr);

        IllegalArgumentException exNeg = assertThrows(IllegalArgumentException.class, () ->
                new Penalty(PenaltyType.OVERSTAY, amount, LocalDateTime.now())
        );
        assertEquals("Penalty amount must be non-negative", exNeg.getMessage());
    }

    // TC-05
    @Test
    @DisplayName("TC-05: Should allow an amount of exactly zero")
    void testConstructorZeroAmount() {
        assertDoesNotThrow(() ->
                new Penalty(PenaltyType.OVERSTAY, BigDecimal.ZERO, LocalDateTime.now())
        );
    }

    // TC-06
    @Test
    @DisplayName("TC-06: Should throw exception when timestamp is null")
    void testConstructorNullTimestamp() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Penalty(PenaltyType.OVERSTAY, BigDecimal.TEN, null)
        );
        assertEquals("Penalty timestamp cannot be null", ex.getMessage());
    }
}