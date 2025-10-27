package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CategoryDetailResponse;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CategorySummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.CreateCategoryRequest;
import com.gearfirst.warehouse.api.parts.dto.CategoryDtos.UpdateCategoryRequest;
import com.gearfirst.warehouse.api.parts.persistence.PartCategoryJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.PartJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartCategoryEntity;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartCategoryServiceImpl implements PartCategoryService {

    private final PartCategoryJpaRepository categoryRepo;
    private final PartJpaRepository partRepo;

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> list(String keyword) {
        var all = categoryRepo.findAll();
        return all.stream()
                .filter(c -> c.isEnabled())
                .filter(c -> keyword == null || keyword.isBlank() || c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(c -> new CategorySummaryResponse(c.getId(), c.getName(), c.getDescription()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDetailResponse get(Long id) {
        var c = categoryRepo.findById(id).orElseThrow(() -> new NotFoundException("Category not found: " + id));
        return new CategoryDetailResponse(c.getId(), c.getName(), c.getDescription(),
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : null,
                c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);
    }

    @Override
    public CategoryDetailResponse create(CreateCategoryRequest req) {
        if (req == null || req.name() == null || req.name().trim().length() < 2 || req.name().length() > 50) {
            throw new BadRequestException(ErrorStatus.PART_CATEGORY_NAME_INVALID);
        }
        if (categoryRepo.existsByNameIgnoreCase(req.name())) {
            throw new ConflictException(ErrorStatus.PART_CATEGORY_NAME_ALREADY_EXISTS);
        }
        var saved = categoryRepo.save(PartCategoryEntity.builder()
                .name(req.name().trim())
                .description(req.description())
                .enabled(true)
                .build());
        return get(saved.getId());
    }

    @Override
    public CategoryDetailResponse update(Long id, UpdateCategoryRequest req) {
        var c = categoryRepo.findById(id).orElseThrow(() -> new NotFoundException("Category not found: " + id));
        if (req == null || req.name() == null || req.name().trim().length() < 2 || req.name().length() > 50) {
            throw new BadRequestException(ErrorStatus.PART_CATEGORY_NAME_INVALID);
        }
        if (!c.getName().equalsIgnoreCase(req.name()) && categoryRepo.existsByNameIgnoreCase(req.name())) {
            throw new ConflictException(ErrorStatus.PART_CATEGORY_NAME_ALREADY_EXISTS);
        }
        c.setName(req.name().trim());
        c.setDescription(req.description());
        categoryRepo.save(c);
        return get(c.getId());
    }

    @Override
    public void delete(Long id) {
        var c = categoryRepo.findById(id).orElseThrow(() -> new NotFoundException("Category not found: " + id));
        long refCount = partRepo.countByCategoryId(c.getId());
        if (refCount > 0) {
            throw new ConflictException(ErrorStatus.PART_CATEGORY_HAS_PARTS);
        }
        categoryRepo.delete(c);
    }
}
