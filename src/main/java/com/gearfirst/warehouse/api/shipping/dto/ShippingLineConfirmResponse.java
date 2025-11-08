package com.gearfirst.warehouse.api.shipping.dto;

public record ShippingLineConfirmResponse(
        Long noteId,
        Long lineId,
        Integer orderedQty,
        Integer onHandQty,
        String previousStatus,
        String suggestedStatus,
        String currentStatus,
        String noteStatus,
        String inventorySnapshotAt
) {}
