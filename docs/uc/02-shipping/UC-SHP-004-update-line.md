# UC-SHP-004 출고 라인 진행 업데이트 (READY/DELAYED 판정)

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

## Policy

- onHand ≥ orderedQty → `READY` (지급 준비 완료)
- onHand < orderedQty → `DELAYED` (지급 지연)

## Preconditions

- Note.status ∈ {PENDING, IN_PROGRESS}
- 라인이 완료 상태 아님(DONE 아님)

## Main Flow

1) `{ inspectedQty, issue? }`를 입력받는다(검수 기록용).
2) 현재 onHand를 조회한다.
3) onHand와 orderedQty를 비교하여 상태를 `READY` 또는 `DELAYED`로 전이한다.
4) 노트가 첫 갱신이면 `PENDING → IN_PROGRESS`.

## Acceptance

- Given onHand ≥ ordered,
  When 업데이트하면,
  Then 라인은 `READY`
- Given onHand < ordered,
  When 업데이트하면,
  Then 라인은 `DELAYED`
- Given 라인이 완료 상태,
  When 업데이트하면,
  Then 409가 반환

## I/O

- PATCH `/v1/shipping/{noteId}/lines/{lineId}`
- Request `{ "inspectedQty": 28, "issue": null }`
- Response: 최신 NoteDetail 스냅샷

## Errors

- 404: note/line 미존재
- 409: 완료 라인 재수정
- 422: 입력 유효성 실패
