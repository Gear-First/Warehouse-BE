package com.gearfirst.warehouse.api.receiving.dto;

public record ReceivingNoteLineResponse(
        Long lineId,
        ReceivingProductResponse product,
        int orderedQty,
        int inspectedQty,
        String status // PENDING | ACCEPTED | REJECTED (Mock model for current phase)
) {
}
