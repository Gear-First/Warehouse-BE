package com.gearfirst.warehouse.api.receiving.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReceivingNoteSummaryResponse(
        @Schema(description = "노트 ID") Long noteId,
        @Schema(description = "입고 번호", example = "IN-서울-20251102-001") String receivingNo,
        @Schema(description = "공급자명") String supplierName,
        @Schema(description = "품목 종류 수") int itemKindsNumber,
        @Schema(description = "총 수량") int totalQty,
        @Schema(description = "상태", example = "PENDING | IN_PROGRESS | COMPLETED_OK | COMPLETED_ISSUE") String status,
        @Schema(description = "창고 코드", example = "서울") String warehouseCode,
        @Schema(description = "요청 일시(KST ISO8601)", example = "2025-11-02T09:00:00+09:00") String requestedAt,
        @Schema(description = "입고 예상일(KST ISO8601)", example = "2025-11-04T09:00:00+09:00") String expectedReceiveDate,
        @Schema(description = "완료 일시(KST ISO8601); 없으면 null", example = "2025-11-03T16:00:00+09:00") String completedAt
) {
}
