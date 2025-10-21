# UC-REC-001 오늘 입고 대기 목록 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `25-10-20`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨  

## Intent

오늘 날짜 기준 `PENDING | IN_PROGRESS` 상태의 입고 납품서 목록을 조회

## Preconditions

- 없음(읽기 UC)

## Main Flow

1) 시스템은 날짜(기본: 오늘)로 필터링
2) 상태 ∈ {PENDING, IN_PROGRESS} 인 납품서를 조회
3) 요약 DTO로 투영하여 반환

## Acceptance (GWT)

- Given 오늘 날짜,
  When 대기 목록을 조회하면,
  Then `PENDING | IN_PROGRESS` 납품서만 noteId 오름차순으로 반환
- Given 결과가 없을 때,
  When 조회하면,
  Then 빈 배열을 반환

## I/O (Sketch)

- GET `/v1/receiving/notDone?date=YYYY-MM-DD`
- Response `[ { noteId, supplierName, itemKinds, totalQty, status } ]`

## Errors

- 400: 잘못된 날짜 포맷
