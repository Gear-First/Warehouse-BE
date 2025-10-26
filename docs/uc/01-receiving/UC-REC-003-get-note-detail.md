# UC-REC-003 입고 납품서 상세 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Intent

검수 화면 진입을 위해 납품서 헤더/라인 상세를 조회

## Preconditions

- noteId 유효

## Main Flow

1) noteId로 납품서를 조회
2) 헤더 + 라인(제품, 수량, 상태)을 반환

## Acceptance

- Given 유효한 noteId,
  When 상세를 조회하면,
  Then 헤더와 라인 배열 반환
- Given 존재하지 않는 noteId,
  When 조회하면,
  Then 404 반환

## I/O

- GET `/v1/receiving/{noteId}`
- Response(예시): 공통 응답 제외(공통 응답은 관련 문서)
    - standards/[exception-and-response.md](../../standards/exception-and-response.md)

```json
{
  "noteId": 101,
  "receivingNo": "IN/WH1/20251024/001",
  "warehouse": { "id": "WH1", "name": "본사창고" },
  "supplier": { "name": "ABC공장", "code": "SUP-001" },
  "requestedAt": "2025-10-24T08:00:00Z",
  "expectedReceiveDate": "2025-10-25",
  "receivedAt": null,
  "inspector": { "name": "홍길동", "dept": "물류", "phone": "010-0000-0000" },
  "remark": null,
  "status": "IN_PROGRESS",
  "itemKinds": 3,
  "totalQty": 120,
  "lines": [
    {
      "lineId": 1,
      "lotNo": "LOT-2401",
      "part": { "code": "P-1001", "name": "오일필터" },
      "orderedQty": 50,
      "inspectedQty": 48,
      "status": "PENDING|ACCEPTED|REJECTED",
      "lineRemark": null
    }
  ]
}
```

## Errors
- 404: noteId 미존재

## References
- Policy: [receiving-inspection.md](../../policy/receiving-inspection.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)
