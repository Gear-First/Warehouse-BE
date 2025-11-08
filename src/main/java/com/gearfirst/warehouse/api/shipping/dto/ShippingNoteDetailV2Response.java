package com.gearfirst.warehouse.api.shipping.dto;

import java.util.List;

public record ShippingNoteDetailV2Response(
        Long noteId,
        String status,
        String completedAt,
        String delayedAt, // optional, may be null (not yet persisted)
        String inventorySnapshotAt,
        List<Line> lines
) {
    public record Line(
            Long lineId,
            Long productId,
            String productCode,
            String productName,
            Integer orderedQty,
            String currentStatus,
            Integer onHandQty,
            String suggestedStatus
    ) {}
}
