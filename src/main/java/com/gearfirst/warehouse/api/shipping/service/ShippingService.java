package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import java.util.List;

public interface ShippingService {
    List<ShippingNoteSummaryResponse> getNotDone(String date);
    List<ShippingNoteSummaryResponse> getDone(String date);
    ShippingNoteDetailResponse getDetail(Long noteId);
    ShippingNoteDetailResponse updateLine(Long noteId, Long lineId, ShippingUpdateLineRequest request);
    ShippingCompleteResponse complete(Long noteId);
}
