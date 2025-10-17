package com.gearfirst.warehouse.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {
    /** 200 SUCCESS */
    SEND_HEALTH_SUCCESS(HttpStatus.OK,"서버 상태 OK"),
    SEND_RECEIVING_NOTE_SUMMARY_SUCCESS(HttpStatus.OK, "입고요청서 요약 조회 성공"),
    /** 201 CREATED */

    /** 202 ACCEPTED */

    /** 204 NO_CONTENT */

    /** 205 RESET_CONTENT */

    /** 206 PARTIAL_CONTENT */

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
