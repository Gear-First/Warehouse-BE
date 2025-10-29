package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.common.response.PageEnvelope;

public interface InventoryService {
    PageEnvelope<OnHandSummary> listOnHand(Long warehouseId, String keyword, int page, int size);

    /** Increase on-hand by qty for the given warehouse/part (warehouseId is optional for MVP). */
    void increase(Long warehouseId, Long partId, int qty);

    /** Decrease on-hand by qty; must not go below zero (throws ConflictException on insufficient). */
    void decrease(Long warehouseId, Long partId, int qty);
}
