package com.gearfirst.warehouse.api.parts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CarModelDtos {
    public record CreateCarModelRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            Boolean enabled
    ) {}

    public record CarModelSummary(Long id, String name) { }
    public record CarModelListItem(Long id, String name, boolean enabled) { }
}
