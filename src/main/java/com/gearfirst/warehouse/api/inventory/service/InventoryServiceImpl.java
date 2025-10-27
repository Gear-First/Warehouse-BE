package com.gearfirst.warehouse.api.inventory.service;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.PartRef;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Override
    public PageEnvelope<OnHandSummary> listOnHand(Long warehouseId, String keyword, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        // Stub data to keep implementation minimal and safe
        List<OnHandSummary> all = seed();

        // filtering
        var filtered = all.stream()
                .filter(i -> warehouseId == null || i.warehouseId().equals(warehouseId))
                .filter(i -> keyword == null || keyword.isBlank() || containsIgnoreCase(i.part().code(), keyword) || containsIgnoreCase(i.part().name(), keyword))
                .collect(Collectors.toList());

        long total = filtered.size();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        var items = filtered.subList(from, to);
        return PageEnvelope.of(items, page, size, total);
    }

    private boolean containsIgnoreCase(String text, String kw) {
        if (text == null) return false;
        return text.toLowerCase(Locale.ROOT).contains(kw.toLowerCase(Locale.ROOT));
    }

    private List<OnHandSummary> seed() {
        var now = OffsetDateTime.now(ZoneOffset.UTC).toString();
        var list = new ArrayList<OnHandSummary>();
        list.add(new OnHandSummary(1L, new PartRef(1001L, "P-1001", "오일필터"), 128, now));
        list.add(new OnHandSummary(1L, new PartRef(1002L, "P-1002", "에어필터"), 64, now));
        list.add(new OnHandSummary(2L, new PartRef(2001L, "P-2001", "스페이서"), 500, now));
        return list;
    }
}
