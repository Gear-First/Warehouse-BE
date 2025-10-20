# UC-SHP-002 오늘 완료 출고 목록 조회

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

## Intent

오늘 완료(`COMPLETED`)된 출고 납품서 목록을 조회 (지연 라인 수 요약 포함)

## I/O

- GET `/v1/shipping/completed?date=YYYY-MM-DD`
- Response `[ { noteId, supplierName, itemKinds, totalQty, status, completedAt, delayedCount } ]`
