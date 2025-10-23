# OOP CRC - Receiving (MVP)

- Status: Accepted(Updated)
- Date: 2025-10-20(수정일)
- Deciders: 현희찬

## DeliveryApplicationService

- 책임: 로드 => 도메인 행위 호출 => 저장 (오케스트레이션)


## DeliveryNote (AR)

- 역할: 라인 업데이트 관문, 완료 가능 판단, 재고 반영 집계
- 책임
    - updateLine(...): 라인에 검수 결과 적용 → ACCEPTED or RETURNED
    - 첫 갱신 시 PENDING→IN_PROGRESS
    - canComplete(): 모든 라인이 ACCEPTED | RETURNED
    - complete(): ACCEPTED 라인만 제품별 증가 수량 집계(RETURNED=0)
- 협력: DeliveryNoteLine, InventoryUpdater

## DeliveryNoteLine

- 역할: All-or-Nothing 규칙의 최소 단위
- 책임
    - 이슈 감지되면 즉시 RETURNED 전이(증가 0)
    - 문제가 없으면 ACCEPTED 전이(증가 수량=주문/검수량)
- 핵심 메서드
    - applyInspection(inspectedQty, hasIssue)
    - boolean isAccepted() / boolean isReturned()

## InventoryUpdater (도메인 서비스)

- 역할/책임: ACCEPTED 라인 집계분만 increase 적용
