package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.PartDtos.CreatePartRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartDetailResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.UpdatePartRequest;
import java.util.List;

public interface PartService {
    List<PartSummaryResponse> list(String code, String name, Long categoryId);
    PartDetailResponse get(Long id);
    PartDetailResponse create(CreatePartRequest req);
    PartDetailResponse update(Long id, UpdatePartRequest req);
    void delete(Long id);
}
