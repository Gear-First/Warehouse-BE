package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem;
import com.gearfirst.warehouse.api.parts.dto.PartSearchCond;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import org.springframework.data.domain.Pageable;

public interface PartQueryService {
    PageEnvelope<PartIntegratedItem> searchIntegrated(PartSearchCond cond, Pageable pageable);
}
