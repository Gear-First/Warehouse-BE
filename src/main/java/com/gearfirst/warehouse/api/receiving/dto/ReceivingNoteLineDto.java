package com.gearfirst.warehouse.api.receiving.dto;

public record ReceivingNoteLineDto(
        Long lineId,
        ReceivingProductDto product,
        int orderedQty,
        int inspectedQty,
        int issueQty,
        String status // NOT_STARTED | IN_PROGRESS | DONE_OK | DONE_ISSUE
) {
}
