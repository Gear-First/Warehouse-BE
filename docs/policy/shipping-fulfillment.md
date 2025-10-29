# Shipping Fulfillment Policy (MVP, Updated)

- Status: Accepted (Updated)
- Date: 2025-10-27 (문서 정합화)
- Deciders: 현희찬

## 불변식

- 0 ≤ pickedQty ≤ allocatedQty ≤ orderedQty (라인 단위)
- orderedQty > 0 (지시 수량은 양수)

## 상태

- Note: PENDING → IN_PROGRESS → COMPLETED | DELAYED
- Line: PENDING → READY | SHORTAGE

## 상태 도출(서버)

- 클라이언트는 수량만 전달한다: { allocatedQty, pickedQty }
- 서버는 위 불변식을 검증한 뒤 라인 상태를 도출한다
  - pickedQty = allocatedQty = orderedQty 이면 READY (예시)
  - 재고/할당/피킹 조건을 충족하지 못하면 SHORTAGE

## DELAYED 기준(명시)

- 라인이 SHORTAGE로 판정되는 순간, Note = DELAYED 로 즉시 전이
  - 이후 라인/노트 변경 및 완료 요청은 409로 거부(블로킹)
  - (선택) Backorder/DelayTicket 생성은 후속 프로세스에서 처리(현 단계 범위 아님)

## 완료 조건

- 모든 라인이 READY일 때만 Note = COMPLETED로 전이 가능

## 재고 반영 규칙(핵심)

- 차감 시점: Note가 COMPLETED로 전이될 때
- 차감 기준 수량: 각 라인의 shippedQty 합계(파생: backorderQty = orderedQty − shippedQty)
- 일관성: 초기 버전은 동기 처리(서비스 계층)로 반영, 후속 ADR에서 비동기 전환 검토

## 멀티 창고/권한 노트
- 창고 담당자: 소속 창고 데이터만 접근 가능(문서 기준). HQ는 전 창고 조회 가능.
- warehouseId 필터/필드는 문서에만 우선 반영하고 구현은 후속 PR에서 진행.

## 참고

- 관련: ADR-01, ADR-02, ADR-05
- UC 문서: docs/uc/02-shipping/* (본 정책의 상태 명칭과 일치시키도록 업데이트됨)
