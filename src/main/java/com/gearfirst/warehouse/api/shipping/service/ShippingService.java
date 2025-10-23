package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.shipping.dto.*;

import java.util.List;

public interface ShippingService {
    List<ShippingNoteSummaryResponse> getNotDone(String date);
    List<ShippingNoteSummaryResponse> getDone(String date);
    ShippingNoteDetailResponse getDetail(Long noteId);
    ShippingNoteDetailResponse updateLine(Long noteId, Long lineId, UpdateLineRequest request);
    ShippingCompleteResponse complete(Long noteId);
}
