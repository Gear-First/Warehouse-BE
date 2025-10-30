package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.util.List;

public interface InventoryService {

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

    /** Increase on-hand by qty for the given warehouse/part. */
    void increase(String warehouseCode, Long partId, int qty);

    /** Decrease on-hand by qty; must not go below zero (throws ConflictException on insufficient). */
    void decrease(String warehouseCode, Long partId, int qty);
}
