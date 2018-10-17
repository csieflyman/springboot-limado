package com.csieflyman.limado.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author James Lin
 */
public class DateTimeUtils {

    public static final String TIMEZONE_ID = "UTC";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(ZoneId.of(TIMEZONE_ID));
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).withZone(ZoneId.of(TIMEZONE_ID));
    public static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of(TIMEZONE_ID));
    public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of(TIMEZONE_ID));

    private DateTimeUtils() { }
}
