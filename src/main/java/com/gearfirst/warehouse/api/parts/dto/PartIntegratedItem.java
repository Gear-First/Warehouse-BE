package com.gearfirst.warehouse.api.parts.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartIntegratedItem {
    // Part core
    private Long id;
    private String code;
    private String name;
    private Integer price;
    private String imageUrl;
    private Integer safetyStockQty;
    private boolean enabled;

    // Category summary
    private Long categoryId;
    private String categoryName;

    // Car models: id + name pairs
    @Builder.Default
    private List<CarModelDtos.CarModelSummary> carModels = List.of();
}
