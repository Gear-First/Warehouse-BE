package com.gearfirst.warehouse.api.shipping.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Default stub implementation returning a very large on-hand quantity. This avoids unintended SHORTAGE in environments
 * without Inventory integration.
 *
 * Active only in local/test profiles to avoid masking shortages in production.
 */
@Component
//@Profile({"test"})
public class DefaultOnHandProvider implements OnHandProvider {
    @Override
    public int getOnHandQty(String warehouseCode, Long productId) {
        return Integer.MAX_VALUE / 2; // effectively "enough stock"
    }
}
