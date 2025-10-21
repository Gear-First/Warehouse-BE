# UC-REC-005 입고 완료 & 재고 반영

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `25-10-20`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Policy

- **READY 라인만** 재고 증가, `RETURNED` 라인은 증가 0

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
