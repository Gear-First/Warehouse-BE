# UC-SHP-003 출고 납품서 상세 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `25-10-20`  
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
  "noteId": 501, "supplierName": "...", "status": "IN_PROGRESS",
  "itemKinds": 3, "totalQty": 90,
  "lines": [
    { "lineId": 1, "product": { "productNo":"P-1001","name":"...","imgUrl":"/img" },
      "orderedQty": 30, "inspectedQty": 28,
      "status": "READY|DELAYED|DONE_ISSUE|IN_PROGRESS|PENDING" }
  ]
}
```
