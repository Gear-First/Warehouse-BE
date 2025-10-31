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
    SHIPPING_ALLOCATED_QTY_EXCEEDS_ORDERED_QTY(HttpStatus.BAD_REQUEST, "출고 할당 수량이 주문 수량을 초과할 수 없습니다."),
    SHIPPING_PICKED_QTY_EXCEEDS_ORDERED_QTY(HttpStatus.BAD_REQUEST, "출고 집품 수량이 주문 수량을 초과할 수 없습니다."),
    PART_CATEGORY_NAME_INVALID(HttpStatus.BAD_REQUEST, "카테고리 이름이 유효하지 않습니다."),
    PART_CODE_INVALID(HttpStatus.BAD_REQUEST, "부품 코드가 유효하지 않습니다."),
    PART_NAME_INVALID(HttpStatus.BAD_REQUEST, "부품 이름이 유효하지 않습니다."),
    PART_PRICE_INVALID(HttpStatus.BAD_REQUEST, "부품 가격이 유효하지 않습니다."),
    RECEIVING_NO_INVALID(HttpStatus.BAD_REQUEST, "입고 번호가 유효하지 않습니다."),
    RECEIVING_REQUESTED_AT_INVALID(HttpStatus.BAD_REQUEST, "입고 요청일자가 유효하지 않습니다."),
    SHIPPING_NO_INVALID(HttpStatus.BAD_REQUEST, "출고 번호가 유효하지 않습니다."),
    SHIPPING_REQUESTED_AT_INVALID(HttpStatus.BAD_REQUEST, "출고 요청일자가 유효하지 않습니다."),
    RECEIVING_HANDLER_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "입고 담당자 정보가 필요합니다."),
    SHIPPING_HANDLER_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "출고 담당자 정보가 필요합니다."),

    /** 401 UNAUTHORIZED */
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    AUTH_BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "자격 증명이 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "액세스 토큰이 유효하지 않거나 손상되었습니다."),
    USER_BLACKLISTED(HttpStatus.UNAUTHORIZED, "차단된 사용자입니다. 관리자에게 문의하세요."),

    /** 404 NOT_FOUND */
    NOT_FOUND_MEMBER_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    PCM_NOT_FOUND(HttpStatus.NOT_FOUND, "부품-차량 모델 매핑을 찾을 수 없습니다."),

    /** 409 CONFLICT */
    CONFLICT_RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 자원입니다."),
    CONFLICT_NOTE_STATUS_WHILE_COMPLETE_OR_DELAYED(HttpStatus.CONFLICT, "출고 전표가 더 이상 수정될 수 없는 상태입니다."),
    CONFLICT_CANNOT_COMPLETE_WHEN_NOT_READY(HttpStatus.CONFLICT, "출고 완료 불가: READY가 아닌 라인이 포함되어 있습니다."),
    CONFLICT_RECEIVING_LINE_ALREADY_DONE(HttpStatus.CONFLICT, "입고 라인이 이미 완료되어 수정할 수 없습니다."),
    CONFLICT_RECEIVING_NOTE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "입고 전표가 이미 완료 상태입니다."),
    CONFLICT_RECEIVING_CANNOT_COMPLETE_WHEN_NOT_DONE(HttpStatus.CONFLICT, "입고 완료 불가: 진행 중 라인이 존재합니다."),
    CONFLICT_INVENTORY_INSUFFICIENT(HttpStatus.CONFLICT, "재고 부족으로 차감할 수 없습니다."),
    PART_CATEGORY_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 카테고리 이름입니다."),
    PART_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 부품 코드입니다."),
    PART_CATEGORY_HAS_PARTS(HttpStatus.CONFLICT, "카테고리에 속한 부품이 있어 삭제할 수 없습니다."),
    PART_HAS_MAPPINGS(HttpStatus.CONFLICT, "해당 부품은 차량 모델 매핑이 있어 삭제할 수 없습니다."),
    PCM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 부품-차량 모델 매핑입니다."),

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
