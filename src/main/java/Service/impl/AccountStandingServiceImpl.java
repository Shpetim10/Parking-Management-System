package Service.impl;

import Service.AccountStandingService;
import Enum.*;
import Settings.Settings;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountStandingServiceImpl implements AccountStandingService {
    @Override
    public AccountStanding evaluateStanding(int penalties, int unpaidSessions, BigDecimal balance) {

        if (penalties == 0 && unpaidSessions == 0 && balance.signum() == 0)
            return AccountStanding.GOOD_STANDING;

        if (penalties >= 3 || unpaidSessions >= 2 || balance.compareTo(Settings.MAX_BALANCE) > 0)
            return AccountStanding.SUSPENDED;

        return AccountStanding.WARNING;
    }

    @Override
    public UserStatus deriveUserStatus(AccountStanding standing, boolean manualBlacklistFlag) {
        Objects.requireNonNull(standing);

        if (manualBlacklistFlag)
            return UserStatus.BLACKLISTED;

        return standing == AccountStanding.SUSPENDED
                ? UserStatus.INACTIVE
                : UserStatus.ACTIVE;
    }
}
