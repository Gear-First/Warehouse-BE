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
- warehouseCode: 창고 코드(문자열)

## I/O (페이지네이션 스케치)

- GET `/v1/receiving/completed?date=YYYY-MM-DD&warehouseCode=WH1&page=0&size=20`
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



### Update (2025-10-30)
- Planned additional query parameters to align with feedback and context docs:
  - dateFrom?, dateTo? (UTC closed interval; when both date and range are provided, the range wins)
  - receivingNo? (partial, case-insensitive)
  - supplierName? (partial, case-insensitive)
  - sort? (baseline allowed fields: noteId, completedAt, status; default noteId asc)
  - Optional warehouse filter remains: warehouseCode?
- New "all" endpoint to be introduced: GET `/v1/receiving/all` with the same pagination and filters. This UC remains focused on done; `/all` will be covered in a new UC or an extension note.
- Response shape remains `ApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>`.


#### Update (2025-10-30 — timestamps on list items)
- Each list item now includes two timestamps:
  - `requestedAt` (UTC ISO-8601): when HQ registered the request.
  - `completedAt` (UTC ISO-8601): completion time; populated for done items.
- Example item snippet:
```json
{
  "noteId": 201,
  "receivingNo": "IN/WH1/20251030/001",
  "supplierName": "ABC공장",
  "itemKinds": 2,
  "totalQty": 80,
  "status": "COMPLETED_OK",
  "warehouseCode": "WH1",
  "requestedAt": "2025-10-29T23:50:00Z",
  "completedAt": "2025-10-30T03:10:00Z"
}
```
- Filtering (phase 1): `date`/`dateFrom`/`dateTo` apply to `requestedAt` by default (range wins; closed interval; UTC). A later PR may add `dateField` to switch to `completedAt`.


#### Update (2025-11-05 — expectedReceiveDate & KST window)
- List item now includes `expectedReceiveDate` (ISO-8601 KST). If not provided on create, server defaults to `requestedAt + 2 days`.
- Date filters (`date`, `dateFrom`, `dateTo`) are interpreted by KST day windows while timestamps are stored in UTC.
- Example item fields now include:
```json
{
  "noteId": 201,
  "receivingNo": "IN-서울-20251102-001",
  "supplierName": "HanKorea",
  "itemKinds": 2,
  "totalQty": 40,
  "status": "COMPLETED_OK",
  "warehouseCode": "서울",
  "requestedAt": "2025-11-02T09:00:00+09:00",
  "expectedReceiveDate": "2025-11-04T09:00:00+09:00",
  "completedAt": "2025-11-03T16:00:00+09:00"
}
```
