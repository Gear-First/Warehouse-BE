# UC-PCT-005 부품 카테고리 삭제

- Status: Draft
- Date: 2025-10-27
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Policy
- 기본은 소프트 삭제 권장(enabled 플래그). 하드 삭제는 참조가 없을 때만 허용(Part가 존재하면 불가).

## I/O
- DELETE `/v1/parts/categories/{id}`
- Response: ApiResponse<{ "deleted": true }>

## Acceptance
- Given 존재하는 id이고, 참조 Part가 없다 → When 삭제 → Then 200, {deleted:true}
- Given 존재하는 id이고, 참조 Part가 있다 → When 삭제 → Then 409
- Given 미존재 id → When 삭제 → Then 404

## Errors
- 404: id 미존재
- 409: 참조 존재(Part)

## References
- Standards: standards/exception-and-response.md
- Domain: domain/parts-domain.md
