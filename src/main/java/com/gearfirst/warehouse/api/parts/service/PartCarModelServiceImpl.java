package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.dto.CarModelDtos.CarModelSummary;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.CategoryRef;
import com.gearfirst.warehouse.api.parts.dto.PartDtos.PartSummaryResponse;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.CreateMappingRequest;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.PartCarModelDetail;
import com.gearfirst.warehouse.api.parts.dto.PartCarModelDtos.UpdateMappingRequest;
import com.gearfirst.warehouse.api.parts.persistence.CarModelJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.PartCarModelJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.PartJpaRepository;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartCarModelEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartCategoryEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartEntity;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gearfirst.warehouse.api.parts.persistence.PartCategoryJpaRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartCarModelServiceImpl implements PartCarModelService {

    private final PartCarModelJpaRepository pcmRepo;
    private final CarModelJpaRepository carModelRepo;
    private final PartJpaRepository partRepo;
    private final PartCategoryJpaRepository categoryRepo;

    @Override
    public List<CarModelSummary> listCarModelsByPart(Long partId, String name) {
        if (partId == null || !partRepo.existsById(partId)) {
            throw new NotFoundException("Part not found: " + partId);
        }
        String kw = name == null ? "" : name;
        var mappings = pcmRepo.findByPartIdAndEnabledTrue(partId);
        if (mappings.isEmpty()) return List.of();
        Set<Long> carModelIds = mappings.stream().map(m -> m.getCarModelId()).collect(Collectors.toSet());
        var carModels = carModelRepo.findAllById(carModelIds).stream()
                .filter(cm -> cm.isEnabled())
                .filter(cm -> kw.isBlank() || cm.getName().toLowerCase().contains(kw.toLowerCase()))
                .map(cm -> new CarModelSummary(cm.getId(), cm.getName()))
                .sorted(Comparator.comparing(CarModelSummary::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return carModels;
    }

    @Override
    public List<PartSummaryResponse> listPartsByCarModel(Long carModelId, String code, String name, Long categoryId) {
        if (carModelId == null || !carModelRepo.existsById(carModelId)) {
            throw new NotFoundException("CarModel not found: " + carModelId);
        }
        String c = code == null ? "" : code;
        String n = name == null ? "" : name;
        var mappings = pcmRepo.findByCarModelIdAndEnabledTrue(carModelId);
        if (mappings.isEmpty()) return List.of();
        Set<Long> partIds = mappings.stream().map(m -> m.getPartId()).collect(Collectors.toSet());
        List<PartEntity> parts = partRepo.findAllById(partIds).stream()
                .filter(p -> p.isEnabled())
                .filter(p -> c.isBlank() || p.getCode().toLowerCase().contains(c.toLowerCase()))
                .filter(p -> n.isBlank() || p.getName().toLowerCase().contains(n.toLowerCase()))
                .filter(p -> categoryId == null || p.getCategoryId().equals(categoryId))
                .toList();
        // batch load categories to avoid N+1
        Set<Long> categoryIds = parts.stream().map(PartEntity::getCategoryId).collect(Collectors.toSet());
        Map<Long, String> catNames = categoryRepo.findAllById(categoryIds).stream()
                .collect(java.util.stream.Collectors.toMap(PartCategoryEntity::getId, PartCategoryEntity::getName));
        var summaries = parts.stream()
                .map(p -> new PartSummaryResponse(p.getId(), p.getCode(), p.getName(),
                        new CategoryRef(p.getCategoryId(), catNames.get(p.getCategoryId()))))
                .sorted(Comparator
                        .comparing(PartSummaryResponse::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PartSummaryResponse::code, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return summaries;
    }

    @Override
    @Transactional
    public PartCarModelDetail createMapping(Long partId, CreateMappingRequest request) {
        if (partId == null || !partRepo.existsById(partId)) {
            throw new NotFoundException("Part not found: " + partId);
        }
        if (request == null || request.carModelId() == null || !carModelRepo.existsById(request.carModelId())) {
            throw new NotFoundException("CarModel not found: " + (request == null ? null : request.carModelId()));
        }
        Optional<com.gearfirst.warehouse.api.parts.persistence.entity.PartCarModelEntity> existingOpt = pcmRepo.findByPartIdAndCarModelId(partId, request.carModelId());
        boolean enableVal = request.enabled() == null ? true : request.enabled();
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (existing.isEnabled()) {
                throw new ConflictException(ErrorStatus.PCM_ALREADY_EXISTS);
            }
            // Reactivate disabled mapping
            existing.setEnabled(enableVal);
            existing.setNote(request.note());
            var saved = pcmRepo.save(existing);
            return toDetail(saved);
        }
        var entity = PartCarModelEntity.builder()
                .partId(partId)
                .carModelId(request.carModelId())
                .note(request.note())
                .enabled(enableVal)
                .build();
        var saved = pcmRepo.save(entity);
        return toDetail(saved);
    }

    @Override
    @Transactional
    public PartCarModelDetail updateMapping(Long partId, Long carModelId, UpdateMappingRequest request) {
        var pcm = pcmRepo.findByPartIdAndCarModelId(partId, carModelId)
                .filter(PartCarModelEntity::isEnabled)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PCM_NOT_FOUND.getMessage()));
        if (request != null) {
            if (request.note() != null) pcm.setNote(request.note());
            if (request.enabled() != null) pcm.setEnabled(request.enabled());
        }
        var saved = pcmRepo.save(pcm);
        return toDetail(saved);
    }

    @Override
    @Transactional
    public void deleteMapping(Long partId, Long carModelId) {
        var pcm = pcmRepo.findByPartIdAndCarModelId(partId, carModelId)
                .filter(PartCarModelEntity::isEnabled)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.PCM_NOT_FOUND.getMessage()));
        pcm.setEnabled(false);
        pcmRepo.save(pcm);
    }

    private PartCarModelDetail toDetail(PartCarModelEntity e) {
        String createdAt = e.getCreatedAt() != null ? e.getCreatedAt().toString() : null;
        String updatedAt = e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null;
        return new PartCarModelDetail(e.getPartId(), e.getCarModelId(), e.getNote(), e.isEnabled(), createdAt, updatedAt);
    }
}
