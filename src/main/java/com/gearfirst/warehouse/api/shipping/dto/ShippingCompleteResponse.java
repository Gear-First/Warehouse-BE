package com.gearfirst.warehouse.api.shipping.dto;

public record ShippingCompleteResponse(
        String completedAt,
        int totalShippedQty
) {
}
