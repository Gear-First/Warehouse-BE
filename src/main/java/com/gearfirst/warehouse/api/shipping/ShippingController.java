package com.gearfirst.warehouse.api.shipping;

import static java.util.Comparator.comparing;

import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Comparator;
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

    @Operation(summary = "출고 예정 리스트 조회", description = "출고 예정된 내역 리스트를 조회합니다. 날짜/창고 필터링 지원. 쿼리 파라미터: date=YYYY-MM-DD (예: 2025-10-29), warehouseId(옵션), page(기본 0, 최소 0), size(기본 20, 1..100), sort(옵션). 기본 정렬: noteId asc")
    @GetMapping("/not-done")
    public ResponseEntity<ApiResponse<PageEnvelope<ShippingNoteSummaryResponse>>> getPendingNotes(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) java.util.List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        var all = (warehouseId == null)
                ? service.getNotDone(date)
                : service.getNotDone(date, warehouseId);
        var sorted = all.stream()
                .sorted(Comparator.comparing(ShippingNoteSummaryResponse::noteId))
                .toList();
        long total = sorted.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        var envelope = PageEnvelope.of(sorted.subList(from, to), p, s, total);
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "출고 완료/지연 리스트 조회", description = "출고 완료 또는 지연된 내역 리스트를 조회합니다. 날짜/창고 필터링 지원. 페이지네이션: page(>=0), size(1..100). 기본 정렬: noteId asc")
    @GetMapping("/done")
    public ResponseEntity<ApiResponse<PageEnvelope<ShippingNoteSummaryResponse>>> getCompletedNotes(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) java.util.List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        var all = (warehouseId == null)
                ? service.getDone(date)
                : service.getDone(date, warehouseId);
        var sorted = all.stream()
                .sorted(comparing(ShippingNoteSummaryResponse::noteId))
                .toList();
        long total = sorted.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        var envelope = PageEnvelope.of(sorted.subList(from, to), p, s, total);
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS, envelope);
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

    @Operation(summary = "출고 요청서 생성", description = "출고 요청서를 생성합니다. 현재 단계에서는 값 검증/번호 생성(shippingNo) 로직을 구현하지 않습니다. TODO 위치만 지정합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ShippingNoteDetailResponse>> create(
            @RequestBody com.gearfirst.warehouse.api.shipping.dto.ShippingCreateNoteRequest req
    ) {
        // TODO: 값 검증(필수 필드, 수량 범위) 및 shippingNo 생성 로직 추가
        var created = service.create(req);
        return ApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_DETAIL_SUCCESS, created);
    }
}
