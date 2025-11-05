package com.gearfirst.warehouse.api.inventory.dto;

public class OnHandDtos {
    public record PartRef(Long id, String code, String name) {
    }

    public record OnHandSummary(
            String warehouseCode,
            PartRef part,
            int onHandQty,
            String lastUpdatedAt,
            boolean lowStock,
            Integer safetyStockQty
    ) {
    }
}
