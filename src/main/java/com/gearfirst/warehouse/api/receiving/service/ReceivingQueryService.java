package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingSearchCond;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import org.springframework.data.domain.Pageable;

public interface ReceivingQueryService {
    PageEnvelope<ReceivingNoteSummaryResponse> search(ReceivingSearchCond cond, Pageable pageable);
}
