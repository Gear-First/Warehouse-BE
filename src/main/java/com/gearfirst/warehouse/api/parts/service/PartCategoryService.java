package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.*;

import java.util.List;

public interface PartCategoryService {
    List<CategorySummaryResponse> list(String keyword);
    CategoryDetailResponse get(Long id);
    CategoryDetailResponse create(CreateCategoryRequest req);
    CategoryDetailResponse update(Long id, UpdateCategoryRequest req);
    void delete(Long id);
}
