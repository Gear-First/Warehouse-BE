package com.gearfirst.warehouse.api.parts.dto;

import jakarta.validation.constraints.Size;

public class PartCarModelDtos {

    public record CreateMappingRequest(
            Long carModelId,
            @Size(max = 200) String note,
            Boolean enabled
    ) {
    }

    public record UpdateMappingRequest(
            @Size(max = 200) String note,
            Boolean enabled
    ) {
    }

    public record PartCarModelDetail(
            Long partId,
            Long carModelId,
            String note,
            boolean enabled,
            String createdAt,
            String updatedAt
    ) {
    }
}
