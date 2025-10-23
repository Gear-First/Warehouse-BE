package com.gearfirst.warehouse.api.shipping;

import com.gearfirst.warehouse.api.shipping.dto.*;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService service;

    @Operation(summary = "출고 예정 리스트 조회", description = "출고 예정된 내역 리스트를 조회합니다. (예정)날짜 필터링이 가능합니다.")
    @GetMapping("/not-done")
    public ResponseEntity<ApiResponse<List<ShippingNoteSummaryResponse>>> getPendingNotes(@RequestParam(required = false) String date) {
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS, service.getNotDone(date));
    }

    @Operation(summary = "출고 완료/지연 리스트 조회", description = "출고 완료 또는 지연된 내역 리스트를 조회합니다. (예정)날짜 필터링이 가능합니다.")
    @GetMapping("/done")
    public ResponseEntity<ApiResponse<List<ShippingNoteSummaryResponse>>> getCompletedNotes(@RequestParam(required = false) String date) {
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS, service.getDone(date));
    }

    @Operation(summary = "출고 내역서 상세 조회", description = "내역서 ID를 통해 출고 내역서 상세 정보를 조회합니다.")
    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponse<ShippingNoteDetailResponse>> getDetailNoteById(@PathVariable Long noteId) {
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_DETAIL_SUCCESS, service.getDetail(noteId));
    }

    @Operation(summary = "출고 항목 업데이트", description = "출고 내역서의 특정 항목에 대해 할당 수량, 집품 수량, 상태를 업데이트합니다.")
    @PatchMapping("/{noteId}/lines/{lineId}")
    public ResponseEntity<ApiResponse<ShippingNoteDetailResponse>> updateLine(
            @PathVariable Long noteId,
            @PathVariable Long lineId,
            @RequestBody @Valid UpdateLineRequest req
    ) {
        var updated = service.updateLine(noteId, lineId, req);
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS, updated);
    }

    @PostMapping("/{noteId}:complete")
    public ResponseEntity<ShippingCompleteResponse> complete(@PathVariable Long noteId) {
        var resp = service.complete(noteId);
        return ResponseEntity.ok(resp);
    }
}
