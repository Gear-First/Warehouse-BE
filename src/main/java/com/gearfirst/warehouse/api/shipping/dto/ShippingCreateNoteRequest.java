package com.gearfirst.warehouse.api.shipping.dto;

import java.util.List;

/**
 * Shipping note creation request (MVP).
 * NOTE: Business validations and shippingNo generation are intentionally omitted for now.
 * TODO: Add value validations and shippingNo generation when rules are finalized.
 */
public record ShippingCreateNoteRequest(
        String customerName,
        Long warehouseId,
        String requestedAt,
        String expectedShipDate,
        String remark,
        List<ShippingCreateNoteRequest.Line> lines
) {
    public record Line(
            Long productId,
            Integer orderedQty,
            String lineRemark
    ) {}
}
