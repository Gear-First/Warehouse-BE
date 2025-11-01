package com.gearfirst.warehouse.api.shipping.service;

/**
 * SPI for providing current on-hand quantity for a product. For now, warehouse dimension is omitted (single-warehouse
 * assumption).
 */
public interface OnHandProvider {
    /**
     * Returns current on-hand quantity for the given product. Implementations should return a non-negative integer.
     */
    int getOnHandQty(Long productId);
}
