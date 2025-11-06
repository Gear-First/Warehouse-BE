# UC-PART-002 부품 상세 조회

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
부품 식별자로 상세 정보를 조회한다.

## Preconditions
- id > 0

## I/O
- GET `/v1/parts/{id}`
- Response: ApiResponse<PartDetail>

PartDetail
- id: Long
- code: String
- name: String
- price: number
- category: { id: Long, name: String }
- imageUrl: String|null
- enabled: boolean (soft-delete 대용)
- createdAt, updatedAt: datetime

예시
```json
{
  "id": 1001,
  "code": "P-1001",
  "name": "오일필터",
  "price": 12000,
  "category": { "id": 10, "name": "Filter" },
  "imageUrl": "/img/parts/p-1001.png",
  "enabled": true,
  "createdAt": "2025-10-01T10:00:00Z",
  "updatedAt": "2025-10-10T09:00:00Z"
}
```

## Errors
- 404: id 미존재

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md
