package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.PartDtos.CreatePartRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartDetailResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.UpdatePartRequest;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.util.List;

public interface PartService {
    // Server-side pagination & sorting (default: name,asc → code,asc)
    PageEnvelope<PartSummaryResponse> list(String code, String name, Long categoryId, int page, int size, java.util.List<String> sort);
    PartDetailResponse get(Long id);
    PartDetailResponse create(CreatePartRequest req);
    PartDetailResponse update(Long id, UpdatePartRequest req);
    void delete(Long id);
}
