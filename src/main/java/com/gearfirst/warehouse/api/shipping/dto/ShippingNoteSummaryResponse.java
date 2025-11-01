package com.gearfirst.warehouse.api.shipping.dto;

public record ShippingNoteSummaryResponse(
        Long noteId,
        String customerName,
        int itemKindsNumber,
        int totalQty,
        String status,       // PENDING | IN_PROGRESS | DELAYED | COMPLETED
        String requestedAt,  // ISO8601 string or null
        String completedAt   // ISO8601 string or null
) {
}
