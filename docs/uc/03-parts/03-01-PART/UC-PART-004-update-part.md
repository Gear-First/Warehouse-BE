# UC-PART-004 부품 수정

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
부품의 기본 정보를 수정한다.

## Validation
- code: optional 변경 가능하나 충돌 금지(중복 불가) — 구체 패턴은 미정
- name: required, 2..100
- price: required, >= 0
- categoryId: required, 존재해야 함
- imageUrl: optional
- enabled: optional(boolean)

## I/O
- PATCH `/v1/parts/{id}`
- Request(예시)
```json
{
  "name": "오일필터(신형)",
  "price": 13000,
  "categoryId": 10,
  "imageUrl": "/img/parts/p-1001-v2.png",
  "enabled": true
}
```
- Response: ApiResponse<PartDetail>

## Errors
- 404: id/categoryId 미존재
- 400: 유효성 실패
- 409: code 중복(코드 변경 시)

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md, standards/glossary.md
