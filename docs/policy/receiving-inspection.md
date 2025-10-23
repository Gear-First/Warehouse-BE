# Receiving Inspection Policy (MVP)

- Status: Accepted(Updated)
- Date: 2025-10-21(수정일, 구현 반영 전)
- Deciders: 현희찬

## 불변식

- 0 ≤ issueQty ≤ inspectedQty ≤ orderedQty
- (ReceivingOrderLine 기준)

## 상태

- Note: PENDING → IN_PROGRESS → ACCEPTED | RETURNED
- Line: PENDING → IN_PROGRESS → DONE_OK | DONE_ISSUE (=> RETURNED)
- DONE_ISSUE의 경우 해당 라인에 한해 전부 RETURNED 처리

## 적용 수량

- inspectedQty: 실제 검수된 수량
- issueQty: 불량으로 판정된 수량
- returnedQty: 반품 수량 = orderedQty (DONE_ISSUE 시)
- acceptedQty: 정상 수량 = orderedQty (DONE_OK 시)

## Note 완료 조건

- 모든 라인이 DONE_* 이어야 완료 가능

## 참고

- 관련: ADR-01, ADR-02 (링크만)
