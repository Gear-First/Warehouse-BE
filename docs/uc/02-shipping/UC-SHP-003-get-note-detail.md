# UC-SHP-003 출고 납품서 상세 조회

- Status: Accepted
- Date: 2025-10-20(작성일)
- Deciders: 현희찬

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
