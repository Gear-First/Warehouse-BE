# UC-REC-002 오늘 완료 입고 목록 조회

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

## Intent

오늘 완료(`COMPLETED`)된 입고 납품서 목록을 조회

## Main Flow

1) 오늘 날짜 기준으로 완료 납품서를 조회
2) 완료 시간과 함께 요약 DTO를 반환

## Acceptance

- Given 오늘 완료된 납품서가 있을 때,
  When 완료 목록을 조회하면,
  Then `COMPLETED` 상태의 납품서와 `completedAt`이 포함된 요약 DTO 리스트가 반환

## I/O

- GET `/v1/receiving/completed?date=YYYY-MM-DD`
- Response `[ { noteId, supplierName, itemKinds, totalQty, status, completedAt } ]`

## Errors

- 400: 잘못된 날짜 포맷
