# UC-PCM-001 부품을 사용하는 차량 모델 목록 조회

- Status: Draft
- Date: 2025-10-28
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선 (ADR-05)

## Intent
특정 부품을 사용하는 차량 모델(CarModel) 목록을 조회한다. 설계상 M:N 매핑(PartCarModel)을 기반으로 한다.

## Preconditions
- page>=0, 1<=size<=100
- partId는 유효한 식별자여야 한다. (404: 존재하지 않음)

## Filters
- name: contains (선택)

## Sort
- 기본: name,asc

## I/O
- GET `/v1/parts/{partId}/car-models?name=&page=0&size=20&sort=name,asc`
- Response: ApiResponse<PageEnvelope<CarModelSummary>>

CarModelSummary
- id
- name

예시
```json
{
  "items": [
    { "id": 501, "name": "Avante" }
  ],
  "page": 0,
  "size": 20,
  "total": 1
}
```

## Errors
- 404: partId가 존재하지 않음
- 400: 페이지/사이즈 범위 오류, sort 키 불일치

## Notes
- 현 단계에서는 문서만 제공하며, 실제 구현/스키마 변경은 후속 PR에서 진행한다.
- 향후 warehouseId 필터 추가 가능성은 본 문서 범위 밖.

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md, standards/query-strategy.md
