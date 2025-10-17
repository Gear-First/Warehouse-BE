package com.gearfirst.warehouse.api.receiving.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateLineRequest(
        @NotNull @Min(0) Integer inspectedQty,
        @NotNull @Min(0) Integer orderedQty,
        @NotNull String status // IN_PROGRESS | DONE_OK | DONE_ISSUE
) {
}
