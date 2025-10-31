package com.gearfirst.warehouse.api.receiving.dto;

import java.util.List;

/**
 * Receiving note creation request (MVP). NOTE: Validation and business rules (e.g., lot format, request number
 * generation) are intentionally omitted for now.
 * TODO: Add value validations and receivingNo generation when rules are finalized.
 */
public record ReceivingCreateNoteRequest(
        String supplierName,
        String warehouseCode,
        String receivingNo,
        String requestedAt,
        String expectedReceiveDate,
        String remark,
        List<ReceivingCreateNoteRequest.Line> lines
) {
    public record Line(
            Long productId,
            Integer orderedQty,
            String lotNo,
            String lineRemark
    ) {
    }
}
