# UC-PART-006 부품 단종(비활성화)

- Status: Updated
- Date: 2025-10-30
- Deciders: 현희찬

> Non-Authoritative!! 충돌 시 Policy/CRC/Standards/코드/테스트가 우선

## Policy
- 운영 권장: 삭제보다 "단종(Discontinue)"을 우선 사용한다.
- 단종은 검색/리스트/선택에서 기본적으로 제외되도록 한다(enabled=false 또는 status=DISCONTINUED). 단, 과거 이력/참조에는 영향 주지 않는다.
- 삭제(Delete)는 강한 제약(관리자 전용, 참조 무결성 보장)하에 제한적으로만 허용한다. 삭제 정책은 [UC-PART-005-delete-part.md](UC-PART-005-delete-part.md) 참고.

## Intent
- 지정한 부품을 운영 흐름에서 비활성화(단종) 처리한다. 이후 신규 입출고/매핑/검색 대상에서 제외한다(정책에 따름).

## I/O
- PATCH `/v1/parts/{id}:discontinue`
- Request Body (선택):
```
{
  "reason": "단종 사유(선택)",
  "discontinueAt": "2025-10-30T12:00:00Z"  
}
```
- Response:
```
{
  "id": 1001,
  "discontinued": true,
  "discontinueAt": "2025-10-30T12:00:00Z"
}
```

## Acceptance
- Given 존재하는 id, 참조 무결성 조건을 위반하지 않는 상황,
  When 단종 요청,
  Then 200 OK, 응답에 `discontinued=true`가 포함된다.
- Given 이미 단종 상태,
  When 단종 재요청,
  Then 200 OK 또는 409 중 택일(팀 정책). 기본 권고: 200 OK 멱등 처리, 상태만 재확인.

## Errors
- 404: id 미존재
- 409: 단종 처리와 충돌하는 비즈니스 제약(예: 강제 활성화 전환 불가 상황) — 세부 정책은 후속 정의

## Notes
- 검색/리스트에서 단종 항목 제외: 기본. 필요 시 `includeDiscontinued=true`로 명시적 포함.
- Swagger 경고 문구: 일반 운영에서는 삭제보다 단종을 사용하도록 안내한다.

## References
- Domain: domain/parts-domain.md
- Standards: standards/exception-and-response.md
