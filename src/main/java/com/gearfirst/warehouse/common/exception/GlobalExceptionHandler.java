package com.gearfirst.warehouse.common.exception;

import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<?>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        var status = ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION;
        return ResponseEntity.status(status.getHttpStatus())
                .body(ApiResponse.fail(status));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(Exception ex) {
        var status = ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION;
        return ResponseEntity.status(status.getHttpStatus())
                .body(ApiResponse.fail(status));
    }
}
