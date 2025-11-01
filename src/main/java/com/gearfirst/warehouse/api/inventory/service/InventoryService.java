package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.common.response.PageEnvelope;

public interface InventoryService {
    /** Legacy list API (used by existing tests). */
    PageEnvelope<OnHandSummary> listOnHand(Long warehouseId, String keyword, int page, int size);

    /** New list API used by controller: accepts warehouseCode and extended filters. */
    default PageEnvelope<OnHandSummary> listOnHand(
            String warehouseCode,
            String partKeyword,
            String supplierName,
            Integer minQty,
            Integer maxQty,
            int page,
            int size,
            java.util.List<String> sort
    ) {
        // For now, delegate to legacy method and ignore extra filters; Impl may override later.
        return listOnHand(null, partKeyword, page, size);
    }

    /** Increase on-hand by qty for the given warehouse/part (warehouseId is optional for MVP). */
    void increase(Long warehouseId, Long partId, int qty);

    /** Decrease on-hand by qty; must not go below zero (throws ConflictException on insufficient). */
    void decrease(Long warehouseId, Long partId, int qty);
}
