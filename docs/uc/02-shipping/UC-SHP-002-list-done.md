# UC-SHP-002 오늘 완료 출고 목록 조회

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `25-10-20`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Intent

오늘 완료(`COMPLETED`)된 출고 납품서(Note) 목록을 조회 (지연 라인 수 요약 포함)

## I/O

- GET `/v1/shipping/completed?date=YYYY-MM-DD`
- Response `[ { noteId, supplierName, itemKinds, totalQty, status, completedAt, delayedCount } ]`

## TODO 
- 페이징/필터링 정책 추가 검토 필요
- DELAYED 노트 처리 추가 필요 (현재는 없음)
    - 예: 지연 노트도 함께 조회, 지연 사유 요약 등
    - 후속 유스케이스로 분리 검토 가능
