# UC-PCT-001 Parts — List Categories (Search)

- Status: Draft
- Date: 2025-11-06
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
부품 카테고리 목록을 검색·정렬·페이지네이션하여 조회한다. 통합 문자열 검색(`q`)을 지원하며, 개별 필터와 AND 결합된다. 정렬 화이트리스트와 KST I/O(UTC 저장) 정책을 따른다.

## Preconditions
- `page >= 0`, `1 <= size <= 200`
- 정렬 키는 화이트리스트만 허용한다.

## Endpoint
- GET `/api/v1/parts/categories`

## Query Parameters
- Pagination
  - `page` (int, default 0)
  - `size` (int, default 20, max 200)
- Sorting (whitelist; default: `name,asc`)
  - `sort=name|createdAt|updatedAt|partsCount[,asc|desc]`
- Unified Search
  - `q` (string, optional): case-insensitive contains over `category.name | category.description`
    - Normalization: trim, collapse spaces, lowercase matching
    - Precedence: combined with all other filters using logical AND
- Filters (all optional)
  - `categoryId` (long): exact
  - `name` (string): contains, case-insensitive
  - `enabled` (boolean): exact

## Response
- 200 OK → `ApiResponse.success(PageEnvelope<CategorySummaryDto>)`
- `CategorySummaryDto`
  - `id: long`, `name: string`, `description: string|null`, `enabled: boolean`, `partsCount: long`, `createdAt: string(KST)`, `updatedAt: string(KST)`

## Examples
```
GET /api/v1/parts/categories?q=브레이크 관련&sort=partsCount,desc&page=0&size=20
GET /api/v1/parts/categories?q=전장&sort=createdAt,desc&page=0&size=20
GET /api/v1/parts/categories?name=엔진&enabled=true&sort=name,asc&page=0&size=50
```

## Errors
- 400: 페이지/사이즈 범위 오류, 정렬 키 화이트리스트 위반(팀 정책에 따라 200 fallback 가능)

## Notes
- Timezone: API I/O KST(+09:00), 서버 UTC 저장/연산
- Unified `q` parameter follows the convention in `standards/query-strategy.md`.

## References
- Standards: standards/exception-and-response.md, standards/query-strategy.md
- Domain: domain/parts-domain.md
