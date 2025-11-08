package com.gearfirst.warehouse.api.shipping.dto;

import java.util.List;

public record ShippingNoteDetailV2Response(
        Long noteId,
        String status,
        String completedAt,
        String delayedAt, // optional, may be null (not yet persisted)
        String inventorySnapshotAt,
        // --- All legacy note-level fields (to avoid two calls) ---
        String shippingNo,
        Long orderId,
        String branchName,
        String warehouseCode,
        String requestedAt,
        String expectedShipDate,
        String shippedAt,
        Integer itemKindsNumber,
        Integer totalQty,
        String assigneeName,
        String assigneeDept,
        String assigneePhone,
        String remark,
        List<Line> lines
) {
    public record Line(
            Long lineId,
            Long productId,
            String productCode,
            String productName,
            String productLot,
            String productImgUrl,
            Integer orderedQty,
            Integer pickedQty,
            String currentStatus,
            Integer onHandQty,
            String suggestedStatus
    ) {}
}
