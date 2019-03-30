package base.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author csieflyman
 */
public class DateTimeUtils {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final String UTC_ZONE_ID_STRING = "UTC";
    public static final ZoneId UTC_ZONE_ID = ZoneId.of(UTC_ZONE_ID_STRING);
    public static final DateTimeFormatter UTC_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(UTC_ZONE_ID);
    public static final DateTimeFormatter UTC_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).withZone(UTC_ZONE_ID);

    public static final String LOCAL_ZONE_ID_STRING = "Asia/Taipei";
    public static final ZoneId LOCAL_ZONE_ID = ZoneId.of(LOCAL_ZONE_ID_STRING);
    public static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(LOCAL_ZONE_ID);
    public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(LOCAL_ZONE_ID);

    private DateTimeUtils() {

    }
}
