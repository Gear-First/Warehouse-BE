# UC-PCM-003 부품-차량 모델 매핑 추가

- Status: Draft
- Date: 2025-10-28
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선 (ADR-05)

## Intent
특정 부품(Part)과 차량 모델(CarModel)의 적용 관계를 추가한다. 동일 쌍은 중복 불가하다.

## Preconditions
- partId, carModelId는 각각 존재해야 한다. (404)
- 동일 (partId, carModelId) 조합이 이미 존재하면 409

## I/O
- POST `/v1/parts/{partId}/car-models`
- Body
```json
{
  "carModelId": 501,
  "note": "1.6/2.0 가솔린 트림만 호환",
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
- enabled: default true

## Errors
- 404: partId 또는 carModelId가 존재하지 않음
- 409: 이미 존재하는 매핑
- 422: 잘못된 입력 값

## Notes
- 구현에서는 DB 고유 제약(UQ_part_car_model: part_id, car_model_id)을 권장.
- Soft delete를 위해 enabled 필드를 유지하고, 삭제는 enabled=false로 처리 가능(조직 정책에 따름).

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md
