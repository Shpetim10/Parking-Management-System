package Settings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;

public final class Settings {
    public final static BigInteger MAX_DURATION_HOURS = new BigInteger(String.valueOf(24));
    public final static BigInteger MAX_EXTRA_HOURS = new BigInteger(String.valueOf(24));
    public final static BigDecimal MAX_PENALTIES_ALLOWED = new BigDecimal(3);
    public final static Duration BLACKLIST_TIME_WINDOW= Duration.ofDays(30);

}
