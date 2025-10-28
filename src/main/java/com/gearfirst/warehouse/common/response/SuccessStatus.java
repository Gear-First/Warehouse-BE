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
    SEND_RECEIVING_COMPLETE_SUCCESS(HttpStatus.OK, "입고 완료 처리 성공"),
    SEND_RECEIVING_NOTE_LINE_UPDATE_SUCCESS(HttpStatus.OK, "입고요청서 항목 수정 성공"),

    SEND_SHIPPING_NOTE_LIST_SUCCESS(HttpStatus.OK, "출고요청서 목록 조회 성공"),
    SEND_SHIPPING_NOTE_DETAIL_SUCCESS(HttpStatus.OK, "출고요청서 상세 조회 성공"),
    SEND_SHIPPING_COMPLETE_SUCCESS(HttpStatus.OK, "출고 완료 처리 성공"),
    SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS(HttpStatus.OK, "출고요청서 항목 수정 성공"),

    SEND_PART_CATEGORY_LIST_SUCCESS(HttpStatus.OK, "부품 카테고리 목록 조회 성공"),
    SEND_PART_CATEGORY_DETAIL_SUCCESS(HttpStatus.OK, "부품 카테고리 상세 조회 성공"),
    SEND_PART_CATEGORY_CREATE_SUCCESS(HttpStatus.OK, "부품 카테고리 생성 성공"),
    SEND_PART_CATEGORY_UPDATE_SUCCESS(HttpStatus.OK, "부품 카테고리 수정 성공"),
    SEND_PART_CATEGORY_DELETE_SUCCESS(HttpStatus.OK, "부품 카테고리 삭제 성공"),

    SEND_PART_LIST_SUCCESS(HttpStatus.OK, "부품 목록 조회 성공"),
    SEND_PART_DETAIL_SUCCESS(HttpStatus.OK, "부품 상세 조회 성공"),
    SEND_PART_CREATE_SUCCESS(HttpStatus.OK, "부품 생성 성공"),
    SEND_PART_UPDATE_SUCCESS(HttpStatus.OK, "부품 수정 성공"),
    SEND_PART_DELETE_SUCCESS(HttpStatus.OK, "부품 삭제 성공"),

    // PCM (Part–CarModel mapping) list endpoints
    SEND_PCM_CARMODEL_LIST_SUCCESS(HttpStatus.OK, "부품 적용 차량 모델 목록 조회 성공"),
    SEND_PCM_PART_LIST_SUCCESS(HttpStatus.OK, "차량 모델 적용 부품 목록 조회 성공"),

    SEND_INVENTORY_ONHAND_LIST_SUCCESS(HttpStatus.OK, "재고 현황 목록 조회 성공"),
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
