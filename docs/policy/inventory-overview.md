# Inventory Overview Policy (Draft)

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

## 목적
- 창고/부품 기준 현재 재고 수량(On-hand)을 조회하기 위한 읽기 모델의 정책을 정의한다.
- 구현은 후속 PR에서 진행하며, 본 문서는 UC와 표준 용어에 맞춰 필드/필터/정렬/페이지네이션을 명세한다.

## 기본 컬럼
- warehouseCode: String
- part: { id, code, name }
- onHandQty: int (>=0)
- lastUpdatedAt: datetime (선택)

## 필터
- warehouseCode: = (필수 또는 기본 단일 창고 환경에서는 생략 가능)
- partKeyword: partCode 또는 partName 부분 일치(대소문자 무시)
- supplierName: 공급업체 명칭 부분 일치(대소문자 무시)
- minQty / maxQty: onHandQty 범위 필터 (선택)

## 정렬
- 기본: partName ASC
- 보조: partCode ASC
- 커스텀 파라미터: sort=partName,asc | onHandQty,desc 등 (후속 PR에서 확정)

## 페이지네이션
- PageEnvelope 표준 사용
- 기본값: page=0, size=20 (size 범위 1..100)

## 에러
- 400: 잘못된 파라미터(페이지/사이즈 범위 벗어남, 정렬 키 불일치 등)

## 용어/계산(핵심)
- OnHand = Σ(acceptedQty from Receiving COMPLETED_OK|COMPLETED_ISSUE) − Σ(shippedQty from Shipping COMPLETED)
  - Receiving: 재고 증가 기준 수량은 acceptedQty
    - MVP 정책: 부분 수용 미지원 → acceptedQty = (ACCEPTED 라인) orderedQty, REJECTED 라인은 0
    - 향후 부분 수용 도입 시: acceptedQty = inspectedQty로 정책/UC 동반 전환
  - Shipping: 재고 감소 기준 수량은 shippedQty(노트가 COMPLETED로 전이될 때 반영)

## CRUD 원칙
- Read: UC-INV-001 List On-Hand 제공(페이지네이션/정렬/필터)
- Create/Update/Delete: 일반 운영에서는 사용하지 않음(이력 보정/초기 적재/오류 복구 등 특수 상황 전용)
  - Swagger/UC에 경고 문구 명시: "일반적인 상황에서는 사용하지 마십시오"

## 입출고 연계 및 일관성
- 초기 버전: 동기 처리(서비스 계층)로 입출고 이벤트 시점에 on-hand 반영
- 후속: 비동기 전환 여부는 별도 ADR로 결정

## 멀티 창고/권한
- 멀티 창고 도입 시 warehouseId 단위로 OnHand를 분리 집계한다
- 권한 스코프(문서 기준): 창고 담당자는 소속 창고 데이터만, HQ는 모든 창고 조회 가능

## Notes
- 실제 on-hand 계산/저장은 후속 PR에서 결정(누적 테이블 vs 뷰/계산). 현재 문서는 조회 스펙만 정의한다.
- minutes 참조: docs/context/minute-of-functional-spec.md > 재고관리(Inventory Overview)
