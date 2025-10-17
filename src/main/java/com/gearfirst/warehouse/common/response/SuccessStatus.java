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
    SEND_RECEIVING_NOTE_LIST_SUCCESS(HttpStatus.OK, "입고요청서 목록 조회 성공"),
    SEND_RECEIVING_NOTE_DETAIL_SUCCESS(HttpStatus.OK, "입고요청서 상세 조회 성공"),
    SED_RECEIVING_COMPLETE_SUCCESS(HttpStatus.OK, "입고 완료 처리 성공"),
    SEND_RECEIVING_NOTE_LINE_UPDATE_SUCCESS(HttpStatus.OK, "입고요청서 항목 수정 성공"),
    /** 201 CREATED */

    /** 202 ACCEPTED */

    /** 204 NO_CONTENT */

    /** 205 RESET_CONTENT */

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
