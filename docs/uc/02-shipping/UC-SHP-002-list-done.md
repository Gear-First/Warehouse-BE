# UC-SHP-002 오늘 완료 출고 목록 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Intent

오늘 날짜 기준 `COMPLETED | DELAYED` 상태의 출고 납품서(Note) 목록을 조회

## Filters
- date: YYYY-MM-DD (기본: 오늘)
- warehouseCode: 창고 코드(문자열)

## I/O (페이지네이션 스케치)

- GET `/v1/shipping/completed?date=YYYY-MM-DD&warehouseCode=WH1&page=0&size=20`
- Response
```
{
  "items": [
    { "noteId": 701, "shippingNo": "OUT/WH1/20251024/002", "branchName": "강남대리점", "itemKinds": 3, "totalQty": 90, "status": "COMPLETED", "completedAt": "2025-10-24T11:00:00Z" }
  ],
  "page": 0,
  "size": 20,
  "total": 34
}
```

## Acceptance (요약)
- 완료 목록은 `COMPLETED | DELAYED` 상태만 포함한다
- 잘못된 날짜/파라미터는 400을 반환한다

## Errors
- 400: 잘못된 날짜/파라미터 형식

## Notes
- 정책: ANY LINE `SHORTAGE` 발생 시 Note는 `DELAYED` (완료 불가). 모든 라인이 `READY`일 때만 `COMPLETED` 가능
- 관련 정책: [shipping-fulfillment.md](../../policy/shipping-fulfillment.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)



### Update (2025-10-30)
- Planned additional query parameters to align with feedback and context docs:
  - dateFrom?, dateTo? (UTC closed interval; when both date and range are provided, the range wins)
  - shippingNo? (partial, case-insensitive)
  - branchName? (partial, case-insensitive)
  - sort? (baseline allowed fields: noteId, completedAt, status; default noteId asc)
- Optional warehouse filter remains: warehouseCode?
- New "all" endpoint to be introduced: GET `/v1/shipping/all` with the same pagination and filters. This UC remains focused on done; `/all` will be covered in a new UC or an extension note.
- Response shape remains `ApiResponse<PageEnvelope<ShippingNoteSummaryResponse>>`.


#### Update (2025-10-30 — timestamps on list items)
- Each list item now includes two timestamps:
  - `requestedAt` (UTC ISO-8601): when HQ registered the request.
  - `completedAt` (UTC ISO-8601): completion time; for `DELAYED`, this records the delay decision time.
- Example item snippet:
```json
{
  "noteId": 701,
  "shippingNo": "OUT/WH1/20251024/002",
  "branchName": "강남대리점",
  "itemKinds": 3,
  "totalQty": 90,
  "status": "DELAYED",
  "warehouseCode": "WH1",
  "requestedAt": "2025-10-24T00:10:00Z",
  "completedAt": "2025-10-24T11:00:00Z"
}
```
- Filtering (phase 1): `date`/`dateFrom`/`dateTo` apply to `requestedAt` by default (range wins; closed interval; UTC). A later PR may add `dateField` to switch to `completedAt`.
