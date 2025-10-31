package com.gearfirst.warehouse.api.receiving;

import static java.util.Comparator.comparing;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCreateNoteRequest;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingUpdateLineRequest;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Comparator;
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

    @Operation(summary = "입고 예정 리스트 조회", description = "입고 예정된 내역 리스트를 조회합니다. 날짜/창고 필터링 지원. 쿼리 파라미터: date=YYYY-MM-DD (예: 2025-10-29), warehouseCode(옵션), page(기본 0, 최소 0), size(기본 20, 1..100), sort(옵션). 기본 정렬: noteId asc")
    @GetMapping("/not-done")
    public ResponseEntity<ApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>> getPendingNotes(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) java.util.List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        var all = (warehouseCode == null || warehouseCode.isBlank())
                ? service.getNotDone(date)
                : service.getNotDone(date, warehouseCode);
        // 기본 정렬: noteId asc
        var sorted = all.stream()
                .sorted(Comparator.comparing(ReceivingNoteSummaryResponse::noteId))
                .toList();
        long total = sorted.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        var envelope = PageEnvelope.of(sorted.subList(from, to), p, s, total);
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "입고 완료 리스트 조회", description = "입고 완료된 내역 리스트를 조회합니다. 날짜/창고 필터링 지원. 쿼리 파라미터: date=YYYY-MM-DD (예: 2025-10-29), warehouseCode(옵션), page(기본 0, 최소 0), size(기본 20, 1..100), sort(옵션). 기본 정렬: noteId asc")
    @GetMapping("/done")
    public ResponseEntity<ApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>> getCompletedNotes(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) java.util.List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        var all = (warehouseCode == null || warehouseCode.isBlank())
                ? service.getDone(date)
                : service.getDone(date, warehouseCode);
        // 기본 정렬: noteId asc
        var sorted = all.stream()
                .sorted(comparing(ReceivingNoteSummaryResponse::noteId))
                .toList();
        long total = sorted.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        var envelope = PageEnvelope.of(sorted.subList(from, to), p, s, total);
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, envelope);
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
            @RequestBody @Valid ReceivingUpdateLineRequest req
    ) {
        var updated = service.updateLine(noteId, lineId, req);
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LINE_UPDATE_SUCCESS, updated);
    }

    @PostMapping("/{noteId}:complete")
    public ResponseEntity<ApiResponse<ReceivingCompleteResponse>> complete(@PathVariable Long noteId) {
        var resp = service.complete(noteId);
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_COMPLETE_SUCCESS, resp);
    }

    @Operation(summary = "입고 요청서 생성", description = "입고 요청서를 생성합니다. 현재 단계에서는 값 검증/번호 생성(LOT, receivingNo) 로직을 구현하지 않습니다. TODO 위치만 지정합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ReceivingNoteDetailResponse>> create(
            @RequestBody ReceivingCreateNoteRequest req
    ) {
        // TODO: 값 검증(필수 필드, 수량 범위), LOT 규칙 검증, receivingNo 생성 로직 추가
        var created = service.create(req);
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_DETAIL_SUCCESS, created);
    }

    @Operation(summary = "입고 통합 리스트 조회", description = "상태 파라미터로 not-done|done|all을 선택하여 조회합니다. 날짜/창고 필터링 지원. 기본 정렬: noteId asc (phase-1: date/dateFrom/dateTo는 requestedAt에 적용)")
    @GetMapping("/notes")
    public ResponseEntity<ApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>> getNotes(
            @RequestParam(defaultValue = "not-done") String status,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) java.util.List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        java.util.List<ReceivingNoteSummaryResponse> list;
        switch (status == null ? "not-done" : status.toLowerCase(java.util.Locale.ROOT)) {
            case "done" -> list = (warehouseCode == null || warehouseCode.isBlank())
                    ? service.getDone(date)
                    : service.getDone(date, warehouseCode);
            case "all" -> {
                var nd = (warehouseCode == null || warehouseCode.isBlank())
                        ? service.getNotDone(date)
                        : service.getNotDone(date, warehouseCode);
                var dn = (warehouseCode == null || warehouseCode.isBlank())
                        ? service.getDone(date)
                        : service.getDone(date, warehouseCode);
                list = new java.util.ArrayList<>(nd.size() + dn.size());
                list.addAll(nd);
                list.addAll(dn);
            }
            default -> list = (warehouseCode == null || warehouseCode.isBlank())
                    ? service.getNotDone(date)
                    : service.getNotDone(date, warehouseCode);
        }
        // Apply date range filter (requestedAt) if dateFrom/dateTo provided (range wins), else apply single date if provided
        java.time.LocalDate from = (dateFrom == null || dateFrom.isBlank()) ? null : java.time.LocalDate.parse(dateFrom);
        java.time.LocalDate to = (dateTo == null || dateTo.isBlank()) ? null : java.time.LocalDate.parse(dateTo);
        if (from != null || to != null || (date != null && !date.isBlank())) {
            if (from == null && to == null && date != null && !date.isBlank()) {
                var d = java.time.LocalDate.parse(date);
                from = d;
                to = d;
            }
            final java.time.LocalDate fFrom = from;
            final java.time.LocalDate fTo = to;
            list = list.stream().filter(it -> {
                String ra = it.requestedAt();
                if (ra == null || ra.isBlank()) return false;
                java.time.LocalDate d;
                try {
                    if (ra.length() > 10) d = java.time.OffsetDateTime.parse(ra).toLocalDate();
                    else d = java.time.LocalDate.parse(ra);
                } catch (Exception e) { return false; }
                if (fFrom != null && d.isBefore(fFrom)) return false;
                if (fTo != null && d.isAfter(fTo)) return false;
                return true;
            }).toList();
        }
        var sorted = list.stream().sorted(java.util.Comparator.comparing(ReceivingNoteSummaryResponse::noteId)).toList();
        long total = sorted.size();
        int fromIdx = Math.min(p * s, (int) total);
        int toIdx = Math.min(fromIdx + s, (int) total);
        var envelope = PageEnvelope.of(sorted.subList(fromIdx, toIdx), p, s, total);
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, envelope);
    }
}
