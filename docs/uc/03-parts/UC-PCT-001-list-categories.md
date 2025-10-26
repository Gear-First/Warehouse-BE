# UC-PCT-001 부품 카테고리 목록 조회

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Intent
부품 카테고리를 이름 키워드로 검색하고 페이지네이션하여 조회한다.

## Preconditions
- page/size가 표준 범위 내(page>=0, 1<=size<=100)

## Main Flow
1) name 키워드(부분 일치)와 정렬 기준으로 카테고리를 조회한다.
2) PageEnvelope로 결과를 반환한다.

## I/O
- GET `/v1/parts/categories?keyword=&page=0&size=20&sort=name,asc`
- Response: ApiResponse<PageEnvelope<CategorySummary>>

CategorySummary
- id: Long
- name: String
- description: String|null

예시
```json
{
  "items": [
    { "id": 10, "name": "Filter", "description": "Oil/Air filters" }
  ],
  "page": 0,
  "size": 20,
  "total": 1
}
```

## Errors
- 400: 페이지/사이즈 범위 오류, sort 키 불일치

## References
- Standards: standards/exception-and-response.md, standards/query-strategy.md
- Domain: domain/parts-domain.md
