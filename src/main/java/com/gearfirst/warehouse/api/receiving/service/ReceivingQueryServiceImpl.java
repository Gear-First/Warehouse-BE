package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummary;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingSearchCond;
import com.gearfirst.warehouse.api.receiving.persistence.ReceivingQueryRepository;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceivingQueryServiceImpl implements ReceivingQueryService {

    private final ReceivingQueryRepository repository;

    @Override
    public PageEnvelope<ReceivingNoteSummaryResponse> search(ReceivingSearchCond cond, Pageable pageable) {
        Page<ReceivingNoteSummary> page = repository.search(cond, pageable);
        List<ReceivingNoteSummaryResponse> items = page.getContent().stream()
            .map(s -> new ReceivingNoteSummaryResponse(
                s.getNoteId(),
                s.getReceivingNo(),
                s.getSupplierName(),
                s.getItemKindsNumber(),
                s.getTotalQty(),
                s.getStatus().name(),
                s.getWarehouseCode(),
                s.getRequestedAt(),
                s.getExpectedReceiveDate(),
                s.getCompletedAt()
            ))
            .toList();
        return PageEnvelope.of(items, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
