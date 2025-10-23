package com.gearfirst.warehouse.api.shipping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateLineRequest(
        @NotNull @Min(0) Integer allocatedQty,
        @NotNull @Min(0) Integer pickedQty,
        @NotNull @Pattern(regexp = "PENDING|READY|SHORTAGE") String status // PENDING | READY | SHORTAGE
) {
}
