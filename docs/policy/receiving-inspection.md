# Receiving Inspection Policy (MVP, Updated)

- Status: Accepted (Updated)
- Date: 2025-10-24 (문서 정합화, 구현 반영 전)
- Deciders: 현희찬

## 불변식

- 0 ≤ issueQty ≤ inspectedQty ≤ orderedQty (라인 단위)
- REJECTED 시 acceptedQty = 0, returnedQty = orderedQty
- ACCEPTED 시 returnedQty = 0, acceptedQty = orderedQty

## 상태 정의(정합화)

- Note: PENDING → IN_PROGRESS → COMPLETED_OK | COMPLETED_ISSUE
- Line: PENDING → ACCEPTED | REJECTED
- REJECTED 트리거: 파손/부족 등 검수 이슈가 발생하면 해당 라인은 전량 거부(REJECTED)

## 적용 수량 규칙

- inspectedQty: 실제 검수된 수량(orderedQty와 동일 가정; 부분 수용은 현 단계 미지원)
- issueQty: 불량/파손으로 판정된 수량(현 단계에서는 REJECTED인 경우 issueQty = orderedQty)
- returnedQty: REJECTED이면 orderedQty, 그 외 0
- acceptedQty: ACCEPTED이면 orderedQty, 그 외 0

## Note 완료 조건 및 판정

- 모든 라인이 최종 상태(ACCEPTED 또는 REJECTED)여야 완료 가능
- 완료 판정:
  - COMPLETED_OK: 모든 라인이 ACCEPTED
  - COMPLETED_ISSUE: 하나 이상 라인이 REJECTED
- 원칙: 문제 없는 라인은 모두 수용(ACCEPTED), 문제가 특정된 라인은 전량 거부(REJECTED). 거부 라인의 재요청/반품 등 후속 처리는 후속 PR에서 다룸.

## 용어 정의

- orderedQty: 요청/발주 수량(받아야 할 물량)
- receivedQty: 실제 도착 수량
- inspectedQty: 검수 통과 수량(MVP에서는 부분 수용 미지원 가정 하에 ACCEPTED 시 orderedQty와 동일)
- acceptedQty(파생): 재고 반영 수량
  - MVP: ACCEPTED → acceptedQty = orderedQty, REJECTED → acceptedQty = 0
  - 향후 부분 수용 도입 시: acceptedQty = inspectedQty로 전환 가능(정책/UC 동시 개정)

> 인벤토리 반영: 입고 완료 시 증가 기준은 acceptedQty의 합계(MVP에서는 사실상 orderedQty 합계)입니다.

## 참고

- 관련: ADR-01, ADR-02, ADR-05
- UC 문서: docs/uc/01-receiving/* (본 정책의 상태 명칭과 일치시키도록 업데이트 예정)
