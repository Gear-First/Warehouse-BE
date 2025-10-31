# UC-SHP-004 출고 라인 진행 업데이트 (READY/DELAYED 판정)

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Policy

- 라인 업데이트 입력은 수량만을 받으며, 상태는 서버가 도출한다.
  - 입력: `pickedQty`(피킹 수량)
  - 제약: `0 ≤ pickedQty ≤ orderedQty`
- 상태 판정(서버 도출, allocation 제거 모델):
  - `remainingNeeded = orderedQty − pickedQty`
  - `remainingNeeded > onHand` → `SHORTAGE`
  - `pickedQty == orderedQty` → `READY`
  - 그 외 → `PENDING`
- 노트 지연 전이(즉시):
  - 어떤 라인이든 `SHORTAGE`가 되는 순간 Note = `DELAYED`로 즉시 전이
  - `DELAYED` 이후에는 라인/노트 변경 및 완료 요청을 409로 거부한다

## Preconditions

- Note.status ∈ {PENDING, IN_PROGRESS}
- 대상 라인이 완료 상태가 아님(READY 판정으로 동결된 것은 아님)
- Note.status ∈ {DELAYED, COMPLETED} 인 경우 업데이트 불가(409)
- 입력 유효성: `0 ≤ pickedQty ≤ allocatedQty ≤ orderedQty`

## Main Flow

1) 클라이언트는 `{ allocatedQty, pickedQty }`를 전송한다.
2) 서버는 제약을 검증하고 라인 상태를 `READY` 또는 `SHORTAGE`로 도출한다.
3) 라인이 `SHORTAGE`가 되는 경우 노트는 즉시 `DELAYED`로 전이되며 이후 모든 변경/완료는 409로 차단된다.
4) 노트가 첫 갱신이면 `PENDING → IN_PROGRESS`로 전이한다.
5) 저장 후 최신 NoteDetail 스냅샷을 반환한다.

## Acceptance

- Given `pickedQty ≤ allocatedQty ≤ orderedQty`,
  When 업데이트하면,
  Then 라인 상태는 조건에 따라 `READY` 또는 `SHORTAGE`로 서버가 도출된다.
- Given 어떤 라인이라도 `SHORTAGE`가 되는 순간,
  When 업데이트하면,
  Then 노트는 즉시 `DELAYED`가 되고 이후 라인/노트 변경 및 완료는 409로 거부된다.
- Given 노트가 `DELAYED` 또는 `COMPLETED` 상태,
  When 라인 업데이트를 요청하면,
  Then 409가 반환된다.
- Given 입력 범위 위반(pickedQty > allocatedQty 등),
  When 라인 업데이트를 요청하면,
  Then 400이 반환된다.

## I/O (예시, 세부 구현은 변경 가능)

- PATCH `/v1/shipping/{noteId}/lines/{lineId}`
- Request `{ "allocatedQty": 3, "pickedQty": 3 }`
- Response: 최신 NoteDetail 스냅샷

## Errors

- 404: note/line 미존재
- 409: DELAYED/COMPLETED 노트 변경 시도, 완료된(동결된) 라인 재수정 시도
- 400: 입력 유효성 실패(수량 범위 위반)
