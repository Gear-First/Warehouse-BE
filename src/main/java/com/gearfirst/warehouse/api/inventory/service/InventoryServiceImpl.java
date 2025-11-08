package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.PartRef;
import com.gearfirst.warehouse.api.inventory.persistence.InventoryOnHandJpaRepository;
import com.gearfirst.warehouse.api.inventory.persistence.entity.InventoryOnHandEntity;
import com.gearfirst.warehouse.api.parts.persistence.PartJpaRepository;
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
    private final PartJpaRepository parts;

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
            var updatedAt = com.gearfirst.warehouse.common.util.DateTimes.toKstString(e.getLastUpdatedAt());
            int onHand = e.getOnHandQty() == null ? 0 : e.getOnHandQty();
            int safety = pe != null && pe.getSafetyStockQty() != null ? pe.getSafetyStockQty() : 0;
            boolean low = onHand < safety;
            String supplier = e.getSupplierName();
            Integer price = (pe != null ? pe.getPrice() : null);
            Integer priceTotal = (price == null ? null : Integer.valueOf(price.intValue() * onHand));
            items.add(new OnHandSummary(e.getWarehouseCode(), partRef, onHand, updatedAt, low, safety, supplier, price, priceTotal));
        }
        // Filters: partKeyword (code|name), supplierName (entity snapshot), qty range
        var filtered = items.stream()
                .filter(i -> partKeyword == null || partKeyword.isBlank()
                        || containsIgnoreCase(i.part().code(), partKeyword)
                        || containsIgnoreCase(i.part().name(), partKeyword))
                .filter(i -> supplierName == null || supplierName.isBlank() || containsIgnoreCase(i.supplierName(), supplierName))
                .filter(i -> minQty == null || i.onHandQty() >= minQty)
                .filter(i -> maxQty == null || i.onHandQty() <= maxQty)
                .toList();

        // Sorting whitelist (legacy default: partName, partCode)
        Comparator<OnHandSummary> cmp = Comparator
                .comparing((OnHandSummary s) -> s.part().name() == null ? "" : s.part().name(), String::compareToIgnoreCase)
                .thenComparing((OnHandSummary s) -> s.part().code() == null ? "" : s.part().code(), String::compareToIgnoreCase);
        if (sort != null && !sort.isEmpty()) {
            cmp = buildComparator(sort, cmp);
        }
        var sorted = filtered.stream().sorted(cmp).toList();

        long total = sorted.size();
        int from = Math.min(page * size, (int) total);
        int to = Math.min(from + size, (int) total);
        return PageEnvelope.of(sorted.subList(from, Math.max(from, to)), page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageEnvelope<OnHandSummary> listOnHandAdvanced(
            String q,
            Long partId,
            String partCode,
            String partName,
            String warehouseCode,
            String supplierName,
            Integer minQty,
            Integer maxQty,
            int page,
            int size,
            List<String> sort
    ) {
        if (page < 0 || size < 1 || size > 200) {
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
            String code0 = pe != null ? pe.getCode() : ("P-" + e.getPartId());
            String name0 = pe != null ? pe.getName() : null;
            var partRef = new PartRef(e.getPartId(), code0, name0);
            var updatedAt = com.gearfirst.warehouse.common.util.DateTimes.toKstString(e.getLastUpdatedAt());
            int onHand = e.getOnHandQty() == null ? 0 : e.getOnHandQty();
            int safety = pe != null && pe.getSafetyStockQty() != null ? pe.getSafetyStockQty() : 0;
            boolean low = onHand < safety;
            String supplier = e.getSupplierName();
            Integer price = (pe != null ? pe.getPrice() : null);
            Integer priceTotal = (price == null ? null : Integer.valueOf(price.intValue() * onHand));
            items.add(new OnHandSummary(e.getWarehouseCode(), partRef, onHand, updatedAt, low, safety, supplier, price, priceTotal));
        }

        String qn = q == null ? null : q.trim();
        var filtered = items.stream()
                .filter(i -> qn == null || qn.isBlank() ||
                        containsIgnoreCase(i.part().code(), qn) ||
                        containsIgnoreCase(i.part().name(), qn) ||
                        containsIgnoreCase(i.supplierName(), qn) ||
                        containsIgnoreCase(i.warehouseCode(), qn))
                .filter(i -> partId == null || java.util.Objects.equals(i.part().id(), partId))
                .filter(i -> partCode == null || partCode.isBlank() || containsIgnoreCase(i.part().code(), partCode))
                .filter(i -> partName == null || partName.isBlank() || containsIgnoreCase(i.part().name(), partName))
                .filter(i -> warehouseCode == null || warehouseCode.isBlank() || java.util.Objects.equals(i.warehouseCode(), warehouseCode))
                .filter(i -> supplierName == null || supplierName.isBlank() || containsIgnoreCase(i.supplierName(), supplierName))
                .filter(i -> minQty == null || i.onHandQty() >= minQty)
                .filter(i -> maxQty == null || i.onHandQty() <= maxQty)
                .toList();

        // Sorting: default updatedAt desc
        Comparator<OnHandSummary> cmp = Comparator.comparing(i -> i.updatedAt() == null ? "" : i.updatedAt());
        // reverse for desc
        cmp = cmp.reversed();
        if (sort != null && !sort.isEmpty()) {
            cmp = buildComparator(sort, null);
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
                case "warehouseCode" -> c = Comparator.comparing(i -> i.warehouseCode() == null ? "" : i.warehouseCode(),
                        String::compareToIgnoreCase);
                case "supplierName" -> c = Comparator.comparing(i -> i.supplierName() == null ? "" : i.supplierName(),
                        String::compareToIgnoreCase);
                case "lastUpdatedAt" ->
                        c = Comparator.comparing(i -> i.updatedAt() == null ? "" : i.updatedAt());
                case "updatedAt" ->
                        c = Comparator.comparing(i -> i.updatedAt() == null ? "" : i.updatedAt());
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
        // Backward-compatible call without supplier attribution
        increase(warehouseCode, partId, qty, null);
    }

    @Override
    @Transactional
    public void increase(String warehouseCode, Long partId, int qty, String supplierName) {
        if (qty <= 0 || partId == null) {
            return;
        }
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var entity = repo.findByWarehouseCodeAndPartId(warehouseCode, partId)
                .orElseGet(() -> InventoryOnHandEntity.builder()
                        .warehouseCode(warehouseCode)
                        .partId(partId)
                        .onHandQty(0)
                        .supplierName(supplierName)
                        .lastUpdatedAt(now)
                        .build());
        // Update supplier snapshot if provided (non-blank)
        if (supplierName != null && !supplierName.isBlank()) {
            entity.setSupplierName(supplierName);
        }
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
