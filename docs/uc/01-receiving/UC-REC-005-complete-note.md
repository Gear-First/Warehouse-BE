# UC-REC-005 입고 완료 & 재고 반영

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

## Policy

- **All-or-Nothing per Product**: 라인에 문제가 한 건이라도 있으면 **그 라인은 `RETURNED`** (재고 증가 0)
- **`ACCEPTED` 라인만** 재고 증가 대상

## Preconditions

- 노트의 모든 라인이 **`ACCEPTED | RETURNED`** 상태여야 한다. (진행 중인 라인 없음)

## Main Flow

1) 시스템은 `noteId`로 노트를 로드하고 `canComplete()`를 확인한다.
2) `complete()` 수행 시 **`ACCEPTED` 라인만** 제품별 합계를 계산한다.
3) Inventory에 계산된 합계를 **증가** 반영한다.
4) `NoteStatus = COMPLETED`로 전이하고 완료 요약을 반환한다.

## Acceptance (GWT)

- **Given** 라인들이 `ACCEPTED/RETURNED` 조합이고,  
  **When** 완료를 요청하면,  
  **Then** **`ACCEPTED` 라인 합계만** 재고가 증가하고 노트는 `COMPLETED`가 된다.
- **Given** `ACCEPTED`/`RETURNED` 외의 상태(예: `IN_PROGRESS`, `PENDING`) 라인이 하나라도 있고,  
  **When** 완료를 요청하면,  
  **Then** 409(완료 불가)가 반환된다.

## I/O (Sketch)

- **POST** `/v1/receiving/{noteId}:complete`
- **Response**

```json
{
  "completedAt": "2025-01-01T00:00:00Z",
  "increased": {
    "P-1001": 48,
    "P-1002": 40
  },
  "acceptedCount": 2,
  "returnedCount": 1
}
```
