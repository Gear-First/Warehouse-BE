package com.gearfirst.warehouse.api.shipping.dto;

import java.util.List;

/**
 * Shipping note creation request (MVP).
 * Note: shippingNo input is removed; server will always generate it.
 */
public record ShippingCreateNoteRequest(
        String branchName,
        String warehouseCode,
        Long orderId,
        String requestedAt,
        String expectedShipDate,
        String remark,
        List<ShippingCreateNoteRequest.Line> lines
) {
    public record Line(
            Long productId,
            Integer orderedQty,
            String lineRemark
    ) {
    }
}
