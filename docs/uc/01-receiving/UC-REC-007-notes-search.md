# UC-REC-007 Receiving — Search Notes (Integrated Search)

- Status: Draft
- Date: 2025-11-05
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
Receiving 노트의 상태/번호/업체/날짜/창고 등 복합 조건 검색을 제공한다. 날짜는 KST 로컬일 해석(범위 우선), 정렬은 화이트리스트 기반이다.

## Preconditions
- `page >= 0`, `1 <= size <= 200`
- 정렬 키는 화이트리스트만 허용한다.
- 날짜 필터 충돌 시 범위(`dateFrom/dateTo`)가 단일 `date`보다 우선한다.

## Endpoint
- GET `/api/v1/receiving/notes?status=not-done|done|all`

## Query Parameters
- Pagination
  - `page` (int, default 0)
  - `size` (int, default 20, max 200)
- Sorting (whitelist; default: `requestedAt,desc`)
  - `sort=requestedAt|completedAt|receivingNo|status|supplierName|warehouseCode[,asc|desc]`
- Unified Search
  - `q` (string, optional): case-insensitive contains over `receivingNo | supplierName | warehouseCode`
    - Normalization: trim, collapse spaces, lowercase matching
    - Precedence: combined with all other filters using logical AND
- Filters (all optional; AND-combined)
  - `status` (enum): `not-done | done | all`
  - Dates (KST local day): `date` or `dateFrom & dateTo` (range wins; closed interval)
  - `receivingNo` (string): exact
  - `productCode` (string): contains, case-insensitive
  - `supplierName` (string): contains, case-insensitive
  - `warehouseCode` (string): exact, as-is compare (no normalization)

## Response
- 200 OK → `ApiResponse.success(PageEnvelope<ReceivingNoteSummaryDto>)`
- ReceivingNoteSummaryDto (list item)
  - `noteId: long`, `receivingNo: string`, `warehouseCode: string`, `status: string`,
    `requestedAt: string(KST)`, `completedAt: string(KST|null)`, `expectedReceiveDate: string(KST|null)`,
    `supplierName: string`

## Examples
- One-string integrated search (supplier name):
```
GET /api/v1/receiving/notes?status=done&q=한빛&dateFrom=2025-10-01&dateTo=2025-10-31&sort=completedAt,desc&page=0&size=20
```
- One-string with warehouse hint (as-is compare) + explicit warehouse filter:
```
GET /api/v1/receiving/notes?status=not-done&q=WH-01&warehouseCode=WH-01&sort=requestedAt,desc&page=0&size=20
```
- Exact receiving number:
```
GET /api/v1/receiving/notes?receivingNo=IN-WH01-20251105-001
```
- Product code contains (case-insensitive):
```
GET /api/v1/receiving/notes?productCode=P-AB123&sort=requestedAt,desc&page=0&size=20
```
- Supplier name contains filter (explicit):
```
GET /api/v1/receiving/notes?supplierName=한빛&date=2025-11-05&sort=receivingNo,asc&page=0&size=20
```

## Errors
- 400: 페이지/사이즈 범위 오류
- 200 + fallback 정렬: 무효 정렬 키는 무시(또는 팀 정책에 따라 400)

## Notes
- Timezone: API I/O KST(+09:00), 서버 UTC 저장/연산
- Date: `dateFrom > dateTo`인 경우 자동으로 값을 교환(auto-swap)하여 처리한다. `date`와 `dateFrom/dateTo`가 함께 오면 범위가 우선한다.
- Query: 향후 Querydsl 도입 시 동적 Predicate로 구현, 화이트리스트 정렬을 `OrderSpecifier`로 매핑
