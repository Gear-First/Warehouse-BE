# UC-REC-006 입고 목록 통합 조회 (도메인 내 통합)

> Status: Draft  
> Date: 2025-10-30  
> Deciders: 현희찬

> Non-Authoritative!! 정책/표준/CRC/코드/테스트가 우선. 본 UC는 그에 맞춰 갱신됨.

## Intent
- 기존 `/not-done`, `/done`, (예정 `/all`)을 하나의 엔드포인트로 통합 제공하되, 기존 엔드포인트는 유지합니다.
- 모든 목록 항목에서 `requestedAt`, `completedAt` 두 타임스탬프를 노출합니다.

## Endpoint
- GET `/api/v1/receiving/notes`

## Query Parameters
- status: `not-done | done | all` (기본: `not-done`)
- page: 기본 0, size: 기본 20 (1..100)
- sort: 허용 필드(1차): `noteId, requestedAt, completedAt, status` (기본 `noteId,asc`)
- warehouseCode?: 선택적 창고 필터
- 날짜 필터(1차): `date` 또는 `dateFrom/dateTo`
  - 기본 적용 대상: `requestedAt` (범위 제공 시 범위 우선; 폐구간, UTC)
  - (후속) `dateField=requestedAt|completedAt` 추가 예정 → 제공 시 해당 필드 기준 필터링
- 고급 필터(1차): `receivingNo?, supplierName?` (부분 일치, 대소문자 무시; 다중 파라미터 AND 매칭)

## Response (PageEnvelope)
- Shape: `ApiResponse<PageEnvelope<ReceivingNoteSummaryResponse>>`
- 목록 항목 공통 필드(요약):
  - `noteId`, `receivingNo?`, `supplierName?`, `itemKinds`, `totalQty`, `status`
  - `requestedAt` (UTC ISO-8601), `completedAt` (UTC ISO-8601|null)

### Example
```
GET /api/v1/receiving/notes?status=all&warehouseCode=WH1&dateFrom=2025-10-01&dateTo=2025-10-31&page=0&size=20&sort=requestedAt,desc
{
  "items": [
    {
      "noteId": 201,
      "receivingNo": "IN/WH1/20251030/001",
      "supplierName": "ABC공장",
      "itemKinds": 2,
      "totalQty": 80,
      "status": "COMPLETED_OK",
      "requestedAt": "2025-10-29T23:50:00Z",
      "completedAt": "2025-10-30T03:10:00Z"
    },
    {
      "noteId": 205,
      "receivingNo": "IN/WH1/20251030/005",
      "supplierName": "XYZ공장",
      "itemKinds": 1,
      "totalQty": 20,
      "status": "IN_PROGRESS",
      "requestedAt": "2025-10-30T02:00:00Z",
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
- [ ] 모든 항목에 `requestedAt`과 `completedAt`이 존재한다(미완료 시 `completedAt=null`).
- [ ] 날짜 필터는 1차로 `requestedAt`에 적용된다(범위 우선, 폐구간, UTC). 
- [ ] 정렬은 최소 `noteId, requestedAt, completedAt, status`를 지원한다(기본 `noteId,asc`).
- [ ] 기존 `/not-done`, `/done`, (예정 `/all`) 응답 항목과 동일한 형태를 유지한다.

## Notes
- 기존 엔드포인트는 호환성 유지를 위해 유지하며, 내부적으로 본 통합 핸들러로 위임해 드리프트를 방지할 수 있습니다.
- (후속) `dateField` 파라미터 추가로 `completedAt` 기준 필터링을 선택 가능하게 합니다.

## References
- Context: `docs/context/analysis-temp.md` §14
- Standards: `docs/standards/exception-and-response.md` (Timestamp policy)
- Existing UCs: UC-REC-001, UC-REC-002
