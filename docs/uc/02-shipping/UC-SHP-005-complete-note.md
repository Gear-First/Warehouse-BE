# UC-SHP-005 출고 완료 (READY 전제, 지연 시 완료 불가)

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Policy

- 모든 라인이 `READY`일 때만 완료 가능 → Note=`COMPLETED`
- 라인 중 하나라도 `SHORTAGE`이면 즉시 Note=`DELAYED`로 전이되며, 완료 요청은 409로 거부(정책상 완료 불가)
- 재고 차감 시점: `COMPLETED` 전이 시, 각 READY 라인의 shippedQty 합 기준으로 차감

## Preconditions

- Note.status ∈ {IN_PROGRESS}
- 모든 라인이 `READY` 상태여야 함(그 외 상태 존재 시 완료 불가)

## Main Flow

1) 시스템은 noteId로 노트를 조회하고 완료 가능 여부를 점검한다.
2) 모든 라인이 READY가 아닌 경우 409를 반환한다(또는 SHORTAGE 존재 시 이미 Note=DELAYED 상태이므로 409).
3) 완료 가능하면 완료 시각 기록과 함께 차감 기준 합계를 계산하고 Note=`COMPLETED`로 전이한다.

## Acceptance

- Given 모든 라인이 READY,
  When 완료를 요청하면,
  Then Note는 `COMPLETED`가 되고 응답에 `completedAt`과 `totalShippedQty`가 포함된다.
- Given 하나라도 SHORTAGE가 포함된 경우,
  When 완료를 요청하면,
  Then 409가 반환되고 Note는 `DELAYED` 상태로 남는다(완료 불가).
- Given READY가 아닌 상태(PENDING/IN_PROGRESS) 라인이 포함된 경우,
  When 완료를 요청하면,
  Then 409가 반환된다.

## I/O (Sketch)

- POST `/v1/shipping/{noteId}:complete`
- Response (성공 사례)

```json
{
  "completedAt": "2025-10-24T11:00:00Z",
  "totalShippedQty": 90
}
```

## Errors

- 404: noteId 미존재
- 409: 완료 불가 상태(READY 미충족 또는 SHORTAGE 포함)
  - Payload(예시):
```json
{
  "status": 409,
  "success": false,
  "message": "출고 완료 불가: SHORTAGE 포함",
  "data": {
    "noteId": 602,
    "noteStatus": "DELAYED",
    "problematicLines": [
      { "lineId": 3, "part": { "code": "P-2002", "name": "브레이크패드" }, "orderedQty": 20, "allocatedQty": 20, "pickedQty": 12, "status": "SHORTAGE", "reason": "onHand<ordered" }
    ]
  }
}
```


## References
- Policy: [shipping-fulfillment.md](../../policy/shipping-fulfillment.md)
- Standards: [exception-and-response.md](../../standards/exception-and-response.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)
