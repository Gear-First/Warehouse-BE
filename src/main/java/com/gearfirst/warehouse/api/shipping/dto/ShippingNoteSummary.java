package com.gearfirst.warehouse.api.shipping.dto;

import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingNoteSummary {
    private Long noteId;
    private String shippingNo;
    private String warehouseCode;
    private String branchName;

    // ISO-8601 strings (KST I/O policy applied in repository)
    private String requestedAt;
    private String expectedShipDate;
    private String completedAt;

    private NoteStatus status;
    private int itemKindsNumber;
    private int totalQty;
}
