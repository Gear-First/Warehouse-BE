package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.common.response.PageEnvelope;

public interface InventoryService {
    PageEnvelope<OnHandSummary> listOnHand(Long warehouseId, String keyword, int page, int size);
}
