package com.gearfirst.warehouse.api.shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ShippingNoteSummaryResponse(
        @Schema(description = "노트 ID") Long noteId,
        @Schema(description = "출고 번호", example = "OUT-부산-20251103-001") String shippingNo,
        @Schema(description = "납품처/지점명", example = "ACME") String branchName,
        @Schema(description = "품목 종류 수") int itemKindsNumber,
        @Schema(description = "총 수량") int totalQty,
        @Schema(description = "상태", example = "PENDING | IN_PROGRESS | DELAYED | COMPLETED") String status,
        @Schema(description = "창고 코드", example = "부산") String warehouseCode,
        @Schema(description = "요청 일시(KST ISO8601)", example = "2025-11-03T09:00:00+09:00") String requestedAt,
        @Schema(description = "출고 예상일(KST ISO8601)", example = "2025-11-05T09:00:00+09:00") String expectedShipDate,
        @Schema(description = "완료 일시(KST ISO8601); 없으면 null", example = "2025-11-07T09:30:00+09:00") String completedAt
) {
}
