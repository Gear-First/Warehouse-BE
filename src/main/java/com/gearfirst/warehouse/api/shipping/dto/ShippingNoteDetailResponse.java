package com.gearfirst.warehouse.api.shipping.dto;

import java.util.List;

public record ShippingNoteDetailResponse(
        Long noteId,
        String customerName,
        int itemKindsNumber,
        int totalQty,
        String status,       // PENDING | IN_PROGRESS | DELAYED | COMPLETED
        String completedAt,   // ISO8601 string or null
        List<ShippingNoteLineResponse> lines
) {
}
