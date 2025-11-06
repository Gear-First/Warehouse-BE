# UC-PART-007 Parts — Integrated Search (List Parts with Category & Car Models)

- Status: Draft
- Date: 2025-11-05
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
Parts 통합 조회를 제공한다. Part 기본 정보와 함께 Category, Car Models 정보까지 한 번에 반환한다. 도메인 로컬(Querydsl) 동적 쿼리를 사용하며, 정렬 화이트리스트와 KST I/O(UTC 저장) 정책을 따른다.

## Preconditions
- `page >= 0`, `1 <= size <= 200` (표준 페이지 범위)
- 정렬 키는 화이트리스트만 허용한다.

## Endpoint
- GET `/api/v1/parts/search`

## Query Parameters
- Pagination
  - `page` (int, default 0)
  - `size` (int, default 20, max 200)
- Sorting (whitelist, multi-sort allowed; default: `code,asc`)
  - `sort=code|name|price|createdAt|updatedAt[,asc|desc]`
- Unified Search
  - `q` (string, optional): case-insensitive contains over `part.code | part.name | category.name | carModel.name`
    - Operators inside `q` (optional): `id:123`(exact), `code:P-1001`, `name:패드`, `category:제동`, `model:아반떼`
    - Normalization: trim, collapse spaces, lowercase matching
    - Precedence: combined with all other filters using logical AND
- Filters (all optional unless noted)
  - `partId` (long): exact; recommended when targeting a specific id (no ambiguity)
  - `partCode` (string): exact (case-insensitive; normalize to uppercase)
  - `partName` (string): contains, case-insensitive
  - `categoryId` (long) | `categoryName` (string, contains, case-insensitive)
  - `carModelId` (long) | `carModelName` (string, contains, case-insensitive)
  - `enabled` (boolean; discontinued는 enabled 변경을 위한 api일 뿐이다)

## Response
- 200 OK → `ApiResponse.success(PageEnvelope<PartIntegratedItemDto>)`
- `PartIntegratedItemDto` (integrated view)
  - Part: `id: long`, `code: string`, `name: string`, `price: int`, `imageUrl: string?`, `safetyStockQty: int?`, `enabled: boolean`, `discontinued?: boolean`, `createdAt: string(KST)`, `updatedAt: string(KST)`
  - Category: `{ id: long, name: string }`
  - CarModels: `[{ id: long, name: string }]`

## Examples
- Requests
```
GET /api/v1/parts/search?q=브레이&categoryName=제동&sort=price,desc&sort=name,asc&page=0&size=10
GET /api/v1/parts/search?partCode=P-AB123&enabled=true
GET /api/v1/parts/search?partName=패드&carModelName=아반떼&sort=code,asc
GET /api/v1/parts/search?q=model:아반떼 category:제동
```
- Response (truncated)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 101,
        "code": "P-AB123",
        "name": "브레이크 패드",
        "price": 32000,
        "imageUrl": "/parts/AB123.jpg",
        "safetyStockQty": 20,
        "enabled": true,
        "createdAt": "2025-11-05T10:12:00+09:00",
        "updatedAt": "2025-11-05T13:45:00+09:00",
        "category": { "id": 10, "name": "제동" },
        "carModels": [ { "id": 7, "name": "아반떼" } ]
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 123,
    "totalPages": 13
  }
}
```

## Errors
- 400: 페이지/사이즈 범위 오류, 파라미터 타입 오류
- 200 + fallback 정렬: 화이트리스트 외 정렬 키는 무시(또는 팀 정책에 따라 400)

## Notes
- Timezone: API I/O는 KST(+09:00) ISO-8601, 서버 내부는 UTC 저장/연산
- Querydsl: projection 기반, N+1 방지, 무거운 조인 제외한 별도 `countQuery` 권장
