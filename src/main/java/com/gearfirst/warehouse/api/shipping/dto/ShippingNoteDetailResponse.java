package com.gearfirst.warehouse.api.shipping.dto;

import java.util.List;

public record ShippingNoteDetailResponse(
        Long noteId,
        String customerName,
        int itemKindsNumber,
        int totalQty,
        String status,       // PENDING | IN_PROGRESS | DELAYED | COMPLETED
        String completedAt,   // ISO8601 string or null
        // Additive, nullable metadata fields (may be null until modeled)
        String shippingNo,
        Long warehouseId,
        String requestedAt,
        String expectedShipDate,
        String shippedAt,
        String assigneeName,
        String assigneeDept,
        String assigneePhone,
        String remark,
        List<ShippingNoteLineResponse> lines
) {
}
