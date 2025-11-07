package com.gearfirst.warehouse.api.shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Completion handler info for Shipping complete API.
 */
@Builder
public record ShippingCompleteRequest(
        @Schema(description = "담당자 이름", example = "김담당")
        @NotBlank String assigneeName,
        @Schema(description = "담당 부서", example = "물류팀")
        @NotBlank String assigneeDept,
        @Schema(description = "담당자 연락처", example = "010-9876-5432")
        @NotBlank String assigneePhone,
        @Schema(description = "(백업) 외부 연동용 주문 ID — 임시/옵셔널", example = "123456")
        Long orderId
) {}
