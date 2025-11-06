package com.gearfirst.warehouse.api.shipping.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShippingSearchCond {
    // status: not-done | done | all
    private final String status;

    // KST local date strings (yyyy-MM-dd). When both range present, range wins; if from>to, auto-swap
    private final String date;      // single day
    private final String dateFrom;  // range start
    private final String dateTo;    // range end

    private final String warehouseCode;
    private final String shippingNo;
    private final String branchName;
}
