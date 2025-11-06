package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem;
import com.gearfirst.warehouse.api.parts.dto.PartSearchCond;
import com.gearfirst.warehouse.api.parts.persistence.PartQueryRepository;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartQueryServiceImpl implements PartQueryService {

    private final PartQueryRepository partQueryRepository;

    @Override
    public PageEnvelope<PartIntegratedItem> searchIntegrated(PartSearchCond cond, Pageable pageable) {
        Page<PartIntegratedItem> page = partQueryRepository.search(cond, pageable);
        return PageEnvelope.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
