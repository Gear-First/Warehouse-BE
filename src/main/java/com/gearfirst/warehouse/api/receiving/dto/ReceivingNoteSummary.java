package com.gearfirst.warehouse.api.receiving.dto;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingNoteSummary {
    private Long noteId;
    private String receivingNo;
    private String warehouseCode;
    private String supplierName;

    // ISO-8601 strings (KST I/O policy can be applied at service/mapper if necessary)
    private String requestedAt;
    private String expectedReceiveDate;
    private String completedAt;

    private ReceivingNoteStatus status;
    private int itemKindsNumber;
    private int totalQty;
}
