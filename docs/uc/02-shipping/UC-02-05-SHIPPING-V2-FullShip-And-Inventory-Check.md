# UC-02-05: 출고 V2 — 전량 출고 + 재고 확인/확정 플로우

작성일: 2025-11-07
담당: Warehouse-BE
관련 컨텍스트: ../context/CONTEXT-2025-11-07-shipping-v2.md

## 목적
전량 출고 정책 하에서, onHand(가용재고)와 ordered(주문수량) 기반으로 라인/노트 상태를 확인(dryRun)하고 필요 시 확정(apply)하여, 지연(DELAYED)과 완료(COMPLETED)를 일관되게 운용한다.

## 액터
- 창고 작업자(피커)
- 출고 관리자
- 재고(Inventory) 서비스(외부/내부 연동)

## 전제조건(Preconditions)
- 출고 노트(Shipping Note)가 생성되어 있음: NoteStatus ∈ {PENDING, IN_PROGRESS, DELAYED}
- 부분 출고 금지(전량 출고만 허용)
- Inventory 서비스에서 품목별 onHand 조회 가능

## 트리거
- 작업자가 노트 상세를 열어 출고 가능 여부를 확인하거나, 라인별 확정을 수행하거나, 전체 완료 처리 시도

## 기본 흐름(Main Success Scenario)
1. 작업자는 노트 상세(V2)를 조회한다.
   - 시스템: onHand를 조회하여 각 라인의 suggestedStatus를 계산해 응답한다.
2. 작업자는 노트 단위로 "출고 가능 여부 확인(check-shippable)"을 실행한다(미적용).
   - 시스템: 모든 라인의 suggestedStatus를 계산하고, 노트의 suggestedStatus를 산출해 보여준다.
3. 작업자는 필요 시 "적용(apply)"을 실행한다.
   - 시스템: suggestedStatus를 영속 반영한다.
   - 하나라도 SHORTAGE → 라인 SHORTAGE 확정, 노트 DELAYED(+ delayedAt)
   - 모두 READY → 라인 READY 확정, 노트는 IN_PROGRESS 유지/전이
4. 모든 라인이 READY가 되면, 작업자는 완료(complete)를 실행한다.
   - 시스템: 트랜잭션 내에서 onHand ≥ ordered를 재검증하고, 재고를 차감한 뒤 노트를 COMPLETED로 전이(+ completedAt)

## 대안 흐름(Extensions)
E1. 라인 개별 확정(Confirm)
- 1a. 작업자는 특정 라인에서 "확정(confirm)"을 실행한다.
- 1b. 시스템은 해당 라인의 onHand를 조회하여 suggestedStatus를 계산하고, 그 상태를 라인 currentStatus로 반영한다.
  - READY → 라인 READY, 노트는 IN_PROGRESS 유지/전이
  - SHORTAGE → 라인 SHORTAGE, 노트 DELAYED(+ delayedAt)

E2. 입고 후 지연 복구
- 2a. 재고 증가(입고) 후, 작업자가 다시 check-shippable(apply=true) 또는 라인 confirm을 실행한다.
- 2b. 시스템은 SHORTAGE가 해소된 라인을 READY로 확정하고, 모든 라인이 SHORTAGE가 아니게 되면 노트를 IN_PROGRESS로 복귀시킨다.

E3. 완료 시 재검증 실패(레이스)
- 3a. complete 직전 재검증에서 일부 라인이 onHand < ordered로 판정되면, 시스템은 완료를 거부(409)하고 노트를 DELAYED로 유지/전이한다.
- 3b. 작업자는 재확인 후 다시 시도한다.

## 예외/에러 처리
- SHIPPING_LINE_SHORTAGE(409): 라인 confirm 시 READY 불가(정책에 따라 200으로 반영만 할 수도 있음)
- SHIPPING_NOTE_NOT_SHIPPABLE(409): complete 시 READY 아닌 라인 존재
- SHIPPING_INVENTORY_CHANGED(409): complete 직전 재검증 실패(레이스)
- CONFLICT_NOTE_STATUS_WHILE_COMPLETE_OR_DELAYED(409): 터미널 상태에서 금지된 작업 요청

## 비즈니스 규칙
- 전량 출고만 허용(부분 출고 금지)
- 재고 차감은 complete에서만 수행
- DELAYED는 가역 상태(입고로 해소되면 IN_PROGRESS 복귀 가능)
- detail-v2와 check-shippable(dryRun)은 영속 상태를 변경하지 않음

## 사후조건(Postconditions)
- COMPLETE 성공 시: 노트는 COMPLETED, completedAt 기록, 각 READY 라인 수량만큼 재고 차감 완료
- DELAYED 확정 시: delayedAt 기록, 이후 입고/재평가를 통해 복귀 가능

## API 매핑(신규)
- GET /shipping/notes/{id}/detail-v2
  - 조회 전용: onHand, suggestedStatus 포함
- POST /shipping/notes/{id}/check-shippable?apply=false|true
  - 노트 단위 재평가(dryRun/apply)
- PATCH /shipping/notes/{id}/lines/{lineId}/confirm
  - 라인 단위 확정
- POST /shipping/notes/{id}/complete
  - 모든 라인 READY일 때 완료(재고 차감 포함)

## 데이터 요소(주요)
- Line: orderedQty, currentStatus, onHandQty(view), suggestedStatus(view)
- Note: status, completedAt, delayedAt, inventorySnapshotAt(view)

## UI 힌트(요약)
- 상세화면(V2): 라인마다 onHandQty, suggestedStatus 배지 표기
- 버튼 구성: [출고 가능 여부 확인] → [적용] / 라인별 [확정]
- COMPLETE 버튼은 모든 라인이 READY일 때만 활성화
