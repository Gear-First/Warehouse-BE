package com.gearfirst.warehouse.api.inventory.dto;

public class OnHandDtos {
    public record PartRef(Long id, String code, String name) {
    }

    public record OnHandSummary(
            String warehouseCode,
            PartRef part,
            int onHandQty,
            String updatedAt,
            boolean lowStock,
            Integer safetyStockQty,
            String supplierName,
            Integer price,
            Integer priceTotal
    ) {
        // Backward-compatible constructor used by existing tests (without supplierName/price fields)
        public OnHandSummary(String warehouseCode,
                              PartRef part,
                              int onHandQty,
                              String updatedAt,
                              boolean lowStock,
                              Integer safetyStockQty) {
            this(warehouseCode, part, onHandQty, updatedAt, lowStock, safetyStockQty, null, null, null);
        }
    }
}
