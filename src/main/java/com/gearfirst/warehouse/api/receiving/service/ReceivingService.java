package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteRequest;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCreateNoteRequest;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingUpdateLineRequest;
import java.util.List;

public interface ReceivingService {
    List<ReceivingNoteSummaryResponse> getNotDone(String date);

    List<ReceivingNoteSummaryResponse> getDone(String date);

    // Overloads with optional warehouse filter (use warehouseCode)
    default List<ReceivingNoteSummaryResponse> getNotDone(String date, String warehouseCode) {
        var list = getNotDone(date);
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return list;
        }
        return list; // impl override will handle filtering where available
    }

    default List<ReceivingNoteSummaryResponse> getDone(String date, String warehouseCode) {
        var list = getDone(date);
        if (warehouseCode == null || warehouseCode.isBlank()) return list;
        return list; // impl override will handle id-based filtering when available
    }

    // New overloads for centralized filtering (range > single) and optional warehouse filter
    List<ReceivingNoteSummaryResponse> getNotDone(String date, String dateFrom, String dateTo, String warehouseCode);

    List<ReceivingNoteSummaryResponse> getDone(String date, String dateFrom, String dateTo, String warehouseCode);

    ReceivingNoteDetailResponse getDetail(Long noteId);

    ReceivingNoteDetailResponse updateLine(Long noteId, Long lineId, ReceivingUpdateLineRequest request);

    // Completion APIs
    ReceivingCompleteResponse complete(Long noteId, ReceivingCompleteRequest request);

    // Create new receiving note (stub for now)
    ReceivingNoteDetailResponse create(ReceivingCreateNoteRequest request);
}
