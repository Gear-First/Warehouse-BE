# UC-PART-005 부품 삭제

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Policy
- 기본은 소프트 삭제(enabled=false) 권장. 실제 하드 삭제는 참조 무결성 보장 시에만 허용.
- Inventory/입출고와의 연동은 현 단계 범위 밖. 단, 향후 참조 존재 시 삭제 제한 가능성을 문서에 고지.

## I/O
- DELETE `/v1/parts/{id}`
- Response: ApiResponse<{ "deleted": true }>

## Acceptance
- Given 존재하는 id → When 삭제 → Then 200, {deleted:true} (soft delete)
- Given 미존재 id → When 삭제 → Then 404

## Errors
- 404: id 미존재

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md
