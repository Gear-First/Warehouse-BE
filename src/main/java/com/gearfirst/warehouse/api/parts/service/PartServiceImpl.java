package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.PartDtos.*;
import com.gearfirst.warehouse.api.parts.persistence.PartCategoryJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.PartJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartCategoryEntity;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PartServiceImpl implements PartService {

    private final PartJpaRepository partRepo;
    private final PartCategoryJpaRepository categoryRepo;

    @Override
    @Transactional(readOnly = true)
    public List<PartSummaryResponse> list(String code, String name, Long categoryId) {
        var list = partRepo.findAll();
        return list.stream()
                .filter(p -> p.isEnabled())
                .filter(p -> code == null || code.isBlank() || p.getCode().toLowerCase().contains(code.toLowerCase()))
                .filter(p -> name == null || name.isBlank() || p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> categoryId == null || p.getCategoryId().equals(categoryId))
                .map(p -> new PartSummaryResponse(p.getId(), p.getCode(), p.getName(),
                        new CategoryRef(p.getCategoryId(), resolveCategoryName(p.getCategoryId()))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PartDetailResponse get(Long id) {
        var p = partRepo.findById(id).orElseThrow(() -> new NotFoundException("Part not found: " + id));
        return toDetail(p);
    }

    @Override
    public PartDetailResponse create(CreatePartRequest req) {
        validateCreate(req);
        if (!categoryRepo.existsById(req.categoryId())) {
            throw new NotFoundException("Category not found: " + req.categoryId());
        }
        if (partRepo.existsByCodeIgnoreCase(req.code())) {
            throw new ConflictException(ErrorStatus.PART_CODE_ALREADY_EXISTS);
        }
        var saved = partRepo.save(PartEntity.builder()
                .code(req.code().trim())
                .name(req.name().trim())
                .price(req.price())
                .categoryId(req.categoryId())
                .imageUrl(req.imageUrl())
                .enabled(true)
                .build());
        return toDetail(saved);
    }

    @Override
    public PartDetailResponse update(Long id, UpdatePartRequest req) {
        var p = partRepo.findById(id).orElseThrow(() -> new NotFoundException("Part not found: " + id));
        validateUpdate(req);
        if (!categoryRepo.existsById(req.categoryId())) {
            throw new NotFoundException("Category not found: " + req.categoryId());
        }
        if (req.code() != null && !req.code().equalsIgnoreCase(p.getCode()) && partRepo.existsByCodeIgnoreCase(req.code())) {
            throw new ConflictException(ErrorStatus.PART_CODE_ALREADY_EXISTS);
        }
        if (req.code() != null && !req.code().isBlank()) p.setCode(req.code().trim());
        p.setName(req.name().trim());
        p.setPrice(req.price());
        p.setCategoryId(req.categoryId());
        p.setImageUrl(req.imageUrl());
        if (req.enabled() != null) p.setEnabled(req.enabled());
        partRepo.save(p);
        return toDetail(p);
    }

    @Override
    public void delete(Long id) {
        var p = partRepo.findById(id).orElseThrow(() -> new NotFoundException("Part not found: " + id));
        // soft delete per docs: enabled=false
        p.setEnabled(false);
        partRepo.save(p);
    }

    private void validateCreate(CreatePartRequest req) {
        if (req == null) throw new BadRequestException(ErrorStatus.PART_NAME_INVALID);
        if (req.code() == null || req.code().isBlank()) throw new BadRequestException(ErrorStatus.PART_CODE_INVALID);
        if (req.name() == null || req.name().trim().length() < 1 || req.name().length() > 100) throw new BadRequestException(ErrorStatus.PART_NAME_INVALID);
        if (req.price() == null || req.price() < 0) throw new BadRequestException(ErrorStatus.PART_PRICE_INVALID);
        if (req.categoryId() == null) throw new BadRequestException(ErrorStatus.PART_CATEGORY_NAME_INVALID);
    }

    private void validateUpdate(UpdatePartRequest req) {
        if (req == null) throw new BadRequestException(ErrorStatus.PART_NAME_INVALID);
        if (req.name() == null || req.name().trim().length() < 2 || req.name().length() > 100) throw new BadRequestException(ErrorStatus.PART_NAME_INVALID);
        if (req.price() == null || req.price() < 0) throw new BadRequestException(ErrorStatus.PART_PRICE_INVALID);
        if (req.categoryId() == null) throw new BadRequestException(ErrorStatus.PART_CATEGORY_NAME_INVALID);
        if (req.code() != null && req.code().isBlank()) throw new BadRequestException(ErrorStatus.PART_CODE_INVALID);
    }

    private PartDetailResponse toDetail(PartEntity p) {
        return new PartDetailResponse(
                p.getId(), p.getCode(), p.getName(), p.getPrice(),
                new CategoryRef(p.getCategoryId(), resolveCategoryName(p.getCategoryId())),
                p.getImageUrl(), p.isEnabled(),
                p.getCreatedAt() != null ? p.getCreatedAt().toString() : null,
                p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null
        );
    }

    private String resolveCategoryName(Long categoryId) {
        return categoryRepo.findById(categoryId).map(PartCategoryEntity::getName).orElse(null);
    }
}
