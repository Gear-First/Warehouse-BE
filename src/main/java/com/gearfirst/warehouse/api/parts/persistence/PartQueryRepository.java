package com.gearfirst.warehouse.api.parts.persistence;

import com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem;
import com.gearfirst.warehouse.api.parts.dto.PartSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartQueryRepository {
    Page<PartIntegratedItem> search(PartSearchCond cond, Pageable pageable);
}
