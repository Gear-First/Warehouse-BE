package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.PartDtos.CategoryRef;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.CreatePartRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartDetailResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.UpdatePartRequest;
import com.gearfirst.warehouse.api.parts.dto.PartDto;
import com.gearfirst.warehouse.api.parts.persistence.PartCategoryJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.PartJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartCategoryEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartEntity;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartServiceImpl implements PartService {

    private final PartJpaRepository partRepo;
    private final PartCategoryJpaRepository categoryRepo;
    private final PartCarModelReader partCarModelReader;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    public PageEnvelope<PartSummaryResponse> list(String code, String name, Long categoryId, int page, int size,
                                                  java.util.List<String> sortParams) {
        String c = code == null ? "" : code;
        String n = name == null ? "" : name;
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        Sort sort = buildSort(sortParams);
        Pageable pageable = PageRequest.of(p, s, sort);

        Page<PartEntity> pageData;
        if (categoryId == null) {
            pageData = partRepo.findByEnabledTrueAndCodeContainingIgnoreCaseAndNameContainingIgnoreCase(c, n, pageable);
        } else {
            pageData = partRepo.findByEnabledTrueAndCodeContainingIgnoreCaseAndNameContainingIgnoreCaseAndCategoryId(c,
                    n, categoryId, pageable);
        }

        List<PartEntity> parts = pageData.getContent();
        // batch load categories to avoid N+1
        Set<Long> categoryIds = parts.stream().map(PartEntity::getCategoryId).collect(Collectors.toSet());
        Map<Long, String> catNames = categoryRepo.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(PartCategoryEntity::getId, PartCategoryEntity::getName));

        List<PartSummaryResponse> items = parts.stream()
                .map(pv -> new PartSummaryResponse(pv.getId(), pv.getCode(), pv.getName(),
                        new CategoryRef(pv.getCategoryId(), catNames.get(pv.getCategoryId()))))
                .toList();

        return PageEnvelope.of(items, pageData.getNumber(), pageData.getSize(), pageData.getTotalElements());
    }

    private Sort buildSort(java.util.List<String> sortParams) {
        // default: name,asc → code,asc
        Sort defaultSort = Sort.by(Sort.Order.asc("name").ignoreCase(), Sort.Order.asc("code").ignoreCase());
        if (sortParams == null || sortParams.isEmpty()) {
            return defaultSort;
        }
        try {
            List<Sort.Order> orders = sortParams.stream()
                    .map(s -> {
                        String[] arr = s.split(",");
                        String prop = arr[0].trim();
                        String dir = arr.length > 1 ? arr[1].trim().toLowerCase() : "asc";
                        Sort.Order o = "desc".equals(dir) ? Sort.Order.desc(prop) : Sort.Order.asc(prop);
                        return o.ignoreCase();
                    })
                    .toList();
            if (orders.isEmpty()) {
                return defaultSort;
            }
            return Sort.by(orders);
        } catch (Exception e) {
            return defaultSort;
        }
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

        String topic = "create-part";
        String categoryName = categoryRepo.findById(saved.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + req.categoryId())).getName();

        PartDto dto = PartDto.builder()
                .id(saved.getId())
                .category(categoryName)
                .partCode(saved.getCode())
                .partName(saved.getName())
                .supplierName(saved.getSupplierName())
                .build();

        kafkaTemplate.send(topic, dto);

        return toDetail(saved);
    }

    @Override
    public PartDetailResponse update(Long id, UpdatePartRequest req) {
        var p = partRepo.findById(id).orElseThrow(() -> new NotFoundException("Part not found: " + id));
        validateUpdate(req);
        if (!categoryRepo.existsById(req.categoryId())) {
            throw new NotFoundException("Category not found: " + req.categoryId());
        }
        if (req.code() != null && !req.code().equalsIgnoreCase(p.getCode()) && partRepo.existsByCodeIgnoreCase(
                req.code())) {
            throw new ConflictException(ErrorStatus.PART_CODE_ALREADY_EXISTS);
        }
        if (req.code() != null && !req.code().isBlank()) {
            p.setCode(req.code().trim());
        }
        p.setName(req.name().trim());
        p.setPrice(req.price());
        p.setCategoryId(req.categoryId());
        p.setImageUrl(req.imageUrl());
        if (req.enabled() != null) {
            p.setEnabled(req.enabled());
        }
        partRepo.save(p);
        return toDetail(p);
    }

    @Override
    public void delete(Long id) {
        var p = partRepo.findById(id).orElseThrow(() -> new NotFoundException("Part not found: " + id));
        // guard: block deletion when mappings exist (PCM ready)
        long mappingCount = partCarModelReader != null ? partCarModelReader.countByPartId(id) : 0L;
        if (mappingCount > 0) {
            throw new ConflictException(ErrorStatus.PART_HAS_MAPPINGS);
        }
        // soft delete per docs: enabled=false
        p.setEnabled(false);
        partRepo.save(p);
    }

    private void validateCreate(CreatePartRequest req) {
        if (req == null) {
            throw new BadRequestException(ErrorStatus.PART_NAME_INVALID);
        }
        if (req.code() == null || req.code().isBlank()) {
            throw new BadRequestException(ErrorStatus.PART_CODE_INVALID);
        }
        if (req.name() == null || req.name().trim().length() < 2 || req.name().length() > 100) {
            throw new BadRequestException(ErrorStatus.PART_NAME_INVALID);
        }
        if (req.price() == null || req.price() < 0) {
            throw new BadRequestException(ErrorStatus.PART_PRICE_INVALID);
        }
        if (req.categoryId() == null) {
            throw new BadRequestException(ErrorStatus.PART_CATEGORY_NAME_INVALID);
        }
    }

    private void validateUpdate(UpdatePartRequest req) {
        if (req == null) {
            throw new BadRequestException(ErrorStatus.PART_NAME_INVALID);
        }
        if (req.name() == null || req.name().trim().length() < 2 || req.name().length() > 100) {
            throw new BadRequestException(ErrorStatus.PART_NAME_INVALID);
        }
        if (req.price() == null || req.price() < 0) {
            throw new BadRequestException(ErrorStatus.PART_PRICE_INVALID);
        }
        if (req.categoryId() == null) {
            throw new BadRequestException(ErrorStatus.PART_CATEGORY_NAME_INVALID);
        }
        if (req.code() != null && req.code().isBlank()) {
            throw new BadRequestException(ErrorStatus.PART_CODE_INVALID);
        }
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
