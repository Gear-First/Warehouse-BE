package com.gearfirst.warehouse.api.shipping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateLineRequest(
        @NotNull @Min(0) Integer allocatedQty,
        @NotNull @Min(0) Integer pickedQty
) {
}
