package com.giozar04.shared.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SharedUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public static long parseLong(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    public static double parseDouble(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public static ZonedDateTime parseZonedDateTime(Object value) {
        if (value instanceof String str) {
            try {
                return ZonedDateTime.parse(str, FORMATTER);
            } catch (Exception ignored) {}
        }
        return ZonedDateTime.now();
    }

    public static DateTimeFormatter getFormatter() {
        return FORMATTER;
    }
}
