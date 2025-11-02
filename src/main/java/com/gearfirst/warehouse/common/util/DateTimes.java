package com.gearfirst.warehouse.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    /**
     * Converts a UTC-based LocalDateTime (server persistence policy) to a KST LocalDate.
     * Returns null for null input.
     */
    public static LocalDate toKstDate(LocalDateTime utcLdt) {
        if (utcLdt == null) return null;
        OffsetDateTime asUtc = utcLdt.atOffset(ZoneOffset.UTC);
        return asUtc.atZoneSameInstant(KST).toLocalDate();
    }

    /**
     * Returns UTC OffsetDateTime bounds that correspond to the given KST local day.
     * fromInclusive <= t <= toInclusive in UTC yields the same calendar day in KST.
     */
    public static DayBounds kstDayBounds(LocalDate kstDay) {
        if (kstDay == null) return null;
        ZonedDateTime kstStart = kstDay.atStartOfDay(KST);
        ZonedDateTime kstEnd = kstDay.plusDays(1).atStartOfDay(KST).minusNanos(1);
        OffsetDateTime utcStart = kstStart.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime utcEnd = kstEnd.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        return new DayBounds(utcStart, utcEnd);
    }

    /**
     * Returns UTC bounds for a KST date range [from..to], inclusive on both ends.
     * If to < from, they are swapped.
     */
    public static DayBounds kstRangeBounds(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        if (from == null) from = to;
        if (to == null) to = from;
        if (to.isBefore(from)) {
            LocalDate tmp = from; from = to; to = tmp;
        }
        DayBounds start = kstDayBounds(from);
        DayBounds end = kstDayBounds(to);
        return new DayBounds(start.fromInclusive(), end.toInclusive());
    }

    /** Simple value object for UTC bounds. */
    public record DayBounds(OffsetDateTime fromInclusive, OffsetDateTime toInclusive) {}
}
