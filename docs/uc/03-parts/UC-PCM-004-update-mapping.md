# UC-PCM-004 부품-차량 모델 매핑 수정

- Status: Draft
- Date: 2025-10-28
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선 (ADR-05)

## Intent
기존 부품-차량 모델 매핑(PartCarModel)의 속성(비고/활성 여부 등)을 수정한다.

## Preconditions
- (partId, carModelId) 매핑이 존재해야 한다. (404)

## I/O
- PATCH `/v1/parts/{partId}/car-models/{carModelId}`
- Body
```json
{
  "note": "디젤 트림 제외",
  "enabled": true
}
```
- Response: ApiResponse<PartCarModelDetail>

PartCarModelDetail
- partId
- carModelId
- note (optional)
- enabled (boolean)
- createdAt, updatedAt (string)

## Validation
- note: optional, max 200

## Errors
- 404: 매핑이 존재하지 않음
- 422: 잘못된 입력 값

## Notes
- enabled=false 처리는 사실상 소프트 삭제로 간주 가능(조직 정책에 따름).

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md
