package com.gearfirst.warehouse.api.parts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class CategoryDtos {
    public record CreateCategoryRequest(
            @NotBlank @Size(min = 2, max = 50) String name,
            @Size(max = 200) String description
    ) {}

    public record UpdateCategoryRequest(
            @NotBlank @Size(min = 2, max = 50) String name,
            @Size(max = 200) String description
    ) {}

    public record CategorySummaryResponse(
            Long id,
            String name,
            String description
    ) {}

    public record CategoryDetailResponse(
            Long id,
            String name,
            String description,
            String createdAt,
            String updatedAt
    ) {}
}
