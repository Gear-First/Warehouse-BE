# Glossary (상태/수량/식별자)

- Status: Updated
- Date: 2025-10-27
- Deciders: 현희찬

## 상태(State)
- Shipping Note: PENDING → IN_PROGRESS → COMPLETED | DELAYED
- Shipping Line: PENDING → READY | SHORTAGE
- Receiving Note: PENDING → IN_PROGRESS → COMPLETED_OK | COMPLETED_ISSUE
- Receiving Line: PENDING → ACCEPTED | REJECTED

## 수량(Quantities)
- orderedQty: 지시/주문 수량(>0)
- allocatedQty: 출고 할당 수량(출고)
- pickedQty: 집품/피킹 수량(출고)
- inspectedQty: 검수 수량(입고)
- acceptedQty: 입고 수용 수량(ACCEPTED 라인)
- returnedQty: 반품/거부 수량(REJECTED 라인은 orderedQty)
- issueQty: 불량/파손 수량(현 단계 REJECTED면 orderedQty)

## 식별자(Identifiers)
- shippingNo: OUT/<warehouse>/<yyyymmdd>/<seq>
- receivingNo: IN/<warehouse>/<yyyymmdd>/<seq>
- warehouseId: 창고 식별자(멀티 창고 도입 보류, 문서 표기만)

## 기타 용어
- branchName: 납품처(대리점) 명칭(Shipping)
- supplierName: 공급업체/공장 명칭(Receiving)
- PageEnvelope: 목록 응답 래퍼 { items, page, size, total }

## 참고
- minutes: context/minute-of-functional-spec.md (상태 정의의 최상위 요약)
- 정책: policy/shipping-fulfillment.md, policy/receiving-inspection.md
- ADR: ADR-05 Use Cases are Non-Authoritative
