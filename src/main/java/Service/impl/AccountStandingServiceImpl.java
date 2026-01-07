package Service.impl;

import Service.AccountStandingService;
import Enum.*;

import java.math.BigDecimal;

public class AccountStandingServiceImpl implements AccountStandingService {

    private static final BigDecimal MAX_BALANCE = BigDecimal.valueOf(100);

    @Override
    public AccountStanding evaluateStanding(int penalties, int unpaidSessions, BigDecimal balance) {

        if (penalties == 0 && unpaidSessions == 0 && balance.signum() == 0)
            return AccountStanding.GOOD_STANDING;

        if (penalties >= 3 || unpaidSessions >= 2 || balance.compareTo(MAX_BALANCE) > 0)
            return AccountStanding.SUSPENDED;

        return AccountStanding.WARNING;
    }

    @Override
    public UserStatus deriveUserStatus(AccountStanding standing, boolean manualBlacklistFlag) {

        if (manualBlacklistFlag)
            return UserStatus.BLACKLISTED;

        return standing == AccountStanding.SUSPENDED
                ? UserStatus.INACTIVE
                : UserStatus.ACTIVE;
    }
}
