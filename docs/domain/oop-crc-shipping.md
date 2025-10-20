# OOP CRC - Shipping (MVP)

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

## ShippingNote (AR)

- 역할: 라인 진행/판정 관리, 완료 시 출하 계획 산출
- 책임
    - updateLine(...): onHand 검사 → READY 또는 DELAYED(or DONE_ISSUE)
    - 첫 갱신 시 PENDING→IN_PROGRESS
    - canComplete(): 모든 라인이 READY | DELAYED | DONE_ISSUE
    - complete(): READY 라인만 제품별 orderedQty 합으로 차감 계획 산출, completedAt 설정
    - DELAYED/DONE_ISSUE는 차감/출하 제외, 지연티켓 목록 반환
- 협력: ShippingLine, InventoryGateway/Repository, DelayTicketService(선택)

## ShippingLine

- 역할: 재고 충족/부족 판단의 최소 단위
- 책임
    - orderedQty > 0 유지, inspectedQty ≥ 0
    - onHand ≥ orderedQty → READY, 아니라면 DELAYED
- 핵심 메서드
    - applyProgress(inspectedQty, onHand, issue?) → READY|DELAYED|DONE_ISSUE
    - Qty orderedQty()
    - DelayTicket (옵션, 엔티티/기록)
- 역할: 지연 라인에 대한 후속 처리 추적(예약, 재배차, 입고대기 매칭 등)
- 책임: productId, missingQty, reason(OUT_OF_STOCK), createdAt, noteId/lineId


## CompleteShippingProcess (App)

- 역할: 트랜잭션 경계, 차감/저장 오케스트레이션, 지연 처리
- 책임
    - note.complete() → {toDeduct, delayedLines, completedAt}
    - toDeduct만 decrease(원자적)
    - delayedLine 하나라도 발생 시 Note 전체는 DELAYED 상태 유지
- 협력: ShippingNote, InventoryService, DelayTicketService
- 고려 사항
    - 지연 티켓
    - 고지 방법
