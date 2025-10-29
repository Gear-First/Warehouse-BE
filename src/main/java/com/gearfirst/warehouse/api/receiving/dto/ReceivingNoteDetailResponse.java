package com.gearfirst.warehouse.api.receiving.dto;

import java.util.List;

public record ReceivingNoteDetailResponse(
        Long noteId,
        String supplierName,
        int itemKindsNumber,
        int totalQty,
        String status,       // PENDING | IN_PROGRESS | COMPLETED_OK | COMPLETED_ISSUE
        String completedAt,   // ISO8601 string or null
        // Additive, nullable metadata fields (may be null until modeled)
        String receivingNo,
        Long warehouseId,
        String requestedAt,
        String expectedReceiveDate,
        String receivedAt,
        String inspectorName,
        String inspectorDept,
        String inspectorPhone,
        String remark,
        List<ReceivingNoteLineResponse> lines
) {
}
