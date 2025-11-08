package com.gearfirst.warehouse.api.shipping.service;

/**
 * SPI for providing current on-hand quantity for a product.
 *
 * Warehouse-aware: prefer {@link #getOnHandQty(String, Long)}. The legacy method without warehouse
 * delegates to {@code warehouseCode=null} for backward compatibility.
 */
public interface OnHandProvider {
    /**
     * Legacy: warehouse-agnostic on-hand. Implementations should delegate to the warehouse-aware method.
     */
    default int getOnHandQty(Long productId) {
        return getOnHandQty(null, productId);
    }

    /**
     * Returns current on-hand quantity for the given product in the specified warehouse.
     * Implementations should return a non-negative integer. When {@code warehouseCode} is null, the
     * behavior is implementation-defined (e.g., aggregate across all warehouses or return 0). Prefer specifying
     * the warehouse to avoid false READY/SHORTAGE derivations.
     */
    int getOnHandQty(String warehouseCode, Long productId);
}
