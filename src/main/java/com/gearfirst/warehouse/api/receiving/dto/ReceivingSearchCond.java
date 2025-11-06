package com.gearfirst.warehouse.api.receiving.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReceivingSearchCond {
    // status: not-done | done | all
    private final String status;

    // Unified text search: receivingNo | supplierName
    private final String q;

    // KST local date strings (yyyy-MM-dd). When both range present, range wins; if from>to, auto-swap
    private final String date;      // single day
    private final String dateFrom;  // range start
    private final String dateTo;    // range end

    private final String warehouseCode;
    private final String receivingNo;
    private final String supplierName;
}
