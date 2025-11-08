package com.gearfirst.warehouse.api.shipping.dto;

import java.util.List;

public record ShippingRecalcResponse(
        Long noteId,
        String currentStatus,
        String suggestedStatus,
        boolean hasShortage,
        int readyCount,
        int pendingCount,
        String inventorySnapshotAt,
        boolean applied,
        List<Line> lines
) {
    public record Line(
            Long lineId,
            Integer orderedQty,
            String currentStatus,
            Integer onHandQty,
            String suggestedStatus
    ) {}
}
