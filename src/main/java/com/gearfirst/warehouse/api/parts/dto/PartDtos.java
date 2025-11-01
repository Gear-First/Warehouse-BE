package com.gearfirst.warehouse.api.parts.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PartDtos {

    public record CreatePartRequest(
            @NotBlank String code,
            @NotBlank @Size(min = 2, max = 100) String name,
            @NotNull @Min(0) Integer price,
            @NotNull Long categoryId,
            String imageUrl
    ) {
    }

    public record UpdatePartRequest(
            String code,
            @NotBlank @Size(min = 2, max = 100) String name,
            @NotNull @Min(0) Integer price,
            @NotNull Long categoryId,
            String imageUrl,
            Boolean enabled
    ) {
    }

    public record PartSummaryResponse(
            Long id,
            String code,
            String name,
            CategoryRef category
    ) {
    }

    public record PartDetailResponse(
            Long id,
            String code,
            String name,
            Integer price,
            CategoryRef category,
            String imageUrl,
            boolean enabled,
            String createdAt,
            String updatedAt
    ) {
    }

    public record CategoryRef(Long id, String name) {
    }
}
