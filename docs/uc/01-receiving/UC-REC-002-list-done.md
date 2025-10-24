# UC-REC-002 오늘 완료 입고 목록 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Intent

오늘 완료된(`COMPLETED_OK | COMPLETED_ISSUE`) 입고 납품서 목록을 조회

## Main Flow

1) 오늘 날짜 기준으로 완료 납품서를 조회
2) 완료 시간과 함께 요약 DTO를 반환

## Acceptance

- Given 오늘 완료된 납품서가 있을 때,
  When 완료 목록을 조회하면,
  Then `COMPLETED_OK | COMPLETED_ISSUE` 상태의 납품서와 `completedAt`이 포함된 요약 DTO 리스트가 반환

## I/O

- GET `/v1/receiving/completed?date=YYYY-MM-DD`
- Response `[ { noteId, supplierName, itemKinds, totalQty, status, completedAt } ]`

## Errors

- 400: 잘못된 날짜 포맷

## Notes

- 선택적 필터 사전 고지: `dateFrom`, `dateTo`, `keyword`, `status[]` (현 단계 미구현)
- 멀티 창고(warehouseId) 필터는 향후 도입 가능성만 문서화, 현 단계 미도입

## References
- Policy: [receiving-inspection.md](../../policy/receiving-inspection.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)
