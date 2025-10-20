# Shipping Inspection Policy (MVP)

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

## 불변식

- 0 ≤ inspectedQty (음수 불가)
- orderedQty > 0 (지시 수량은 양수)

## 상태

- Note: NOT_STARTED => IN_PROGRESS => DONE_OK | DELAYED
- Line: NOT_STARTED => IN_PROGRESS → DONE_OK | DONE_ISSUE

### DONE 관련 로직 구체화 필요 (// TODO)
- 재고 부족 시 ‘지급 지연(Delay)’
- 검수/피킹 시 onHand < orderedQty면 해당 라인은 DELAYED 처리.

## 완료 조건: 모든 라인 DONE_*

## 재고 반영 규칙(핵심)

### 기본 차감량: 각 라인의 orderedQty

- 출고 지시 수량(orderedQty)을 기준으로 재고 차감
- 만약 재고가 부족하면 완료 거부 및 출고 연기(예외 발생)
- 차감 시 onHandQty(product) ≥ orderedQty 가 아니면 예외 (부족/shortage)
- 부족 발생 시: 완료 거부(예외)로 처리 → “출고 불가”를 명확히 알림
