# UC-PCT-004 부품 카테고리 수정

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
카테고리의 이름/설명을 수정한다.

## Validation
- name: required, 2..50, unique (case-insensitive 권장)
- description: optional, 0..200

## I/O
- PATCH `/v1/parts/categories/{id}`
- Request
```json
{ "name": "Filter", "description": "Oil/Air filters" }
```
- Response: ApiResponse<CategoryDetail>

## Errors
- 404: id 미존재
- 400: name 누락/길이 초과
- 409: name 중복

## References
- Standards: standards/exception-and-response.md
- Domain: domain/parts-domain.md
