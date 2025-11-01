package com.gearfirst.warehouse.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonApiResponse<T> {
    private final int status;
    private final boolean success;
    private final String message;
    private T data;

    /**
     * SUCCESS
     */
    public static <T> ResponseEntity<CommonApiResponse<T>> success(SuccessStatus status, T data) {
        CommonApiResponse<T> response = CommonApiResponse.<T>builder()
                .status(status.getStatusCode())
                .success(true)
                .message(status.getMessage())
                .data(data)
                .build();
        return ResponseEntity.status(status.getStatusCode()).body(response);
    }

    /**
     * SUCCESS ONLY
     */
    public static ResponseEntity<CommonApiResponse<Void>> success_only(SuccessStatus status) {
        CommonApiResponse<Void> response = CommonApiResponse.<Void>builder()
                .status(status.getStatusCode())
                .success(true)
                .message(status.getMessage())
                .build();
        return ResponseEntity.status(status.getStatusCode()).body(response);
    }

    /**
     * FAIL
     */
    public static CommonApiResponse<Void> fail(int status, String message) {
        return CommonApiResponse.<Void>builder()
                .status(status)
                .success(false)
                .message(message)
                .build();
    }

    public static CommonApiResponse<Void> fail(ErrorStatus status) {
        return CommonApiResponse.<Void>builder()
                .status(status.getStatusCode())
                .success(false)
                .message(status.getMessage())
                .build();
    }

    /**
     * FAIL ONLY
     */
    public static CommonApiResponse<Void> fail_only(ErrorStatus status) {
        return CommonApiResponse.<Void>builder()
                .status(status.getStatusCode())
                .success(false)
                .message(status.getMessage())
                .build();
    }
}
