# UC-SHP-004 출고 라인 진행 업데이트 (READY/DELAYED 판정)

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Policy

- 검수/피킹 시점에 출고 라인의 지급 준비 상태 갱신
    - 검수 수량(inspectedQty) 기록
    - 재고 수량(onHand)과 지시 수량(orderedQty) 비교
- 지급 준비 상태 판정:
    - 담당자가 검수 후 재고 수량(onHand)을 입력 또는 시스템이 조회한 값을 사용
    - onHand ≥ orderedQty → `READY` (라인 출고 준비 완료)
    - onHand < orderedQty → `SHORTAGE` (라인 출고 지연)
- 노트 지연 전이:
    - ANY LINE `SHORTAGE` 발생 시 Note = `DELAYED`로 즉시 전이하며 이후 변경/완료 요청은 409로 거부

## Preconditions

- Note.status ∈ {PENDING, IN_PROGRESS}
    - 라인이 완료 상태 아님(DONE 아님)
    - 완료 상태 라인 재수정 금지(409)
- 입력 유효성:
    - inspectedQty ≥ 0
    - orderedQty > 0 (불변)

## Main Flow

1) `{ inspectedQty, issue? }`를 입력받는다(검수 기록용).
2) 현재 재고 조회
3) 담당자가 검수 후 지급 가능 수량을 입력
4) onHand와 orderedQty를 비교하여 상태를 `READY` 또는 `DELAYED`로 변경
5) 노트가 첫 갱신이면 `PENDING → IN_PROGRESS`

## Acceptance

- Given onHand ≥ ordered,
  When 업데이트하면,
  Then 라인은 `READY`
- Given onHand < ordered,
  When 업데이트하면,
  Then 라인은 `SHORTAGE`
- Given 어떤 라인이라도 `SHORTAGE`가 되는 순간,
  When 업데이트하면,
  Then 노트는 즉시 `DELAYED`가 되고 이후 라인/노트 변경은 409로 거부됨
- Given 라인이 완료 상태,
  When 업데이트하면,
  Then 409가 반환

## I/O (예시, 세부 구현은 변경 가능)

- PATCH `/v1/shipping/{noteId}/lines/{lineId}`
- Request `{ "inspectedQty": 28, "issue": null }`
- Response: 최신 NoteDetail 스냅샷

## Errors

- 404: note/line 미존재
- 409: 완료 라인 재수정
- 422: 입력 유효성 실패
