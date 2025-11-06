# UC-PART-001 부품 목록 조회

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
부품을 코드/이름/카테고리로 검색하고 페이지네이션하여 조회한다.

## Preconditions
- page>=0, 1<=size<=100

## Filters
- code: contains (부분 일치)
- name: contains (부분 일치)
- categoryId: = (선택)

## Sort
- 기본: name,asc; 보조: code,asc
- 파라미터 예: `sort=name,asc&sort=code,asc`

## I/O
- GET `/v1/parts?code=&name=&categoryId=&page=0&size=20&sort=name,asc`
- Response: ApiResponse<PageEnvelope<PartSummary>>

PartSummary
- id
- code
- name
- category: { id, name }

예시
```json
{
  "items": [
    { "id": 1001, "code": "P-1001", "name": "오일필터", "category": {"id": 10, "name": "Filter"} }
  ],
  "page": 0,
  "size": 20,
  "total": 1
}
```

## Errors
- 400: 페이지/사이즈 범위 오류, sort 키 불일치

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md, standards/query-strategy.md
