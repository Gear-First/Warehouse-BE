package com.gearfirst.warehouse.common.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Time utilities for API I/O.
 *
 * Policy: API inputs/outputs use KST (+09:00) formatted ISO-8601 strings,
 * while server persists/operates in UTC. Use these helpers when serializing
 * entity timestamps to response DTOs.
 */
public final class DateTimes {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private DateTimes() {}

    /**
     * Formats the given UTC (or any-offset) time as a KST ISO-8601 string.
     * Returns null if input is null.
     */
    public static String toKstString(OffsetDateTime odt) {
        if (odt == null) return null;
        return odt.atZoneSameInstant(KST).format(ISO_OFFSET);
        
    }
}
