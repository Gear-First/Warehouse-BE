package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.UpdateLineRequest;
import java.util.List;

public interface ReceivingService {
    List<ReceivingNoteSummaryResponse> getNotDone(String date);
    List<ReceivingNoteSummaryResponse> getDone(String date);
    ReceivingNoteDetailResponse getDetail(Long noteId);
    ReceivingNoteDetailResponse updateLine(Long noteId, Long lineId, UpdateLineRequest request);
    ReceivingCompleteResponse complete(Long noteId);
}
