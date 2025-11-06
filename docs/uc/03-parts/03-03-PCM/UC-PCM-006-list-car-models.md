# UC-PCM-006 Parts — List Car Models (Search)

- Status: Draft
- Date: 2025-11-05
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
차량 모델 목록을 이름(옵셔널)로 검색·정렬·페이지네이션하여 조회한다. 통합 문자열 검색(`q`)을 지원하며, 개별 필터와 AND 결합된다.

## Preconditions
- `page >= 0`, `1 <= size <= 200`
- 정렬 키는 화이트리스트만 허용한다.

## Endpoint
- GET `/api/v1/parts/car-models`

## Query Parameters
- Pagination
  - `page` (int, default 0)
  - `size` (int, default 20, max 200)
- Sorting (whitelist; default: `name,asc`)
  - `sort=name|createdAt|updatedAt[,asc|desc]`
- Unified Search
  - `q` (string, optional): case-insensitive contains over `carModel.name`
    - Normalization: trim, collapse spaces, lowercase matching
    - Precedence: combined with all other filters using logical AND
- Filters (all optional)
  - `name` (string): contains, case-insensitive
  - `carModelId` (long): exact
  - `enabled` (boolean): exact match; combined with other filters using AND

## Response
- 200 OK → `ApiResponse.success(PageEnvelope<CarModelSummaryDto>)`
- `CarModelSummaryDto`
  - `id: long`, `name: string`, `enabled: boolean`, `createdAt: string(KST)`, `updatedAt: string(KST)`

## Examples
- Requests
```
GET /api/v1/parts/car-models?q=쏘나&sort=updatedAt,desc&page=0&size=10
GET /api/v1/parts/car-models?name=아반떼&enabled=true&sort=name,asc&page=0&size=20
GET /api/v1/parts/car-models?enabled=false&page=0&size=50
```
- Response (truncated)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 7,
        "name": "아반떼",
        "enabled": true,
        "createdAt": "2025-11-05T10:12:00+09:00",
        "updatedAt": "2025-11-06T09:30:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 37,
    "totalPages": 2
  }
}
```

## Errors
- 400: 페이지/사이즈 범위 오류, 파라미터 타입 오류
- 200 + fallback 정렬: 화이트리스트 외 정렬 키는 무시(또는 팀 정책에 따라 400)

## Notes
- Timezone: API I/O KST(+09:00), 서버 UTC 저장/연산
- Search behavior: 한글 초성(초성검색)은 미지원. 부분 일치(contains)만 지원한다.
