# UC-SHP-003 출고 납품서 상세 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Intent

출고 검수 진행 화면을 위한 헤더/라인 상세 조회

## I/O

- GET `/v1/shipping/{noteId}`
- Response

```json
{
  "noteId": 501, "shippingNo": "OUT/WH1/20251024/003", "customerName": "...", "status": "IN_PROGRESS",
  "itemKinds": 3, "totalQty": 90,
  "requestedAt": "2025-10-24T08:00:00Z",  
  "expectedShipDate": "2025-10-25",      
  "shippedAt": null,                       
  "assignee": { "name": "홍길동", "dept": "물류", "phone": "010-0000-0000" },
  "remark": null,
  "lines": [
    { "lineId": 1, "product": { "productNo":"P-1001","name":"...","unit":"EA","imgUrl":"/img" },
      "orderedQty": 30, "allocatedQty": 30, "pickedQty": 28,
      "status": "READY|SHORTAGE|IN_PROGRESS|PENDING" }
  ]
}
```

> Note: requestedAt/expectedShipDate/shippedAt/assignee/remark는 현 단계에서 문서에만 반영된 ‘미구현 필드’이며, 후속 PR에서 코드 반영 예정

## References
- Policy: [shipping-fulfillment.md](../../policy/shipping-fulfillment.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)
