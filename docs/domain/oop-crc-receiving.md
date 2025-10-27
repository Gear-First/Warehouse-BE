# OOP CRC - Receiving (MVP, Updated)

- Status: Accepted (Updated)
- Date: 2025-10-24 (정책 정합화)
- Deciders: 현희찬

## ReceivingApplicationService

- 책임: 로드 → 도메인 행위 호출 → 저장 (오케스트레이션)

## ReceivingNote (AR)

- 역할: 라인 업데이트 관문, 완료 가능 판단, 재고 반영 집계(후속 PR)
- 책임
    - updateLine(...): 라인에 검수 결과 적용 → ACCEPTED or REJECTED
    - 첫 갱신 시 PENDING → IN_PROGRESS
    - canComplete(): 모든 라인이 ACCEPTED | REJECTED
    - complete(): ACCEPTED 라인만 제품별 증가 수량 집계(REJECTED=0)
- 협력: ReceivingNoteLine, InventoryUpdater

## ReceivingNoteLine

- 역할: All-or-Nothing 규칙의 최소 단위(현 단계 부분수용 미지원)
- 책임
    - 이슈 감지되면 즉시 REJECTED 전이(증가 0)
    - 문제가 없으면 ACCEPTED 전이(증가 수량=주문/검수량)
- 핵심 메서드
    - applyInspection(inspectedQty, hasIssue)
    - boolean isAccepted() / boolean isRejected()

## InventoryUpdater (도메인 서비스, 이후 단계)

- 역할/책임: ACCEPTED 라인 집계분만 increase 적용(Receiving 완료 시)
