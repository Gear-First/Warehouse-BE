package com.gearfirst.warehouse.api.receiving.persistence;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummary;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingSearchCond;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReceivingQueryRepository {
    Page<ReceivingNoteSummary> search(ReceivingSearchCond cond, Pageable pageable);

    /**
     * Count all receiving notes for the given KST local date based on requestedAt.
     * Includes all statuses.
     */
    long countByRequestedAtDateKst(LocalDate day);
}
