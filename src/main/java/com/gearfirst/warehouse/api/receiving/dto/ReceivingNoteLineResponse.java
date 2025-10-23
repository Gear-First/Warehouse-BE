package com.gearfirst.warehouse.api.receiving.dto;

public record ReceivingNoteLineResponse(
        Long lineId,
        ReceivingProductResponse product,
        int orderedQty,
        int inspectedQty,
        int issueQty,
        String status // NOT_STARTED | IN_PROGRESS | DONE_OK | DONE_ISSUE
) {
}
