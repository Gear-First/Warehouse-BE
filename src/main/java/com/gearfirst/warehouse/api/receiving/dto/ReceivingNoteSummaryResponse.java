package com.gearfirst.warehouse.api.receiving.dto;

public record ReceivingNoteSummaryResponse(
        Long noteId,
        String supplierName,
        int itemKindsNumber,
        int totalQty,
        String status,       // PENDING | IN_PROGRESS | DONE_OK | DONE_ISSU
        String completedAt   // ISO8601 string or null
) {
}
