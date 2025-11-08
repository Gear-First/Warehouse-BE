package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Inventory-backed implementation of {@link OnHandProvider}.
 *
 * Warehouse-aware: derives on-hand per specified warehouse. When {@code warehouseCode} is null/blank,
 * returns 0 to avoid false READY derivation (explicit warehouse is required in shipping context).
 */
@Component
@Primary
@RequiredArgsConstructor
public class InventoryOnHandProvider implements OnHandProvider {

    private final InventoryService inventoryService;

    @Override
    public int getOnHandQty(String warehouseCode, Long productId) {
        if (productId == null) return 0;
        if (warehouseCode == null || warehouseCode.isBlank()) {
            // No warehouse specified — in shipping we require per-warehouse checks; be conservative.
            return 0;
        }
        // Use advanced API with explicit filters so that server-side filtering reduces result set,
        // and stay within size constraints (<= 200). One row per (warehouse, part) is expected.
        var page = inventoryService.listOnHandAdvanced(
                null,            // q
                productId,       // partId filter
                null,            // partCode
                null,            // partName
                warehouseCode,   // warehouse filter
                null,            // supplierName
                null,            // minQty
                null,            // maxQty
                0,
                1,               // minimal page size; filtered result will contain our target if exists
                List.of()
        );
        return page.items().stream()
                .mapToInt(OnHandSummary::onHandQty)
                .sum();
    }
}
