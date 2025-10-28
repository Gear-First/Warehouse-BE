# UC-PCM-005 부품-차량 모델 매핑 삭제

- Status: Draft
- Date: 2025-10-28
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선 (ADR-05)

## Intent
부품(Part)과 차량 모델(CarModel) 간의 적용 관계를 삭제한다. 기본 정책은 소프트 삭제(enabled=false)를 권장한다.

## Preconditions
- (partId, carModelId) 매핑이 존재해야 한다. (404)

## I/O (권장: Soft delete)
- DELETE `/v1/parts/{partId}/car-models/{carModelId}`
- Response: ApiResponse<{"deleted": true}>

동작 권장 사항
- 기본은 enabled=false 처리로 소프트 삭제
- 하드 삭제가 필요하면 운영 정책/이력 보존 규정과 합의 후 별도 엔드포인트 제공

## Errors
- 404: 매핑이 존재하지 않음
- 409: 다른 제약과 충돌(예: 이력 보존 규칙에 따라 삭제 금지) — 정책에 따라 사용

## Notes
- 소프트 삭제 시 중복 추가 검증 시에는 기존 비활성 매핑을 재활성화(UPDATE)할지, 새로 추가할지 정책을 명확히 해야 함. 본 문서에서는 재활성화를 권장.

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md
