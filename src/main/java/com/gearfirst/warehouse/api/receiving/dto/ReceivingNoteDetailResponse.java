package com.gearfirst.warehouse.api.receiving.dto;

import java.util.List;

public record ReceivingNoteDetailResponse(
        Long noteId,
        String supplierName,
        int itemKindsNumber,
        int totalQty,
        String status,       // NOT_STARTED | IN_PROGRESS | DONE_OK | DONE_ISSU
        String completedAt,   // ISO8601 string or null
        List<ReceivingNoteLineDto> lines
) {
}
