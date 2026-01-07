package Service;

import Enum.AccountStanding;
import Enum.UserStatus;
import java.math.BigDecimal;

public interface AccountStandingService {

    AccountStanding evaluateStanding(
            int penaltiesLast30Days,
            int unpaidSessions,
            BigDecimal unpaidBalance
    );

    UserStatus deriveUserStatus(
            AccountStanding standing,
            boolean manualBlacklistFlag
    );
}
