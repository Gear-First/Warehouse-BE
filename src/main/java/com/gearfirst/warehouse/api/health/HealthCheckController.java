package com.gearfirst.warehouse.api.health;


import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.response.CommonApiResponse;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "HealthCheck", description = "HealthCheck 관련 API 입니다.")
@RequestMapping({"/api/v1/health"})
public class HealthCheckController {

    // 응답 시 데이터 반환 없이 응답코드, 응답 메세지만 보낼때
    @Operation(summary = "헬스 체크", description = "서버의 정상 동작 여부를 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정상 응답")
    })
    @GetMapping("")
    public ResponseEntity<CommonApiResponse<Void>> healthCheck() {
        return CommonApiResponse.success_only(SuccessStatus.SEND_HEALTH_SUCCESS);
    }

    // 응답 시 데이터 반환 과 함께 응답코드, 응답 메세지를 보낼때
    @Operation(summary = "헬스 체크(데이터 포함)", description = "서버의 정상 동작 여부와 데이터를 함께 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정상 응답")
    })
    @GetMapping("/check-data")
    public ResponseEntity<CommonApiResponse<String>> healthCheckData() {
        return CommonApiResponse.success(SuccessStatus.SEND_HEALTH_SUCCESS, "OK");
    }


    // 예외처리 예제
    @Operation(summary = "헬스 체크 예외 테스트", description = "예외 상황을 테스트합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정상 응답"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "예외 발생")
    })
    @GetMapping("/exception-test/{data}")
    public ResponseEntity<CommonApiResponse<Void>> healthCheckData(@PathVariable("data") String data) {

        if (data.equals("run")) {
            // 커스텀 예외처리(BadRequstException) 사용방법 및 ErrorStatus 사용방법
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getMessage());
        }

        return CommonApiResponse.success_only(SuccessStatus.SEND_HEALTH_SUCCESS);
    }
}
