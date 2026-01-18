package UnitTesting.ShpetimShabanaj;

import Model.PenaltySummaryReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PenaltySummaryReportConstructorTest {
    // TC-01
    @Test
    @DisplayName("TC-01: Should initialize correctly when all BigDecimals are provided")
    void testConstructorWithValidValues() {
        BigDecimal overstay = new BigDecimal("10.0");
        BigDecimal lost = new BigDecimal("20.0");
        BigDecimal misuse = new BigDecimal("30.0");
        int count = 5;

        PenaltySummaryReport report = new PenaltySummaryReport(overstay, lost, misuse, count);

        assertAll("Verify constructor assignments",
                () -> assertEquals(overstay, report.getTotalOverstay()),
                () -> assertEquals(lost, report.getTotalLostTicket()),
                () -> assertEquals(misuse, report.getTotalMisuse()),
                () -> assertEquals(count, report.getBlacklistCandidatesCount())
        );
    }

    // TC-02, TC-03, TC-04
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideNullConstructorArguments")
    @DisplayName("TC-02 to TC-04: Should throw NullPointerException when any BigDecimal field is null")
    void testConstructorNullChecks(String testName, BigDecimal overstay, BigDecimal lost, BigDecimal misuse) {
        assertThrows(NullPointerException.class, () ->
                new PenaltySummaryReport(overstay, lost, misuse, 0)
        );
    }

    private static Stream<Arguments> provideNullConstructorArguments() {
        BigDecimal valid = BigDecimal.ZERO;
        return Stream.of(
                Arguments.of("TC-02: Null totalOverstay", null, valid, valid),
                Arguments.of("TC-03: Null totalLostTicket", valid, null, valid),
                Arguments.of("TC-04: Null totalMisuse", valid, valid, null)
        );
    }
}