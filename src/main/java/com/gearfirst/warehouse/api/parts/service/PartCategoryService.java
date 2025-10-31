package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CategoryDetailResponse;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CategorySummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CreateCategoryRequest;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.UpdateCategoryRequest;
import java.util.List;

public interface PartCategoryService {
    List<CategorySummaryResponse> list(String keyword);

    CategoryDetailResponse get(Long id);

    CategoryDetailResponse create(CreateCategoryRequest req);

    CategoryDetailResponse update(Long id, UpdateCategoryRequest req);

    void delete(Long id);
}
