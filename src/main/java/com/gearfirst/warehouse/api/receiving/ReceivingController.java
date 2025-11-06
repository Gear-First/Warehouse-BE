package com.gearfirst.warehouse.api.receiving;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteRequest;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCreateNoteRequest;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingSearchCond;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingUpdateLineRequest;
import com.gearfirst.warehouse.api.receiving.service.ReceivingQueryService;
import com.gearfirst.warehouse.api.receiving.service.ReceivingService;
import com.gearfirst.warehouse.common.response.CommonApiResponse;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import com.gearfirst.warehouse.common.util.DateFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(name = "Receiving", description = "입고 API: 서버가 ACCEPTED/REJECTED를 도출하고 완료 시 COMPLETED_OK/ISSUE")
public class ReceivingController {

    private final ReceivingService service;
    private final ReceivingQueryService receivingQueryService;

    @Operation(summary = "입고 예정 리스트 조회", description = "입고 예정된 내역 리스트를 조회합니다. 날짜/창고 필터링 지원. 쿼리 파라미터: date=YYYY-MM-DD (예: 2025-10-29), warehouseCode(옵션), page(기본 0, 최소 0), size(기본 20, 1..100), sort(옵션). 기본 정렬: noteId asc. 날짜 필터는 requestedAt 기준이며, dateFrom/dateTo가 있을 경우 범위가 단일 값보다 우선합니다(경계 포함). KST(+09:00) 로컬일을 UTC 경계로 변환해 포함 범위로 처리합니다.")
    @Parameters({
            @Parameter(name = "date", description = "단일 날짜(YYYY-MM-DD, KST 로컬일) — requestedAt 기준"),
            @Parameter(name = "dateFrom", description = "시작일(YYYY-MM-DD, KST 로컬일) — 범위가 단일보다 우선"),
            @Parameter(name = "dateTo", description = "종료일(YYYY-MM-DD, KST 로컬일) — 경계 포함"),
            @Parameter(name = "warehouseCode", description = "창고 코드(예: 서울)"),
            @Parameter(name = "page", description = "페이지(기본 0, 최소 0)"),
            @Parameter(name = "size", description = "페이지 크기(기본 20, 1..100)"),
            @Parameter(name = "sort", description = "정렬 필드(예: noteId,asc)")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입고 예정 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/not-done")
    public ResponseEntity<CommonApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>> getPendingNotes(
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
        // Normalize dates via DateFilter (range wins; swap when from>to)
        DateFilter.Normalized nf = DateFilter.normalize(date, dateFrom, dateTo);
        List<ReceivingNoteSummaryResponse> list;
        if (!nf.hasRange() && (warehouseCode == null || warehouseCode.isBlank())) {
            list = service.getNotDone(date);
        } else {
            String dateArg = nf.hasRange() ? null : date;
            list = service.getNotDone(dateArg, nf.from(), nf.to(), warehouseCode);
        }
        long total = list.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        var envelope = PageEnvelope.of(list.subList(from, to), p, s, total);
        return CommonApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "입고 완료 리스트 조회", description = "입고 완료된 내역 리스트를 조회합니다. 날짜/창고 필터링 지원. 쿼리 파라미터: date=YYYY-MM-DD (예: 2025-10-29), warehouseCode(옵션), page(기본 0, 최소 0), size(기본 20, 1..100), sort(옵션). 기본 정렬: noteId asc. 날짜 필터는 requestedAt 기준이며, dateFrom/dateTo가 있을 경우 범위가 단일 값보다 우선합니다(KST(+09:00) 로컬일을 UTC 경계로 변환해 포함 범위로 처리).")
    @Parameters({
            @Parameter(name = "date", description = "단일 날짜(YYYY-MM-DD, KST 로컬일) — requestedAt 기준"),
            @Parameter(name = "dateFrom", description = "시작일(YYYY-MM-DD, KST 로컬일) — 범위가 단일보다 우선"),
            @Parameter(name = "dateTo", description = "종료일(YYYY-MM-DD, KST 로컬일) — 경계 포함"),
            @Parameter(name = "warehouseCode", description = "창고 코드(예: 서울)"),
            @Parameter(name = "page", description = "페이지(기본 0, 최소 0)"),
            @Parameter(name = "size", description = "페이지 크기(기본 20, 1..100)"),
            @Parameter(name = "sort", description = "정렬 필드(예: noteId,asc)")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입고 완료 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/done")
    public ResponseEntity<CommonApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>> getCompletedNotes(
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

        // Normalize dates via DateFilter (range wins; swap when from>to)
        DateFilter.Normalized nf = DateFilter.normalize(date, dateFrom, dateTo);

        List<ReceivingNoteSummaryResponse> list;
        boolean noFilters = !nf.hasRange() && (warehouseCode == null || warehouseCode.isBlank());
        if (noFilters) {
            list = service.getDone(date);
        } else {
            String dateArg = nf.hasRange() ? null : date;
            list = service.getDone(dateArg, nf.from(), nf.to(), warehouseCode);
        }

        long total = list.size();
        int from = Math.min(p * s, (int) total);
        int to = Math.min(from + s, (int) total);
        var envelope = PageEnvelope.of(list.subList(from, to), p, s, total);
        return CommonApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, envelope);
    }

    @Operation(summary = "입고 내역서 상세 조회", description = "내역서 ID를 통해 입고 내역서 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입고 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "내역서 없음")
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<CommonApiResponse<ReceivingNoteDetailResponse>> getDetailNoteById(@PathVariable Long noteId) {
        return CommonApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_DETAIL_SUCCESS, service.getDetail(noteId));
    }

    @Operation(summary = "입고 항목 업데이트", description = "입고 내역서의 특정 항목을 업데이트합니다. 요청 바디: { inspectedQty(0..orderedQty), rejected, lineRemark? }. 라인 상태는 서버가 도출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입고 항목 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (inspectedQty 범위 등)"),
            @ApiResponse(responseCode = "404", description = "내역서/항목 없음"),
            @ApiResponse(responseCode = "409", description = "이미 완료된 전표 등 갱신 불가 상태")
    })
    @PatchMapping("/{noteId}/lines/{lineId}")
    public ResponseEntity<CommonApiResponse<ReceivingNoteDetailResponse>> updateLine(
            @PathVariable Long noteId,
            @PathVariable Long lineId,
            @RequestBody @Valid ReceivingUpdateLineRequest req
    ) {
        var updated = service.updateLine(noteId, lineId, req);
        return CommonApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LINE_UPDATE_SUCCESS, updated);
    }

    @Operation(summary = "입고 완료", description = "입고 내역서의 모든 항목이 처리되었음을 확인하고, 완료 가능 여부를 판단 및 적용합니다. 엔드포인트 전용 완료 처리입니다. 검사자 정보가 필요하며(요청 전 설정), 완료 시 ACCEPTED 라인 수량을 기준으로 재고가 증가합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "검수자 정보 입력이 필요합니다: { inspectorName, inspectorDept, inspectorPhone }"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입고 완료 처리 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (검사자 정보 누락 등)"),
            @ApiResponse(responseCode = "409", description = "완료 불가 상태(미처리 라인 존재/이미 완료)")
    })
    @PostMapping("/{noteId}:complete")
    public ResponseEntity<CommonApiResponse<ReceivingCompleteResponse>> complete(
            @PathVariable Long noteId,
            @RequestBody @Valid ReceivingCompleteRequest req
    ) {
        var resp = service.complete(noteId, req);
        return CommonApiResponse.success(SuccessStatus.SEND_RECEIVING_COMPLETE_SUCCESS, resp);
    }

    @Operation(summary = "입고 요청서 생성", description = "입고 요청서를 생성합니다. requestedAt은 필수이며 expectedReceiveDate가 비어 있으면 requestedAt+2일로 설정됩니다. receivingNo가 비어 있으면 서버가 IN-{warehouseCode}-{yyyyMMdd(UTC)}-{seq3} 형식으로 생성합니다. noteId/lineId는 DB에서 자동 생성됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입고 요청서 생성 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (요청 본문 누락/형식 오류)")
    })
    @PostMapping
    public ResponseEntity<CommonApiResponse<ReceivingNoteDetailResponse>> create(
            @RequestBody ReceivingCreateNoteRequest req
    ) {
        var created = service.create(req);
        return CommonApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_DETAIL_SUCCESS, created);
    }

    @Operation(summary = "입고 통합 리스트 조회", description = "상태 파라미터로 not-done|done|all을 선택하여 조회합니다. 날짜/창고 필터링 지원. 기본 정렬: noteId asc (phase-1: date/dateFrom/dateTo는 requestedAt에 적용). 날짜 필터는 KST(+09:00) 로컬일을 UTC 경계로 변환해 포함 범위로 처리하며, 범위(dateFrom/dateTo)가 단일(date)보다 우선합니다.")
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
            @ApiResponse(responseCode = "200", description = "입고 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/notes")
    public ResponseEntity<CommonApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>> getNotes(
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

        // Normalize dates via DateFilter (range wins; swap when from>to)
        DateFilter.Normalized nf = DateFilter.normalize(date, dateFrom, dateTo);
        String statusNormalized = (status == null ? "not-done" : status.toLowerCase(Locale.ROOT));

        // Build condition (range wins: when range exists, ignore single date)
        ReceivingSearchCond cond = ReceivingSearchCond.builder()
                .status(statusNormalized)
                .date(nf.hasRange() ? null : date)
                .dateFrom(nf.from())
                .dateTo(nf.to())
                .warehouseCode(warehouseCode)
                .receivingNo(null)
                .supplierName(null)
                .build();

        Pageable pageable = PageRequest.of(p, s, parseSort(sort));
        var envelope = receivingQueryService.search(cond, pageable);
        return CommonApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_LIST_SUCCESS, envelope);
    }

    private Sort parseSort(java.util.List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }
        try {
            java.util.List<Sort.Order> orders = sortParams.stream()
                    .map(s -> {
                        String[] arr = s.split(",");
                        String prop = arr[0].trim();
                        String dir = arr.length > 1 ? arr[1].trim().toLowerCase(java.util.Locale.ROOT) : "asc";
                        Sort.Order o = "desc".equals(dir)
                                ? Sort.Order.desc(prop)
                                : Sort.Order.asc(prop);
                        return o.ignoreCase();
                    })
                    .toList();
            return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        } catch (Exception e) {
            return Sort.unsorted();
        }
    }
}
