# Shipping Inspection Policy (MVP)

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

## 불변식

- 0 ≤ inspectedQty (음수 불가)
- orderedQty > 0 (지시 수량은 양수)

## 상태

- Note: PENDING => IN_PROGRESS => COMPLETED | DELAYED
- Line: PENDING => IN_PROGRESS => READY | SHORTAGE

### DONE 관련 로직 구체화 필요 (// TODO)
- 재고 부족 시 ‘지급 지연(Delay)’
- 검수/피킹 시 onHand < orderedQty면 해당 라인은 DELAYED 처리

## 완료 조건: 모든 라인 READY

## 재고 반영 규칙(핵심)

### 기본 차감량: 각 라인의 orderedQty

- 부족(ANY LINE SHORTAGE) 발생 시 즉시 Note = DELAYED
    - 이후 라인/노트 수정 불가(409), 출고 완료 불가
    - (선택) Backorder/DelayTicket 생성은 별도 후속 프로세스
- **완료(COMPLETED)** : 전 라인이 READY일 때만 가능
- **차감(재고 감소)** : 완료 시에만 발생(COMPLETED - READY 전 라인 orderedQty 기준)
