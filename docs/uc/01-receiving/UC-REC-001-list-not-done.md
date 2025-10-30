# UC-REC-001 입고 대기 목록 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨  

## Intent

오늘 날짜 기준 `PENDING | IN_PROGRESS` 상태의 입고 납품서 목록을 조회

## Filters
- date: YYYY-MM-DD (기본: 오늘)
- warehouseCode: 창고 코드(문자열)

## Acceptance (GWT)

- Given 오늘 날짜,
  When 대기 목록을 조회하면,
  Then `PENDING | IN_PROGRESS` 납품서만 noteId 오름차순으로 반환
- Given 결과가 없을 때,
  When 조회하면,
  Then 빈 배열을 반환

## I/O (페이지네이션 스케치)

- GET `/v1/receiving/notDone?date=YYYY-MM-DD&warehouseCode=WH1&page=0&size=20`
- Response
```
{
  "items": [
    { "noteId": 101, "supplierName": "ABC공장", "itemKinds": 2, "totalQty": 80, "status": "PENDING" }
  ],
  "page": 0,
  "size": 20,
  "total": 34
}
```

## Errors

- 400: 잘못된 날짜/파라미터 포맷

## Notes

- 선택적 필터 사전 고지: `dateFrom`, `dateTo`, `keyword`, `status[]` (현재 미구현)
- 멀티 창고: warehouseCode 필터를 본 UC에 적용(문서 기준)
- ‘재입고 내역’(재입고 이력) 항목은 이번 라운드 Defer: 별도 UC는 후속 PR에서 제공

## References
- Policy: [receiving-inspection.md](../../policy/receiving-inspection.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)



### Update (2025-10-30)
- Planned additional query parameters to align with feedback and context docs:
  - dateFrom?, dateTo? (UTC closed interval; when provided with date, the range takes precedence)
  - receivingNo? (partial, case-insensitive)
  - supplierName? (partial, case-insensitive)
  - sort? (baseline allowed fields: noteId, completedAt, status; default noteId asc)
- Optional warehouse filter remains: warehouseCode?
- New "all" endpoint to be introduced: GET `/v1/receiving/all` with the same pagination and filters. This UC remains focused on not-done; `/all` will be covered in a new UC or an extension note.
- Response shape remains `ApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>`.


#### Update (2025-10-30 — timestamps on list items)
- Each list item now includes two timestamps:
  - `requestedAt` (UTC ISO-8601): when HQ registered the request.
  - `completedAt` (UTC ISO-8601 or null): completion time; null for not-done items.
- Example item snippet:
```json
{
  "noteId": 101,
  "receivingNo": "IN/WH1/20251030/001",
  "supplierName": "ABC공장",
  "itemKinds": 2,
  "totalQty": 80,
  "status": "PENDING",
  "warehouseCode": "WH1",
  "requestedAt": "2025-10-29T23:50:00Z",
  "completedAt": null
}
```
- Filtering (phase 1): `date`/`dateFrom`/`dateTo` apply to `requestedAt` by default (range wins; closed interval; UTC).
