package com.gearfirst.warehouse.api.shipping.dto;

public record ShippingNoteSummaryResponse(
        Long noteId,
        String shippingNo,
        String branchName,
        int itemKindsNumber,
        int totalQty,
        String status,       // PENDING | IN_PROGRESS | DELAYED | COMPLETED
        String warehouseCode,
        String requestedAt,  // ISO8601 string or null
        String completedAt   // ISO8601 string or null
) {
}
