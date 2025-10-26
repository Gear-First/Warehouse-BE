# UC-PCT-003 부품 카테고리 생성

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
새로운 부품 카테고리를 생성한다.

## Validation
- name: required, 2..50, unique (case-insensitive 권장)
- description: optional, 0..200

## I/O
- POST `/v1/parts/categories`
- Request
```json
{ "name": "Filter", "description": "Oil/Air filters" }
```
- Response: ApiResponse<CategoryDetail>

예시
```json
{
  "id": 10,
  "name": "Filter",
  "description": "Oil/Air filters",
  "createdAt": "2025-10-27T05:00:00Z",
  "updatedAt": "2025-10-27T05:00:00Z"
}
```

## Errors
- 400: name 누락/길이 초과
- 409: name 중복

## References
- Standards: standards/exception-and-response.md
- Domain: domain/parts-domain.md
