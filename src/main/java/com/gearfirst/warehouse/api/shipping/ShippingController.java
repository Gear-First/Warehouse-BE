package com.gearfirst.warehouse.api.shipping;

import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Shipping", description = "출고 API: 서버가 상태를 도출하며 SHORTAGE 시 DELAYED 전이")
public class ShippingController {

    private final ShippingService service;

    @Operation(summary = "출고 예정 리스트 조회", description = "출고 예정된 내역 리스트를 조회합니다. (예정)날짜 필터링이 가능합니다.")
    @GetMapping("/not-done")
    public ResponseEntity<ApiResponse<List<ShippingNoteSummaryResponse>>> getPendingNotes(@RequestParam(required = false) String date,
                                                                                          @RequestParam(required = false) Long warehouseId) {
        // Note: warehouseId filter is planned; parameter is accepted for forward-compatibility
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

    @Operation(summary = "출고 항목 업데이트", description = "출고 내역서의 특정 항목에 대해 할당 수량과 집품 수량을 업데이트합니다. 상태는 서버가 도출합니다.")
    @PatchMapping("/{noteId}/lines/{lineId}")
    public ResponseEntity<ApiResponse<ShippingNoteDetailResponse>> updateLine(
            @PathVariable Long noteId,
            @PathVariable Long lineId,
            @RequestBody @Valid ShippingUpdateLineRequest req
    ) {
        var updated = service.updateLine(noteId, lineId, req);
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS, updated);
    }

    @PostMapping("/{noteId}:complete")
    public ResponseEntity<ApiResponse<ShippingCompleteResponse>> complete(@PathVariable Long noteId) {
        var resp = service.complete(noteId);
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_COMPLETE_SUCCESS, resp);
    }
}
