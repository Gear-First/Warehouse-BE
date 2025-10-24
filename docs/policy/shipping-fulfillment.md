# Shipping Fulfillment Policy (MVP, Updated)

- Status: Accepted (Updated)
- Date: 2025-10-24 (문서 정합화)
- Deciders: 현희찬

## 불변식

- 0 ≤ pickedQty ≤ orderedQty (라인 단위)
- orderedQty > 0 (지시 수량은 양수)

## 상태

- Note: PENDING → IN_PROGRESS → COMPLETED | DELAYED
- Line: PENDING → IN_PROGRESS → READY | SHORTAGE

## DELAYED 기준(명시)

- 피킹/검수 시 onHand < orderedQty인 라인이 하나라도 있으면 해당 라인은 SHORTAGE로 표시
- ANY LINE SHORTAGE가 발생하면 Note = DELAYED로 즉시 전이
  - 이후 라인/노트 수정 불가(409), 출고 완료 불가
  - (선택) Backorder/DelayTicket 생성은 후속 프로세스에서 처리(현 단계 범위 아님)

## 완료 조건

- 모든 라인이 READY일 때만 Note = COMPLETED로 전이 가능

## 재고 반영 규칙(핵심)

- 차감 시점: Note가 COMPLETED로 전이될 때
- 차감 기준 수량: 각 READY 라인의 orderedQty

## 참고

- 관련: ADR-01, ADR-02, ADR-05
- UC 문서: docs/uc/02-shipping/* (본 정책의 상태 명칭과 일치시키도록 업데이트 예정)
