# UC-REC-004 입고 라인 검수 상태/수량 업데이트

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `2025-10-24`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Policy

- **All-or-Nothing per Product**
    - 일부 수량에 문제가 있으면 **그 라인은 `REJECTED`**(재고 증가 0)
- 문제가 없으면 `ACCEPTED`

## Preconditions

- Note.status ∈ {PENDING, IN_PROGRESS}
- 대상 라인이 완료 상태가 아님(ACCEPTED/REJECTED 아님)

## Main Flow

1) 클라이언트는 `{ inspectedQty, hasIssue }`를 전송
2) 노트는 첫 갱신이라면 `PENDING → IN_PROGRESS`로 전이
3) 라인은 `hasIssue==true` → `REJECTED`, 아니면 `ACCEPTED`로 전이
4) 저장 후 갱신 스냅샷을 반환

## Acceptance

- Given hasIssue=false,
  When 업데이트하면,
  Then 라인 상태는 `ACCEPTED`가 된다
- Given hasIssue=true,
  When 업데이트하면,
  Then 라인 상태는 `REJECTED`가 되며 증가 수량은 0으로 간주된다
- Given 라인이 ACCEPTED/REJECTED,
  When 다시 업데이트하면,
  Then 409가 반환된다

## I/O

- PATCH `/v1/receiving/{noteId}/lines/{lineId}`
- Request `{ "inspectedQty": 48, "hasIssue": false }`
- Response: 최신 NoteDetail 스냅샷

예시(ACCEPTED 결과):
```json
{
  "noteId": 301,
  "status": "IN_PROGRESS",
  "lines": [
    {
      "lineId": 7,
      "part": { "code": "P-1001", "name": "오일필터" },
      "orderedQty": 50,
      "inspectedQty": 50,
      "status": "ACCEPTED"
    }
  ]
}
```

예시(REJECTED 결과):
```json
{
  "noteId": 301,
  "status": "IN_PROGRESS",
  "lines": [
    {
      "lineId": 8,
      "part": { "code": "P-1002", "name": "에어필터" },
      "orderedQty": 40,
      "inspectedQty": 0,
      "status": "REJECTED",
      "remark": "파손 발견"
    }
  ]
}
```

## Errors

- 404: note/line 미존재
- 409: 완료 라인 재수정
- 400: 음수 수량 등 유효성 실패

## References
- Policy: [receiving-inspection.md](../../policy/receiving-inspection.md)
- ADR: [ADR-05-Use-Cases-are-Non-Authoritative.md](../../adr/ADR-05-Use-Cases-are-Non-Authoritative.md)
