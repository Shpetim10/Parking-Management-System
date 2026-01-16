package Settings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class Settings {
    public final static BigInteger MAX_DURATION_HOURS = new BigInteger(String.valueOf(24));
    public final static BigInteger MAX_EXTRA_HOURS = new BigInteger(String.valueOf(24));
    public final static BigDecimal MAX_PENALTIES_ALLOWED = new BigDecimal(3);
    public final static Duration BLACKLIST_TIME_WINDOW= Duration.ofDays(30);

    public final static BigDecimal MAX_PRICE_CAPACITY = new BigDecimal(String.valueOf(1000000)); //1 million
    public final static BigDecimal TAX_RATIO= new BigDecimal(String.valueOf(0.2));

    public static final LocalTime START_PEAK_TIME = LocalTime.of(11, 0); // 11:00
    public static final LocalTime END_PEAK_TIME   = LocalTime.of(21, 0); // 21:00
}
