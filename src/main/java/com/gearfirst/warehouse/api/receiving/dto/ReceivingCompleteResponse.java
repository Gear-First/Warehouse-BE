package com.gearfirst.warehouse.api.receiving.dto;

public record ReceivingCompleteResponse(
        String completedAt, int appliedQtyTotal) {
}
