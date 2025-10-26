# Inventory Overview Policy (Draft)

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

## 목적
- 창고/부품 기준 현재 재고 수량(On-hand)을 조회하기 위한 읽기 모델의 정책을 정의한다.
- 구현은 후속 PR에서 진행하며, 본 문서는 UC와 표준 용어에 맞춰 필드/필터/정렬/페이지네이션을 명세한다.

## 기본 컬럼
- warehouseId: Long
- part: { id, code, name }
- onHandQty: int (>=0)
- lastUpdatedAt: datetime (선택)

## 필터
- warehouseId: = (필수 또는 기본 단일 창고 환경에서는 생략 가능)
- partCode / partName: contains (부분 일치)
- minQty / maxQty: 범위 필터 (선택)

## 정렬
- 기본: partName ASC
- 보조: partCode ASC
- 커스텀 파라미터: sort=partName,asc | onHandQty,desc 등 (후속 PR에서 확정)

## 페이지네이션
- PageEnvelope 표준 사용
- 기본값: page=0, size=20 (size 범위 1..100)

## 에러
- 400: 잘못된 파라미터(페이지/사이즈 범위 벗어남, 정렬 키 불일치 등)

## Notes
- 실제 on-hand 계산/저장은 후속 PR에서 결정(누적 테이블 vs 뷰/계산). 현재 문서는 조회 스펙만 정의한다.
- minutes 참조: docs/context/minute-of-functional-spec.md > 재고관리(Inventory Overview)
