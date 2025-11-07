package com.gearfirst.warehouse.api.shipping.persistence;

import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummary;
import com.gearfirst.warehouse.api.shipping.dto.ShippingSearchCond;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShippingQueryRepository {
    Page<ShippingNoteSummary> search(ShippingSearchCond cond, Pageable pageable);

    /**
     * Unpaged search used by legacy service overloads that apply paging at controller level.
     */
    List<ShippingNoteSummary> searchAll(ShippingSearchCond cond);

    /**
     * Count all shipping notes for the given KST local date based on requestedAt.
     * Includes all statuses.
     */
    long countByRequestedAtDateKst(LocalDate day);
}
