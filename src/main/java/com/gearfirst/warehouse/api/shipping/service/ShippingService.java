package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteRequest;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCreateNoteRequest;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailV2Response;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingRecalcResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingLineConfirmResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import java.util.List;

public interface ShippingService {
    List<ShippingNoteSummaryResponse> getNotDone(String date);

    List<ShippingNoteSummaryResponse> getDone(String date);

    // Overloads with optional warehouse filter (use warehouseCode)
    default List<ShippingNoteSummaryResponse> getNotDone(String date, String warehouseCode) {
        var list = getNotDone(date);
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return list;
        }
        return list; // impl override will handle filtering where available
    }

    default List<ShippingNoteSummaryResponse> getDone(String date, String warehouseCode) {
        var list = getDone(date);
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return list;
        }
        return list; // impl override will handle
    }

    // New overloads for centralized filtering (range > single) and optional warehouse/text filters
    List<ShippingNoteSummaryResponse> getNotDone(String date, String dateFrom, String dateTo, String warehouseCode);

    List<ShippingNoteSummaryResponse> getDone(String date, String dateFrom, String dateTo, String warehouseCode);

    // Extended overloads including shippingNo/branchName filters (preferred)
    default List<ShippingNoteSummaryResponse> getNotDone(String date, String dateFrom, String dateTo, String warehouseCode,
                                                         String shippingNo, String branchName) {
        // by default delegate to 4-arg overload for backward compatibility
        return getNotDone(date, dateFrom, dateTo, warehouseCode);
    }

    default List<ShippingNoteSummaryResponse> getDone(String date, String dateFrom, String dateTo, String warehouseCode,
                                                      String shippingNo, String branchName) {
        return getDone(date, dateFrom, dateTo, warehouseCode);
    }

    // Unified q overloads (shippingNo | branchName)
    default List<ShippingNoteSummaryResponse> getNotDone(String date, String dateFrom, String dateTo, String warehouseCode,
                                                         String shippingNo, String branchName, String q) {
        return getNotDone(date, dateFrom, dateTo, warehouseCode, shippingNo, branchName);
    }

    default List<ShippingNoteSummaryResponse> getDone(String date, String dateFrom, String dateTo, String warehouseCode,
                                                      String shippingNo, String branchName, String q) {
        return getDone(date, dateFrom, dateTo, warehouseCode, shippingNo, branchName);
    }

    ShippingNoteDetailResponse getDetail(Long noteId);

    ShippingNoteDetailResponse updateLine(Long noteId, Long lineId, ShippingUpdateLineRequest request);

    // Completion APIs
    ShippingCompleteResponse complete(Long noteId, ShippingCompleteRequest request);

    // Create new shipping note (stub for now)
    ShippingNoteDetailResponse create(ShippingCreateNoteRequest request);

    // ---------- V2 additions (non-breaking) ----------
    ShippingNoteDetailV2Response getDetailV2(Long noteId);

    ShippingRecalcResponse checkShippable(Long noteId, boolean apply, List<Long> lineIds);

    ShippingLineConfirmResponse confirmLine(Long noteId, Long lineId);
}
