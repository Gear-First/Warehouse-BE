package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.util.List;

public interface InventoryService {

    /** Legacy list API kept for backward compatibility (/inventory/onhand). */
    PageEnvelope<OnHandSummary> listOnHand(
            String warehouseCode,
            String partKeyword,
            String supplierName,
            Integer minQty,
            Integer maxQty,
            int page,
            int size,
            List<String> sort
    );

    /** Advanced list API for UC-INV-002 (/inventory/on-hand). */
    default PageEnvelope<OnHandSummary> listOnHandAdvanced(
            String q,
            Long partId,
            String partCode,
            String partName,
            String warehouseCode,
            String supplierName,
            Integer minQty,
            Integer maxQty,
            int page,
            int size,
            List<String> sort
    ) {
        // Default fallback maps to legacy API with a best-effort keyword
        String keyword = (q != null && !q.isBlank()) ? q :
                ((partCode != null && !partCode.isBlank()) ? partCode :
                        ((partName != null && !partName.isBlank()) ? partName : null));
        return listOnHand(warehouseCode, keyword, supplierName, minQty, maxQty, page, size, sort);
    }

    /** Increase on-hand by qty for the given warehouse/part (warehouseId is optional for MVP). */
    void increase(String warehouseCode, Long partId, int qty);

    /** Increase with supplier attribution snapshot (nullable supplierName allowed). */
    default void increase(String warehouseCode, Long partId, int qty, String supplierName) {
        // default delegates to legacy method without supplier attribution
        increase(warehouseCode, partId, qty);
    }

    /**
     * Decrease on-hand by qty; must not go below zero (throws ConflictException on insufficient).
     */
    void decrease(String warehouseCode, Long partId, int qty);
}
