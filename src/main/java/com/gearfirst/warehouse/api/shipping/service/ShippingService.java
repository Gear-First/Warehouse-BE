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

    // Overloads with optional warehouse filter (legacy id-based)
    default List<ShippingNoteSummaryResponse> getNotDone(String date, Long warehouseId) {
        var list = getNotDone(date);
        if (warehouseId == null) return list;
        return list; // impl override may handle id-based filtering
    }
    default List<ShippingNoteSummaryResponse> getDone(String date, Long warehouseId) {
        var list = getDone(date);
        if (warehouseId == null) return list;
        return list; // impl override may handle id-based filtering
    }

    // Overloads with warehouseCode (string) used by controllers
    default List<ShippingNoteSummaryResponse> getNotDone(String date, String warehouseCode) {
        return getNotDone(date);
    }
    default List<ShippingNoteSummaryResponse> getDone(String date, String warehouseCode) {
        return getDone(date);
    }

    ShippingNoteDetailResponse getDetail(Long noteId);
    ShippingNoteDetailResponse updateLine(Long noteId, Long lineId, ShippingUpdateLineRequest request);
    ShippingCompleteResponse complete(Long noteId);

    // Create new shipping note (stub for now)
    ShippingNoteDetailResponse create(ShippingCreateNoteRequest request);
}
