package com.gearfirst.warehouse.api.receiving.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Completion handler info for Receiving complete API.
 */
@Builder
public record ReceivingCompleteRequest(
        @Schema(description = "검수자 이름", example = "홍길동")
        @NotBlank String inspectorName,
        @Schema(description = "검수 부서", example = "품질관리팀")
        @NotBlank String inspectorDept,
        @Schema(description = "검수자 연락처", example = "010-1234-5678")
        @NotBlank String inspectorPhone
) {}
