package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.PartRef;
import com.gearfirst.warehouse.api.inventory.persistence.InventoryOnHandJpaRepository;
import com.gearfirst.warehouse.api.inventory.persistence.entity.InventoryOnHandEntity;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@lombok.RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryOnHandJpaRepository jpa;
    private final com.gearfirst.warehouse.api.parts.persistence.PartJpaRepository parts;

    @Override
    @Transactional(readOnly = true)
    public PageEnvelope<OnHandSummary> listOnHand(Long warehouseId, String keyword, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        List<InventoryOnHandEntity> entities;
        if (warehouseId != null) {
            entities = jpa.findAllByWarehouseId(warehouseId);
        } else {
            entities = jpa.findAll();
        }
        var items = new ArrayList<OnHandSummary>();
        // Load Part names for better UX
        var partIds = entities.stream().map(InventoryOnHandEntity::getPartId).distinct().toList();
        var partMap = new java.util.HashMap<Long, com.gearfirst.warehouse.api.parts.persistence.entity.PartEntity>();
        if (!partIds.isEmpty()) {
            for (var p : parts.findAllById(partIds)) {
                partMap.put(p.getId(), p);
            }
        }
        for (var e : entities) {
            var pe = partMap.get(e.getPartId());
            String code = pe != null ? pe.getCode() : ("P-" + e.getPartId());
            String name = pe != null ? pe.getName() : null;
            var partRef = new PartRef(e.getPartId(), code, name);
            var last = e.getLastUpdatedAt() != null ? e.getLastUpdatedAt().toString() : null;
            items.add(new OnHandSummary(e.getWarehouseId(), partRef, e.getOnHandQty(), last));
        }
        // keyword filter (by code/name)
        var filtered = items.stream()
                .filter(i -> keyword == null || keyword.isBlank() || containsIgnoreCase(i.part().code(), keyword) || containsIgnoreCase(i.part().name(), keyword))
                .toList();
        long total = filtered.size();
        int from = Math.min(page * size, (int) total);
        int to = Math.min(from + size, (int) total);
        return PageEnvelope.of(filtered.subList(from, to), page, size, total);
    }

    @Override
    @Transactional
    public void increase(Long warehouseId, Long partId, int qty) {
        if (qty <= 0 || partId == null) return;
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var entity = jpa.findByWarehouseIdAndPartId(warehouseId, partId)
                .orElseGet(() -> InventoryOnHandEntity.builder()
                        .warehouseId(warehouseId)
                        .partId(partId)
                        .onHandQty(0)
                        .lastUpdatedAt(now)
                        .build());
        entity.increase(qty, now);
        jpa.save(entity);
    }

    @Override
    @Transactional
    public void decrease(Long warehouseId, Long partId, int qty) {
        if (qty <= 0 || partId == null) return;
        var entity = jpa.findByWarehouseIdAndPartId(warehouseId, partId)
                .orElseGet(() -> InventoryOnHandEntity.builder()
                        .warehouseId(warehouseId)
                        .partId(partId)
                        .onHandQty(0)
                        .lastUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build());
        int current = entity.getOnHandQty() == null ? 0 : entity.getOnHandQty();
        if (current < qty) {
            throw new ConflictException(ErrorStatus.CONFLICT_INVENTORY_INSUFFICIENT);
        }
        entity.decrease(qty, OffsetDateTime.now(ZoneOffset.UTC));
        jpa.save(entity);
    }

    private boolean containsIgnoreCase(String text, String kw) {
        if (text == null) return false;
        return text.toLowerCase(java.util.Locale.ROOT).contains(kw.toLowerCase(java.util.Locale.ROOT));
    }
}
