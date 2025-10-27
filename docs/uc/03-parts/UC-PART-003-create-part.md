# UC-PART-003 부품 생성

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
새로운 부품을 등록한다.

## Validation
- code: required, unique (구체 패턴은 미정)
- name: required, 2..100
- price: required, >= 0
- categoryId: required, 존재해야 함
- imageUrl: optional, URL 포맷 권장(문서 단계)

## I/O
- POST `/v1/parts`
- Request
```json
{
  "code": "P-1001",
  "name": "오일필터",
  "price": 12000,
  "categoryId": 10,
  "imageUrl": "/img/parts/p-1001.png"
}
```
- Response: ApiResponse<PartDetail>

## Errors
- 400: 유효성 실패(포맷/범위/누락)
- 404: categoryId 미존재
- 409: code 중복

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md, standards/glossary.md
