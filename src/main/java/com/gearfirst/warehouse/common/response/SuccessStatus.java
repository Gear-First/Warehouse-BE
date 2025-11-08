package com.gearfirst.warehouse.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {
    /**
     * 200 SUCCESS
     */
    SEND_HEALTH_SUCCESS(HttpStatus.OK, "서버 상태 OK"),
    SEND_RECEIVING_NOTE_LIST_SUCCESS(HttpStatus.OK, "입고요청서 목록 조회 성공"),
    SEND_RECEIVING_NOTE_DETAIL_SUCCESS(HttpStatus.OK, "입고요청서 상세 조회 성공"),
    SEND_RECEIVING_COMPLETE_SUCCESS(HttpStatus.OK, "입고 완료 처리 성공"),
    SEND_RECEIVING_NOTE_LINE_UPDATE_SUCCESS(HttpStatus.OK, "입고요청서 항목 수정 성공"),

    SEND_SHIPPING_NOTE_LIST_SUCCESS(HttpStatus.OK, "출고요청서 목록 조회 성공"),
    SEND_SHIPPING_NOTE_DETAIL_SUCCESS(HttpStatus.OK, "출고요청서 상세 조회 성공"),
    SEND_SHIPPING_COMPLETE_SUCCESS(HttpStatus.OK, "출고 완료 처리 성공"),
    SEND_SHIPPING_NOTE_LINE_UPDATE_SUCCESS(HttpStatus.OK, "출고요청서 항목 수정 성공"),
    SEND_SHIPPING_NOTE_DETAIL_V2_SUCCESS(HttpStatus.OK, "출고요청서 상세(V2) 조회 성공"),
    SEND_SHIPPING_NOTE_RECALC_SUCCESS(HttpStatus.OK, "출고요청서 재고 재평가 성공"),
    SEND_SHIPPING_NOTE_LINE_CONFIRM_SUCCESS(HttpStatus.OK, "출고요청서 항목 확정 성공"),
    SEND_SHIPPING_NOTE_COMPLETED_DETAIL_V2_SUCCESS(HttpStatus.OK, "출고 완료 처리 성공(V2 상세)"),

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

    // PCM (Part–CarModel mapping)
    SEND_PCM_CARMODEL_LIST_SUCCESS(HttpStatus.OK, "부품 적용 차량 모델 목록 조회 성공"),
    SEND_PCM_PART_LIST_SUCCESS(HttpStatus.OK, "차량 모델 적용 부품 목록 조회 성공"),
    SEND_PCM_CREATE_SUCCESS(HttpStatus.OK, "부품-차량 모델 매핑 생성 성공"),
    SEND_PCM_UPDATE_SUCCESS(HttpStatus.OK, "부품-차량 모델 매핑 수정 성공"),
    SEND_PCM_DELETE_SUCCESS(HttpStatus.OK, "부품-차량 모델 매핑 삭제(비활성화) 성공"),

    SEND_INVENTORY_ONHAND_LIST_SUCCESS(HttpStatus.OK, "재고 현황 목록 조회 성공"),

    // Summary
    SEND_NOTE_COUNTS_SUCCESS(HttpStatus.OK, "요청일 기준 입/출고 전표 건수 조회 성공"),
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
