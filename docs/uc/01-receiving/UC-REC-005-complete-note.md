# UC-REC-005 입고 완료 & 재고 반영

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Policy

- **All-or-Nothing per Product**: 라인에 문제가 한 건이라도 있으면 **그 라인은 `REJECTED`** (재고 증가 0)
- **`ACCEPTED` 라인만** 재고 증가 대상

## Preconditions

- 노트의 모든 라인이 **`ACCEPTED | REJECTED`** 상태여야 한다. (진행 중인 라인 없음)

## Main Flow

1) 시스템은 `noteId`로 노트를 로드하고 `canComplete()`를 확인한다.
2) `complete()` 수행 시 **`ACCEPTED` 라인만** 제품별 합계를 계산한다(증가 기준: acceptedQty).
   - MVP 정책에서는 부분 수용 미지원 → acceptedQty = orderedQty
3) Inventory에 계산된 합계(acceptedQty 합) 를 **증가** 반영한다.
4) `NoteStatus = COMPLETED_OK | COMPLETED_ISSUE`로 전이한다(REJECTED 라인 존재 시 COMPLETED_ISSUE), 완료 요약을 반환한다.

## Acceptance (GWT)

- **Given** 모든 라인이 `ACCEPTED`이고,  
  **When** 완료를 요청하면,  
  **Then** **`ACCEPTED` 라인 합계만** 재고가 증가하고 노트는 `COMPLETED_OK`가 된다.
- **Given** `REJECTED` 라인이 하나 이상 포함되어 있고,  
  **When** 완료를 요청하면,  
  **Then** **`ACCEPTED` 라인 합계만** 재고가 증가하고 노트는 `COMPLETED_ISSUE`가 된다.
- **Given** `ACCEPTED`/`REJECTED` 외의 상태(예: `PENDING`) 라인이 하나라도 있고,
  **When** 완료를 요청하면,  
  **Then** 409(완료 불가)가 반환된다.

## I/O (Sketch)

- **POST** `/v1/receiving/{noteId}:complete`
- **Response**

```json
{
  "noteStatus": "COMPLETED_OK",
  "completedAt": "2025-01-01T00:00:00Z",
  "increased": {
    "P-1001": 48,
    "P-1002": 40
  },
  "acceptedCount": 2,
  "rejectedCount": 1
}
```

## Implementation Note
- 실제 재고 증가 처리(Inventory 반영)는 도메인 서비스/애플리케이션 서비스 계층에서 수행되며, 본 문서는 결과 스냅샷 형태만 정의한다. 구현은 후속 PR에서 반영된다.

## Errors

- 404: noteId 미존재
- 409: 완료 불가 상태(ACCEPTED/REJECTED 외 상태 포함)
  - Payload(예시):
```json
{
  "status": 409,
  "success": false,
  "message": "입고 완료 불가: 진행 중 라인 존재",
  "data": {
    "noteId": 301,
    "noteStatus": "IN_PROGRESS",
    "problematicLines": [
      { "lineId": 7, "part": { "code": "P-1003", "name": "에어필터" }, "orderedQty": 50, "inspectedQty": 48, "status": "PENDING", "reason": "미검수 라인 존재" }
    ]
  }
}
```


## References
- Policy: [receiving-inspection.md](../../policy/receiving-inspection.md)
- Standards: [exception-and-response.md](../../standards/exception-and-response.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)
