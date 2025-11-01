package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingUpdateLineRequest;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCreateNoteRequest;
import java.util.List;

public interface ReceivingService {
    List<ReceivingNoteSummaryResponse> getNotDone(String date);
    List<ReceivingNoteSummaryResponse> getDone(String date);

    // Overloads with optional warehouse filter (legacy id-based for backward compatibility)
    default List<ReceivingNoteSummaryResponse> getNotDone(String date, Long warehouseId) {
        var list = getNotDone(date);
        if (warehouseId == null) return list;
        return list; // impl override will handle id-based filtering when available
    }
    default List<ReceivingNoteSummaryResponse> getDone(String date, Long warehouseId) {
        var list = getDone(date);
        if (warehouseId == null) return list;
        return list; // impl override will handle id-based filtering when available
    }

    // Overloads with warehouseCode (string) used by controllers
    default List<ReceivingNoteSummaryResponse> getNotDone(String date, String warehouseCode) {
        // By default, ignore code and return the same list; impls may override for filtering
        return getNotDone(date);
    }
    default List<ReceivingNoteSummaryResponse> getDone(String date, String warehouseCode) {
        return getDone(date);
    }

    ReceivingNoteDetailResponse getDetail(Long noteId);
    ReceivingNoteDetailResponse updateLine(Long noteId, Long lineId, ReceivingUpdateLineRequest request);
    ReceivingCompleteResponse complete(Long noteId);

    // Create new receiving note (stub for now)
    ReceivingNoteDetailResponse create(ReceivingCreateNoteRequest request);
}
