# UC-SHP-006 출고 목록 통합 조회 (도메인 내 통합)

> Status: Draft  
> Date: 2025-10-30  
> Deciders: 현희찬

> Non-Authoritative!! 정책/표준/CRC/코드/테스트가 우선. 본 UC는 그에 맞춰 갱신됨.

## Intent
- 기존 `/not-done`, `/done`, (예정 `/all`)을 하나의 엔드포인트로 통합 제공하되, 기존 엔드포인트는 유지합니다.
- 모든 목록 항목에서 `requestedAt`, `completedAt` 두 타임스탬프를 노출합니다. (DELAYED 시점은 `completedAt`에 기록)

## Endpoint
- GET `/api/v1/shipping/notes`

## Query Parameters
- status: `not-done | done | all` (기본: `not-done`)
- page: 기본 0, size: 기본 20 (1..100)
- sort: 허용 필드(1차): `noteId, requestedAt, completedAt, status` (기본 `noteId,asc`)
- warehouseCode?: 선택적 창고 필터
- 날짜 필터(1차): `date` 또는 `dateFrom/dateTo`
  - 기본 적용 대상: `requestedAt` (범위 제공 시 범위 우선; 폐구간, UTC)
  - (후속) `dateField=requestedAt|completedAt` 추가 예정 → 제공 시 해당 필드 기준 필터링
- 고급 필터(1차): `shippingNo?, branchName?` (부분 일치, 대소문자 무시; 다중 파라미터 AND 매칭)

## Response (PageEnvelope)
- Shape: `ApiResponse<PageEnvelope<ShippingNoteSummaryResponse>>`
- 목록 항목 공통 필드(요약):
  - `noteId`, `shippingNo?`, `branchName?`, `itemKinds`, `totalQty`, `status`, `warehouseCode`
  - `requestedAt` (UTC ISO-8601), `completedAt` (UTC ISO-8601|null; DELAYED 시 기록됨)

### Example
```
GET /api/v1/shipping/notes?status=all&warehouseCode=WH1&dateFrom=2025-10-01&dateTo=2025-10-31&page=0&size=20&sort=requestedAt,desc
{
  "items": [
    {
      "noteId": 701,
      "shippingNo": "OUT/WH1/20251024/002",
      "branchName": "강남대리점",
      "itemKinds": 3,
      "totalQty": 90,
      "status": "DELAYED",
      "requestedAt": "2025-10-24T00:10:00Z",
      "completedAt": "2025-10-24T11:00:00Z"
    },
    {
      "noteId": 705,
      "shippingNo": "OUT/WH1/20251024/006",
      "branchName": "서초대리점",
      "itemKinds": 2,
      "totalQty": 36,
      "status": "IN_PROGRESS",
      "warehouseCode": "WH1",
      "requestedAt": "2025-10-24T01:20:00Z",
      "completedAt": null
    }
  ],
  "page": 0,
  "size": 20,
  "total": 2
}
```

## Acceptance (요약)
- [ ] status 파라미터로 `not-done|done|all` 범위를 전환할 수 있다(기본 `not-done`).
- [ ] 모든 항목에 `requestedAt`과 `completedAt`이 존재한다(미완료 시 `completedAt=null`; `DELAYED`는 지연 시각으로 채워짐).
- [ ] 날짜 필터는 1차로 `requestedAt`에 적용된다(범위 우선, 폐구간, UTC). 
- [ ] 정렬은 최소 `noteId, requestedAt, completedAt, status`를 지원한다(기본 `noteId,asc`).
- [ ] 기존 `/not-done`, `/done`, (예정 `/all`) 응답 항목과 동일한 형태를 유지한다.

## Notes
- 기존 엔드포인트는 호환성 유지를 위해 유지하며, 내부적으로 본 통합 핸들러로 위임해 드리프트를 방지할 수 있습니다.
- (후속) `dateField` 파라미터 추가로 `completedAt` 기준 필터링을 선택 가능하게 합니다.

## References
- Context: `docs/context/analysis-temp.md` §14
- Standards: `docs/standards/exception-and-response.md` (Timestamp policy)
- Existing UCs: UC-SHP-001, UC-SHP-002
