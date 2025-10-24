# UC-SHP-001 오늘 출고 대기 목록 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Intent

오늘 날짜 기준 `PENDING | IN_PROGRESS` 상태의 출고 납품서 목록을 조회

## Preconditions

- 없음(읽기 UC)

## Main Flow

1) 시스템은 날짜(기본: 오늘)로 필터링
2) 상태 ∈ {PENDING, IN_PROGRESS} 인 출고 노트를 조회
3) 요약 DTO로 투영하여 반환

## Acceptance (GWT)

- Given 오늘 날짜,
  When 대기 목록을 조회하면,
  Then `PENDING | IN_PROGRESS` 노트만 noteId 오름차순으로 반환
- Given 결과가 없을 때,
  When 조회하면,
  Then 빈 배열을 반환

## Filters
- date: YYYY-MM-DD (기본: 오늘)
- warehouseId: 창고 식별자(멀티 창고 적용)
- page: 기본 0
- size: 기본 20

## I/O (페이지네이션 스케치)

- GET `/v1/shipping/notDone?date=YYYY-MM-DD&warehouseId=WH1&page=0&size=20`
- Response
```
{
  "items": [
    { "noteId": 501, "shippingNo": "OUT/WH1/20251024/001", "branchName": "강남대리점", "itemKinds": 3, "totalQty": 90, "status": "PENDING" }
  ],
  "page": 0,
  "size": 20,
  "total": 0
}
```

## Errors

- 400: 잘못된 날짜 포맷

## Notes

- 선택적 필터 사전 고지: `dateFrom`, `dateTo`, `keyword`, `status[]` (현재 미구현)
- 멀티 창고: warehouseId 필터를 본 UC에 적용(문서 기준)

## References
- Policy: [shipping-fulfillment.md](../../policy/shipping-fulfillment.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)
