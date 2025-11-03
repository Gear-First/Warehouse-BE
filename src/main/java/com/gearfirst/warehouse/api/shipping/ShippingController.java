package com.gearfirst.warehouse.api.shipping;

import static java.util.Comparator.comparing;

import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCreateNoteRequest;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.service.ShippingService;
import com.gearfirst.warehouse.common.response.CommonApiResponse;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
@Tag(name = "Shipping", description = "출고 API: 서버가 상태를 도출하며 SHORTAGE 시 DELAYED 전이")
public class ShippingController {

    private final ShippingService service;

    @Operation(summary = "출고 예정 리스트 조회", description = "출고 예정된 내역 리스트를 조회합니다. 날짜/창고 필터링 지원. 쿼리 파라미터: date=YYYY-MM-DD (예: 2025-10-29), warehouseCode(옵션), page(기본 0, 최소 0), size(기본 20, 1..100), sort(옵션). 기본 정렬: noteId asc. 날짜 필터는 requestedAt 기준이며 KST(+09:00) 로컬일을 UTC 경계로 변환해 포함 범위로 처리합니다. 통합 엔드포인트(/notes) 사용을 권장합니다.")
    @Parameters({
            @Parameter(name = "date", description = "단일 날짜(YYYY-MM-DD) — requestedAt 기준"),
            @Parameter(name = "warehouseCode", description = "창고 코드(예: 서울)"),
            @Parameter(name = "page", description = "페이지(기본 0, 최소 0)"),
            @Parameter(name = "size", description = "페이지 크기(기본 20, 1..100)"),
            @Parameter(name = "sort", description = "정렬 필드(예: noteId,asc)")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출고 예정 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/not-done")
    public ResponseEntity<CommonApiResponse<PageEnvelope<ShippingNoteSummaryResponse>>> getPendingNotes(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));

        var all = (warehouseCode == null)
                ? service.getNotDone(date)
                : service.getNotDone(date, warehouseCode);
        var sorted = all.stream()
                .sorted(Comparator.comparing(ShippingNoteSummaryResponse::noteId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        long total = sorted.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        var envelope = PageEnvelope.of(sorted.subList(from, to), p, s, total);
        return CommonApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "출고 완료/지연 리스트 조회", description = "출고 완료 또는 지연된 내역 리스트를 조회합니다. 날짜/창고 필터링 지원. 기본 정렬: noteId asc. 날짜 필터는 requestedAt 기준이며 UTC, 경계 포함.")
    @Parameters({
            @Parameter(name = "date", description = "단일 날짜(YYYY-MM-DD) — requestedAt 기준"),
            @Parameter(name = "warehouseCode", description = "창고 코드(예: 서울)"),
            @Parameter(name = "page", description = "페이지(기본 0, 최소 0)"),
            @Parameter(name = "size", description = "페이지 크기(기본 20, 1..100)"),
            @Parameter(name = "sort", description = "정렬 필드(예: noteId,asc)")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출고 완료/지연 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/done")
    public ResponseEntity<CommonApiResponse<PageEnvelope<ShippingNoteSummaryResponse>>> getCompletedNotes(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));

        var all = (warehouseCode == null)
                ? service.getDone(date)
                : service.getDone(date, warehouseCode);
        var sorted = all.stream()
                .sorted(Comparator.comparing(ShippingNoteSummaryResponse::noteId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        long total = sorted.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        var envelope = PageEnvelope.of(sorted.subList(from, to), p, s, total);
        return CommonApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "출고 내역서 상세 조회", description = "내역서 ID를 통해 출고 내역서 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출고 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "내역서 없음")
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<CommonApiResponse<ShippingNoteDetailResponse>> getDetailNoteById(@PathVariable Long noteId) {
        return CommonApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_DETAIL_SUCCESS, service.getDetail(noteId));
    }

    @Operation(summary = "출고 항목 업데이트", description = "출고 내역서의 특정 항목을 업데이트합니다. 요청 바디: { pickedQty(0..orderedQty), lineRemark? }. 서버는 on-hand를 기반으로 PENDING/READY/SHORTAGE를 도출하며, SHORTAGE 존재 시 전표를 DELAYED로 전환하고 해당 시점에 completedAt을 기록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출고 항목 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (pickedQty 범위 등)"),
            @ApiResponse(responseCode = "404", description = "내역서/항목 없음"),
            @ApiResponse(responseCode = "409", description = "이미 최종 상태(DELAYED/COMPLETED)로 갱신 불가(DELAYED 재시작 로직 추가 예정)")
    })
    @PatchMapping("/{noteId}/lines/{lineId}")
    public ResponseEntity<CommonApiResponse<ShippingNoteDetailResponse>> updateLine(
            @PathVariable Long noteId,
            @PathVariable Long lineId,
            @RequestBody @Valid ShippingUpdateLineRequest req
    ) {
        var updated = service.updateLine(noteId, lineId, req);
        return CommonApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS, updated);
    }

    @Operation(summary = "출고 완료 처리", description = "출고 내역서의 모든 항목이 처리되었음을 확인하고, 완료 상태(DELAYED/COMPLETED)로 전환합니다. 엔드포인트 전용 완료 처리입니다. 담당자 정보가 필요하며(요청 전 설정), READY 라인의 pickedQty 합계를 기준으로 재고가 감소합니다. SHORTAGE가 존재하면 전표는 DELAYED로 전환되며 재고 감소는 수행되지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출고 완료 처리 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (담당자 정보 누락 등)"),
            @ApiResponse(responseCode = "409", description = "완료 불가 상태(혼합 상태/이미 최종)")
    })
    @PostMapping("/{noteId}:complete")
    public ResponseEntity<CommonApiResponse<ShippingCompleteResponse>> complete(@PathVariable Long noteId) {
        var resp = service.complete(noteId);
        return CommonApiResponse.success(SuccessStatus.SEND_SHIPPING_COMPLETE_SUCCESS, resp);
    }

    @Operation(summary = "출고 요청서 생성", description = "출고 요청서를 생성합니다. requestedAt은 필수이며 expectedShipDate가 비어 있으면 requestedAt+2일로 설정됩니다. shippingNo가 비어 있으면 서버가 OUT-{warehouseCode}-{yyyyMMdd(UTC)}-{seq3} 형식으로 생성합니다. noteId/lineId는 DB에서 자동 생성됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출고 요청서 생성 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (요청 본문 누락/형식 오류)")
    })
    @PostMapping
    public ResponseEntity<CommonApiResponse<ShippingNoteDetailResponse>> create(
            @RequestBody ShippingCreateNoteRequest req
    ) {
        var created = service.create(req);
        return CommonApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_DETAIL_SUCCESS, created);
    }

    @Operation(summary = "출고 통합 리스트 조회", description = "상태 파라미터로 not-done|done|all을 선택하여 조회합니다. 날짜/창고 필터링 지원. 기본 정렬: noteId asc (phase-1: date/dateFrom/dateTo는 requestedAt에 적용). 날짜 필터는 UTC 기준이며 범위(dateFrom/dateTo)가 단일(date)보다 우선하고 경계를 포함합니다.")
    @Parameters({
            @Parameter(name = "status", description = "조회 상태 (not-done|done|all). 기본값 not-done"),
            @Parameter(name = "date", description = "단일 날짜(YYYY-MM-DD) — requestedAt 기준"),
            @Parameter(name = "dateFrom", description = "시작일(YYYY-MM-DD) — requestedAt 기준"),
            @Parameter(name = "dateTo", description = "종료일(YYYY-MM-DD) — requestedAt 기준"),
            @Parameter(name = "warehouseCode", description = "창고 코드(예: 서울)"),
            @Parameter(name = "page", description = "페이지(기본 0, 최소 0)"),
            @Parameter(name = "size", description = "페이지 크기(기본 20, 1..100)"),
            @Parameter(name = "sort", description = "정렬 필드(예: noteId,asc)")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출고 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/notes")
    public ResponseEntity<CommonApiResponse<PageEnvelope<ShippingNoteSummaryResponse>>> getNotes(
            @RequestParam(defaultValue = "not-done") String status,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(size, 100));
        List<ShippingNoteSummaryResponse> list;
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
        LocalDate from =
                (dateFrom == null || dateFrom.isBlank()) ? null : LocalDate.parse(dateFrom);
        LocalDate to = (dateTo == null || dateTo.isBlank()) ? null : LocalDate.parse(dateTo);
        if (from != null || to != null || (date != null && !date.isBlank())) {
            if (from == null && to == null && date != null && !date.isBlank()) {
                var d = LocalDate.parse(date);
                from = d;
                to = d;
            }
            final LocalDate fFrom = from;
            final LocalDate fTo = to;
            list = list.stream().filter(it -> {
                String ra = it.requestedAt();
                if (ra == null || ra.isBlank()) {
                    return false;
                }
                LocalDate d;
                try {
                    d = (ra.length() > 10) ? OffsetDateTime.parse(ra).toLocalDate()
                            : LocalDate.parse(ra);
                } catch (Exception e) {
                    return false;
                }
                if (fFrom != null && d.isBefore(fFrom)) {
                    return false;
                }
                return fTo == null || !d.isAfter(fTo);
            }).toList();
        }
        var sorted = list.stream()
                .sorted(Comparator.comparing(ShippingNoteSummaryResponse::noteId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        long total = sorted.size();
        int fromIdx = Math.min(p * s, (int) total);
        int toIdx = Math.min(fromIdx + s, (int) total);
        var envelope = PageEnvelope.of(sorted.subList(fromIdx, toIdx), p, s, total);
        return CommonApiResponse.success(SuccessStatus.SEND_SHIPPING_NOTE_LIST_SUCCESS, envelope);
    }
}
