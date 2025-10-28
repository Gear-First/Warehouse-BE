package com.gearfirst.warehouse.api.receiving.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReceivingUpdateLineRequest(
        @NotNull @Min(0) Integer inspectedQty,
        @NotNull Boolean hasIssue
) {
}
