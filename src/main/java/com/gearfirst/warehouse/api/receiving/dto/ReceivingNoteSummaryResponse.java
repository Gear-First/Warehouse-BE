package com.gearfirst.warehouse.api.receiving.dto;

public record ReceivingNoteSummaryResponse(
        Long noteId,
        String supplierName,
        int itemKindsNumber,
        int totalQty,
        String status,       // PENDING | IN_PROGRESS | COMPLETED_OK | COMPLETED_ISSUE
        String warehouseCode,
        String requestedAt,  // ISO8601 string or null
        String completedAt   // ISO8601 string or null
) {
}
