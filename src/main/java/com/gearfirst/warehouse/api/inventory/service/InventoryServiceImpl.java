package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.PartRef;
import com.gearfirst.warehouse.api.inventory.persistence.InventoryOnHandJpaRepository;
import com.gearfirst.warehouse.api.inventory.persistence.entity.InventoryOnHandEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.PartEntity;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@lombok.RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryOnHandJpaRepository repo;
    private final com.gearfirst.warehouse.api.parts.persistence.PartJpaRepository parts;

    @Override
    @Transactional(readOnly = true)
    public PageEnvelope<OnHandSummary> listOnHand(
            String warehouseCode,
            String partKeyword,
            String supplierName,
            Integer minQty,
            Integer maxQty,
            int page,
            int size,
            List<String> sort
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        if (minQty != null && maxQty != null && minQty > maxQty) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        List<InventoryOnHandEntity> entities = (warehouseCode == null || warehouseCode.isBlank())
                ? repo.findAll()
                : repo.findAllByWarehouseCode(warehouseCode);

        // Enrich with Part data
        var partIds = entities.stream().map(InventoryOnHandEntity::getPartId).distinct().toList();
        var partMap = new HashMap<Long, PartEntity>();
        if (!partIds.isEmpty()) {
            for (var p : parts.findAllById(partIds)) {
                partMap.put(p.getId(), p);
            }
        }
        var items = new ArrayList<OnHandSummary>(entities.size());
        for (var e : entities) {
            var pe = partMap.get(e.getPartId());
            String code = pe != null ? pe.getCode() : ("P-" + e.getPartId());
            String name = pe != null ? pe.getName() : null;
            var partRef = new PartRef(e.getPartId(), code, name);
            var last = com.gearfirst.warehouse.common.util.DateTimes.toKstString(e.getLastUpdatedAt());
            items.add(new OnHandSummary(e.getWarehouseCode(), partRef, e.getOnHandQty(), last));
        }
        // Filters: partKeyword (code|name), supplierName (part attribution), qty range
        var filtered = items.stream()
                .filter(i -> partKeyword == null || partKeyword.isBlank()
                        || containsIgnoreCase(i.part().code(), partKeyword)
                        || containsIgnoreCase(i.part().name(), partKeyword))
                .filter(i -> {
                    if (supplierName == null || supplierName.isBlank()) {
                        return true;
                    }
                    var pe = partMap.get(i.part().id());
                    var sname = (pe == null ? null : pe.getSupplierName());
                    return containsIgnoreCase(sname, supplierName);
                })
                .filter(i -> minQty == null || i.onHandQty() >= minQty)
                .filter(i -> maxQty == null || i.onHandQty() <= maxQty)
                .toList();

        // Sorting whitelist
        Comparator<OnHandSummary> cmp = Comparator
                .comparing((OnHandSummary s) -> s.part().name() == null ? "" : s.part().name(),
                        String::compareToIgnoreCase)
                .thenComparing((OnHandSummary s) -> s.part().code() == null ? "" : s.part().code(),
                        String::compareToIgnoreCase);
        if (sort != null && !sort.isEmpty()) {
            cmp = buildComparator(sort, cmp);
        }
        var sorted = filtered.stream().sorted(cmp).toList();

        long total = sorted.size();
        int from = Math.min(page * size, (int) total);
        int to = Math.min(from + size, (int) total);
        return PageEnvelope.of(sorted.subList(from, Math.max(from, to)), page, size, total);
    }

    private Comparator<OnHandSummary> buildComparator(List<String> sort, Comparator<OnHandSummary> defaultCmp) {
        Comparator<OnHandSummary> cmp = null;
        for (String s : sort) {
            if (s == null || s.isBlank()) {
                continue;
            }
            var parts = s.split(",");
            String field = parts[0].trim();
            String dir = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "asc";
            Comparator<OnHandSummary> c;
            switch (field) {
                case "partName" -> c = Comparator.comparing(i -> i.part().name() == null ? "" : i.part().name(),
                        String::compareToIgnoreCase);
                case "partCode" -> c = Comparator.comparing(i -> i.part().code() == null ? "" : i.part().code(),
                        String::compareToIgnoreCase);
                case "onHandQty" -> c = Comparator.comparingInt(OnHandSummary::onHandQty);
                case "lastUpdatedAt" ->
                        c = Comparator.comparing(i -> i.lastUpdatedAt() == null ? "" : i.lastUpdatedAt());
                default -> throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
            }
            if ("desc".equals(dir)) {
                c = c.reversed();
            }
            cmp = (cmp == null) ? c : cmp.thenComparing(c);
        }
        return cmp == null ? defaultCmp : cmp;
    }

    @Override
    @Transactional
    public void increase(String warehouseCode, Long partId, int qty) {
        if (qty <= 0 || partId == null) {
            return;
        }
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var entity = repo.findByWarehouseCodeAndPartId(warehouseCode, partId)
                .orElseGet(() -> InventoryOnHandEntity.builder()
                        .warehouseCode(warehouseCode)
                        .partId(partId)
                        .onHandQty(0)
                        .lastUpdatedAt(now)
                        .build());
        entity.increase(qty, now);
        repo.save(entity);
    }

    @Override
    @Transactional
    public void decrease(String warehouseCode, Long partId, int qty) {
        if (qty <= 0 || partId == null) {
            return;
        }
        var entity = repo.findByWarehouseCodeAndPartId(warehouseCode, partId)
                .orElseGet(() -> InventoryOnHandEntity.builder()
                        .warehouseCode(warehouseCode)
                        .partId(partId)
                        .onHandQty(0)
                        .lastUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build());
        int current = entity.getOnHandQty() == null ? 0 : entity.getOnHandQty();
        if (current < qty) {
            throw new ConflictException(ErrorStatus.CONFLICT_INVENTORY_INSUFFICIENT);
        }
        entity.decrease(qty, OffsetDateTime.now(ZoneOffset.UTC));
        repo.save(entity);
    }

    private boolean containsIgnoreCase(String text, String kw) {
        if (text == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(kw.toLowerCase(Locale.ROOT));
    }
}
