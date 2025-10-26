package com.gearfirst.warehouse.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {
    /** 400 BAD_REQUEST */
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."),
    RECEIVING_ORDERED_QTY_EXCEEDS_INSPECTED_QTY(HttpStatus.BAD_REQUEST, "입고 요청 수량이 검사 수량을 초과할 수 없습니다."),
    SHIPPING_PICKED_QTY_EXCEEDS_ALLOCATED_QTY(HttpStatus.BAD_REQUEST, "출고 집품 수량이 할당 수량을 초과할 수 없습니다."),



    /** 401 UNAUTHORIZED */
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    AUTH_BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "자격 증명이 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "액세스 토큰이 유효하지 않거나 손상되었습니다."),
    USER_BLACKLISTED(HttpStatus.UNAUTHORIZED, "차단된 사용자입니다. 관리자에게 문의하세요."),

    /** 404 NOT_FOUND */
    NOT_FOUND_MEMBER_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),

    /** 409 CONFLICT */
    CONFLICT_RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 자원입니다."),

    /** 500 SERVER_ERROR */
    FAILED_TO_SAVE_ENTITY(HttpStatus.INTERNAL_SERVER_ERROR, "엔티티 저장에 실패했습니다."),
    FAILED_TO_UPDATE_ENTITY(HttpStatus.INTERNAL_SERVER_ERROR, "엔티티 수정에 실패했습니다."),
    FAILED_TO_DELETE_ENTITY(HttpStatus.INTERNAL_SERVER_ERROR, "엔티티 삭제에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
