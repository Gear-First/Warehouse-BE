package com.gearfirst.warehouse.api.summary;

import com.gearfirst.warehouse.api.dto.NoteCountsByDateResponse;
import com.gearfirst.warehouse.common.response.CommonApiResponse;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/v1/summary")
@RequiredArgsConstructor
@Tag(name = "Summary", description = "집계/요약 API")
public class SummaryController {

    private final SummaryService summaryService;

    @Operation(
        summary = "요청일 기준 입/출고 전표 건수 조회",
        description = "단일 요청일(YYYY-MM-DD, KST 로컬일)을 기준으로 입고/출고 전표(all 상태 포함) 건수를 반환합니다. requestedAt 기준이며 KST 하루를 UTC 경계로 변환하여 계산합니다."
    )
    @Parameter(name = "requestDate", description = "단일 날짜(YYYY-MM-DD, KST 로컬일)", required = true)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "집계 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/note-counts")
    public ResponseEntity<CommonApiResponse<NoteCountsByDateResponse>> countNotesByDate(
        @RequestParam String requestDate
    ) {
        NoteCountsByDateResponse dto = summaryService.countNotesByDate(requestDate);
        return CommonApiResponse.success(SuccessStatus.SEND_NOTE_COUNTS_SUCCESS, dto);
    }
}
