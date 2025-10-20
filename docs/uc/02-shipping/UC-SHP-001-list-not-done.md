# UC-REC-005 입고 완료 & 재고 반영

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

## Policy

- **ACCEPTED 라인만** 재고 증가, `RETURNED` 라인은 증가 0

## Preconditions

- 모든 라인이 `ACCEPTED | RETURNED` 상태

## Main Flow

1) `canComplete()`를 확인
2) `complete()` 수행 시 ACCEPTED 라인의 제품별 합계를 계산
3) Inventory에 합계만큼 증가 적용
4) NoteStatus = `COMPLETED`, 완료 요약을 반환

## Acceptance

- Given 라인들이 ACCEPTED/RETURNED 조합,
  When 완료를 요청하면,
  Then ACCEPTED 라인 합계만 증가하고 Note는 `COMPLETED`가
- Given 미완료 라인이 존재,
  When 완료를 요청하면,
  Then 409가 반환

## I/O

- POST `/v1/receiving/{noteId}:complete`
- Response
  `{ "completedAt": "2025-01-01T00:00:00Z", "increased": { "P-1001": 48 }, "acceptedCount": 2, "returnedCount": 1 }`

## Errors

- 404: noteId 없음
- 409: 완료 불가 상태
