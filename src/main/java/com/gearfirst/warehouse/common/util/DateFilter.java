package com.gearfirst.warehouse.common.util;

import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import java.time.LocalDate;

/**
 * Utility for normalizing date filters used by list endpoints.
 * Policy:
 * - Inputs are KST local-day strings in yyyy-MM-dd.
 * - When both date and range are provided, range(dateFrom/dateTo) wins.
 * - If only single date is provided, it is used as both from/to.
 * - If dateFrom > dateTo, they are swapped.
 * - On parse error, throws BadRequestException with standard status.
 */
public final class DateFilter {

    private DateFilter() {}

    public static Normalized normalize(String date, String dateFrom, String dateTo) {
        LocalDate fromLd = null;
        LocalDate toLd = null;
        try {
            fromLd = (dateFrom == null || dateFrom.isBlank()) ? null : LocalDate.parse(dateFrom);
            toLd = (dateTo == null || dateTo.isBlank()) ? null : LocalDate.parse(dateTo);
            if (fromLd == null && toLd == null && date != null && !date.isBlank()) {
                LocalDate d = LocalDate.parse(date);
                fromLd = d;
                toLd = d;
            }
            if (fromLd != null && toLd != null && toLd.isBefore(fromLd)) {
                LocalDate tmp = fromLd; fromLd = toLd; toLd = tmp;
            }
        } catch (Exception e) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        String from = fromLd == null ? null : fromLd.toString();
        String to = toLd == null ? null : toLd.toString();
        // hasRange should reflect explicit range parameters (dateFrom/dateTo) presence,
        // not a derived single-date normalization. That ensures controller uses single-date overload when only `date` is provided.
        boolean hasRange = (dateFrom != null && !dateFrom.isBlank()) || (dateTo != null && !dateTo.isBlank());
        return new Normalized(from, to, hasRange);
    }

    public record Normalized(String from, String to, boolean hasRange) {}
}
