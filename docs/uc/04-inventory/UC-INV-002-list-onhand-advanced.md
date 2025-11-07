# UC-INV-002 Inventory — List On-hand (Advanced Search)

- Status: Draft
- Date: 2025-11-05
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
재고 On-hand를 부품코드/이름/창고코드 등으로 통합 검색하고, 정렬/페이지네이션을 적용하여 조회한다. 안전재고 비교 필드를 응답에 포함할 수 있다.

## Preconditions
- `page >= 0`, `1 <= size <= 200`
- 정렬 키는 화이트리스트만 허용한다.
- 수량 범위(`minQty`, `maxQty`)는 `minQty ≤ maxQty` 여야 한다.

## Endpoint
- GET `/api/v1/inventory/on-hand`

## Query Parameters
- Pagination
  - `page` (int, default 0)
  - `size` (int, default 20, max 200)
- Sorting (whitelist; default: `updatedAt,desc`)
  - `sort=warehouseCode|partCode|partName|onHandQty|supplierName|updatedAt|price|priceTotal[,asc|desc]`
- Unified Search
  - `q` (string, optional): case-insensitive contains over `part.code | part.name | supplierName | warehouseCode`
    - Normalization: trim, collapse spaces, lowercase matching
    - Precedence: combined with all other filters using logical AND
- Filters (all optional; AND-combined)
  - `partId` (long): exact
  - `partCode` (string): contains, case-insensitive
  - `partName` (string): contains, case-insensitive
  - `warehouseCode` (string): exact, as-is compare (no normalization; may be non-English)
  - `supplierName` (string): contains, case-insensitive
  - `minQty`, `maxQty` (int)

## Response
- 200 OK → `ApiResponse.success(PageEnvelope<OnHandSummaryDto>)`
- `OnHandSummaryDto`
  - `warehouseCode: string`, `partId: long`, `partCode: string`, `partName: string`, `onHandQty: int`, `supplierName?: string`,
    `updatedAt: string(KST)`, `safetyStockQty?: int`, `lowStock?: boolean`, `price: int`, `priceTotal: int(price*onHandQty)`

## Examples
```
GET /api/v1/inventory/on-hand?q=패드&sort=updatedAt,desc&page=0&size=20
GET /api/v1/inventory/on-hand?q=WH-01&warehouseCode=WH-01&sort=onHandQty,asc&page=0&size=20
GET /api/v1/inventory/on-hand?partCode=P-AB123&sort=onHandQty,asc&page=0&size=20
```

## Errors
- 400: 페이지/사이즈 범위 오류, 수량 범위 역전(`minQty > maxQty`)
- 200 + fallback 정렬: 무효 정렬 키는 무시(또는 팀 정책에 따라 400)

## Notes
- Timezone: API I/O KST(+09:00), 서버 UTC 저장/연산
- Query: Join 시 N+1 회피(필요 시 LEFT JOIN + projection), 카운트 쿼리는 가벼운 형태 권장
