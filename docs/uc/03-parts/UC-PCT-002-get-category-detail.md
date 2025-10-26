# UC-PCT-002 부품 카테고리 상세 조회

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
카테고리 식별자로 상세 정보를 조회한다.

## Preconditions
- id > 0

## I/O
- GET `/v1/parts/categories/{id}`
- Response: ApiResponse<CategoryDetail>

CategoryDetail
- id: Long
- name: String
- description: String|null
- createdAt, updatedAt: datetime

예시
```json
{
  "id": 10,
  "name": "Filter",
  "description": "Oil/Air filters",
  "createdAt": "2025-10-01T10:00:00Z",
  "updatedAt": "2025-10-10T09:00:00Z"
}
```

## Errors
- 404: id 미존재

## References
- Standards: standards/exception-and-response.md
- Domain: domain/parts-domain.md
