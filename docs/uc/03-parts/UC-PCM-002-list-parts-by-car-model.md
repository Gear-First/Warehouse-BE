# UC-PCM-002 차량 모델에 적용 가능한 부품 목록 조회

- Status: Draft
- Date: 2025-10-28
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선 (ADR-05)

## Intent
특정 차량 모델(CarModel)에 적용 가능한 부품(Part) 목록을 조회한다. 설계상 M:N 매핑(PartCarModel)을 기반으로 한다.

## Preconditions
- page>=0, 1<=size<=100
- carModelId는 유효해야 한다. (404: 존재하지 않음)

## Filters
- code: contains (선택)
- name: contains (선택)
- categoryId: = (선택)

## Sort
- 기본: name,asc → code,asc

## I/O
- GET `/v1/car-models/{carModelId}/parts?code=&name=&categoryId=&page=0&size=20&sort=name,asc`
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
- 404: carModelId가 존재하지 않음
- 400: 페이지/사이즈 범위 오류, sort 키 불일치

## Notes
- 본 문서는 스펙만 정의한다. 구현은 후속 PR에서 진행.

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md, standards/query-strategy.md
