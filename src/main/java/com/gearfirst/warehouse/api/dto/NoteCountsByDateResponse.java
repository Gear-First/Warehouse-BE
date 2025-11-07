package com.gearfirst.warehouse.api.dto;

/**
 * Aggregated counts of receiving and shipping notes for a single KST local date (requestedAt 기준).
 */
public record NoteCountsByDateResponse(
    String date,
    long receivingCount,
    long shippingCount
) {}
