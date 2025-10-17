package com.gearfirst.warehouse.api.receiving;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.UpdateLineRequest;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/receiving")
public class ReceivingController {

    @Operation(summary = "입고 예정 리스트 조회", description = "입고 예정된 내역 리스트를 조회합니다. (예정)날짜 필터링이 가능합니다.")
    @GetMapping("/not-done")
    public ResponseEntity<ApiResponse<List<ReceivingNoteSummaryResponse>>> getPendingNotes(@RequestParam(required = false) String date) {
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, ReceivingMockStore.findNotDone(date));
    }

    @Operation(summary = "입고 완료 리스트 조회", description = "입고 완료된 내역 리스트를 조회합니다. (예정)날짜 필터링이 가능합니다.")
    @GetMapping("/done")
    public ResponseEntity<ApiResponse<List<ReceivingNoteSummaryResponse>>> getCompletedNotes(@RequestParam(required = false) String date) {
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, ReceivingMockStore.findDone(date));
    }

    @Operation(summary = "입고 내역서 상세 조회", description = "내역서 ID를 통해 입고 내역서 상세 정보를 조회합니다.")
    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponse<ReceivingNoteDetailResponse>> getDetailNoteById(@PathVariable Long noteId) {
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_DETAIL_SUCCESS, ReceivingMockStore.getNoteDetail(noteId));
    }

    @PatchMapping("/{noteId}/lines/{lineId}")
    public ResponseEntity<ReceivingNoteDetailResponse> updateLine(
            @PathVariable Long noteId,
            @PathVariable Long lineId,
            @RequestBody @Valid UpdateLineRequest req
    ) {
        // 검증: 입고 수량이 검사 수량보다 클 수 없음
        if (req.orderedQty() > req.inspectedQty()) {
            return ResponseEntity.unprocessableEntity().build(); // 422(간략 처리)
        }
        var updated = ReceivingMockStore.updateNoteLine(noteId, lineId, req.inspectedQty(), req.orderedQty(), req.status());
        return ResponseEntity.ok(updated);
    }

}
