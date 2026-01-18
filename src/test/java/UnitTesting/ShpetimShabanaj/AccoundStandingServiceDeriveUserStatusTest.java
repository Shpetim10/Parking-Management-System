package UnitTesting.ShpetimShabanaj;

import Enum.UserStatus;
import Enum.AccountStanding;
import Service.AccountStandingService;
import Service.impl.AccountStandingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccoundStandingServiceDeriveUserStatusTest {
    private AccountStandingService accountStandingService;

    @BeforeEach
    public void init() {
        this.accountStandingService=new AccountStandingServiceImpl();
    }

    @Test
    @DisplayName("TC-01 & TC-02: Manual Blacklist should always return BLACKLISTED")
    void testManualBlacklistPrecedence() {
        // Blacklist is true, regardless of standing
        assertEquals(UserStatus.BLACKLISTED,
                accountStandingService.deriveUserStatus(AccountStanding.GOOD_STANDING, true));

        assertEquals(UserStatus.BLACKLISTED,
                accountStandingService.deriveUserStatus(AccountStanding.SUSPENDED, true));
    }

    //TC-03 - TC-05
    @ParameterizedTest(name = "Standing {0} with no blacklist should be {1}")
    @CsvSource({
            "SUSPENDED, INACTIVE",
            "GOOD_STANDING, ACTIVE",
            "WARNING, ACTIVE"
    })
    @DisplayName("TC-03, TC-04, TC-05: Status mapping based on AccountStanding")
    void testStatusMapping(AccountStanding standing, UserStatus expectedStatus) {
        UserStatus actualStatus = accountStandingService.deriveUserStatus(standing, false);

        assertEquals(expectedStatus, actualStatus,
                "User with standing " + standing + " should be " + expectedStatus);
    }

    //TC-06
    @Test
    @DisplayName("TC-06: Test for null account standing")
    void testForNullAccountStanding(){
        assertThrows(NullPointerException.class,
                ()->accountStandingService.deriveUserStatus(null, false));
    }
}
