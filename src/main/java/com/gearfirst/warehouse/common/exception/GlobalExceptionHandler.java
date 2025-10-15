package com.gearfirst.warehouse.common.exception;

import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException ex) {
        // ErrorStatus 메시지와 상태코드에 맞게 응답
        // 메시지 매칭
        ErrorStatus status = null;
        for (ErrorStatus s : ErrorStatus.values()) {
            if (s.getMessage().equals(ex.getMessage())) {
                status = s;
                break;
            }
        }
        if (status == null) {
            status = ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION;
        }
        return ResponseEntity.status(status.getHttpStatus())
                .body(ApiResponse.fail(status.getStatusCode(), status.getMessage()));
    }
}
