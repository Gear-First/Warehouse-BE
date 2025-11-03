package com.gearfirst.warehouse.common.exception;

import com.gearfirst.warehouse.common.response.CommonApiResponse;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CommonApiResponse<?>> handleBadRequest(BadRequestException ex) {
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
                .body(CommonApiResponse.fail(status.getStatusCode(), status.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CommonApiResponse<?>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonApiResponse.fail(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<CommonApiResponse<?>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(CommonApiResponse.fail(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        var status = ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION;
        return ResponseEntity.status(status.getHttpStatus())
                .body(CommonApiResponse.fail(status));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<CommonApiResponse<?>> handleValidationExceptions(Exception ex) {
        var status = ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION;
        return ResponseEntity.status(status.getHttpStatus())
                .body(CommonApiResponse.fail(status));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<?>> handleAllExceptions(Exception ex) {
        log.error("Unexpected error occurred", ex);
        String message = "An unexpected error occurred: " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
    }
}
