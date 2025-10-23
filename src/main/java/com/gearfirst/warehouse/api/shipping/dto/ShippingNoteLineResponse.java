package com.gearfirst.warehouse.api.shipping.dto;

public record ShippingNoteLineResponse(
        Long lineId,
        ShippingProductResponse product,
        int orderedQty,
        int allocatedQty,
        int pickedQty,
        String status // PENDING | READY | SHORTAGE
) {
}
