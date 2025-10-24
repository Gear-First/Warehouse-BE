# UC-REC-002 완료 입고 목록 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Intent

오늘 날짜 기준 `COMPLETED_OK | COMPLETED_ISSUE` 상태의 입고 납품서 목록을 조회

## Filters
- date: YYYY-MM-DD (기본: 오늘)
- warehouseId: 창고 식별자(멀티 창고 적용)

## I/O (페이지네이션 스케치)

- GET `/v1/receiving/completed?date=YYYY-MM-DD&warehouseId=WH1&page=0&size=20`
- Response
```
{
  "items": [
    { "noteId": 201, "supplierName": "ABC공장", "itemKinds": 2, "totalQty": 80, "status": "COMPLETED_OK", "completedAt": "2025-10-24T10:00:00Z" }
  ],
  "page": 0,
  "size": 20,
  "total": 15
}
```

## Errors
- 400: 잘못된 날짜/파라미터 포맷

## Notes
- 선택적 필터 사전 고지: `dateFrom`, `dateTo`, `keyword`, `status[]` (현재 미구현)
- 멀티 창고: warehouseId 필터를 본 UC에 적용(문서 기준)
- ‘재입고 내역’(재입고 이력) 항목은 이번 라운드 Defer: 별도 UC는 후속 PR에서 제공

## References
- Policy: [receiving-inspection.md](../../policy/receiving-inspection.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)
