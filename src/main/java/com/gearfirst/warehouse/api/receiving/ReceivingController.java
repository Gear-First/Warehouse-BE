package com.gearfirst.warehouse.api.receiving;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.UpdateLineRequest;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
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
@RequestMapping("/api/v1/receiving")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Receiving", description = "입고 API: 서버가 ACCEPTED/REJECTED를 도출하고 완료 시 COMPLETED_OK/ISSUE")
public class ReceivingController {

    private final ReceivingService service;

    @Operation(summary = "입고 예정 리스트 조회", description = "입고 예정된 내역 리스트를 조회합니다. (예정)날짜 필터링이 가능합니다.")
    @GetMapping("/not-done")
    public ResponseEntity<ApiResponse<List<ReceivingNoteSummaryResponse>>> getPendingNotes(@RequestParam(required = false) String date) {
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, service.getNotDone(date));
    }

    @Operation(summary = "입고 완료 리스트 조회", description = "입고 완료된 내역 리스트를 조회합니다. (예정)날짜 필터링이 가능합니다.")
    @GetMapping("/done")
    public ResponseEntity<ApiResponse<List<ReceivingNoteSummaryResponse>>> getCompletedNotes(@RequestParam(required = false) String date) {
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, service.getDone(date));
    }

    @Operation(summary = "입고 내역서 상세 조회", description = "내역서 ID를 통해 입고 내역서 상세 정보를 조회합니다.")
    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponse<ReceivingNoteDetailResponse>> getDetailNoteById(@PathVariable Long noteId) {
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_DETAIL_SUCCESS, service.getDetail(noteId));
    }

    @Operation(summary = "입고 항목 업데이트", description = "입고 내역서의 특정 항목에 대해 검사 수량과 이슈 여부를 업데이트합니다. 라인 상태는 서버가 도출합니다.")
    @PatchMapping("/{noteId}/lines/{lineId}")
    public ResponseEntity<ApiResponse<ReceivingNoteDetailResponse>> updateLine(
            @PathVariable Long noteId,
            @PathVariable Long lineId,
            @RequestBody @Valid UpdateLineRequest req
    ) {
        var updated = service.updateLine(noteId, lineId, req);
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LINE_UPDATE_SUCCESS, updated);
    }

    @PostMapping("/{noteId}:complete")
    public ResponseEntity<ApiResponse<ReceivingCompleteResponse>> complete(@PathVariable Long noteId) {
        var resp = service.complete(noteId);
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_COMPLETE_SUCCESS, resp);
    }
}
