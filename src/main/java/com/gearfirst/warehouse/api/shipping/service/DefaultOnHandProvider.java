package com.gearfirst.warehouse.api.shipping.service;

import org.springframework.stereotype.Component;

/**
 * Default stub implementation returning a very large on-hand quantity.
 * This avoids unintended SHORTAGE in environments without Inventory integration.
 */
@Component
public class DefaultOnHandProvider implements OnHandProvider {
    @Override
    public int getOnHandQty(Long productId) {
        return Integer.MAX_VALUE / 2; // effectively "enough stock"
    }
}
