package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCreateNoteRequest;
import java.util.List;

public interface ShippingService {
    List<ShippingNoteSummaryResponse> getNotDone(String date);
    List<ShippingNoteSummaryResponse> getDone(String date);

    // Overloads with optional warehouse filter (use warehouseCode)
    default List<ShippingNoteSummaryResponse> getNotDone(String date, String warehouseCode) {
        var list = getNotDone(date);
        if (warehouseCode == null || warehouseCode.isBlank()) return list;
        return list; // impl override will handle filtering where available
    }
    default List<ShippingNoteSummaryResponse> getDone(String date, String warehouseCode) {
        var list = getDone(date);
        if (warehouseCode == null || warehouseCode.isBlank()) return list;
        return list; // impl override will handle
    }

    ShippingNoteDetailResponse getDetail(Long noteId);
    ShippingNoteDetailResponse updateLine(Long noteId, Long lineId, ShippingUpdateLineRequest request);
    ShippingCompleteResponse complete(Long noteId);

    // Create new shipping note (stub for now)
    ShippingNoteDetailResponse create(ShippingCreateNoteRequest request);
}
