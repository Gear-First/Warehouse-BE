# UC-SHP-005 출고 완료 (부분출하 + 지연 처리)

> **Status: `Updated`**   (Draft|In-Progress|Updated|Aligned|Deprecated)  
> **Date:** `25-10-20`  
> **Deciders:** `현희찬`

> **Non-Authoritative!!**: `Aligned`이 아닌 UC 문서는 참고용  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선, 본 문서는 그에 맞춰 갱신됨

## Policy

- **READY 라인만** 출하(차감) `DELAYED / DONE_ISSUE` 라인은 출하 제외, 지연 목록에 포함
- 차감은 제품별 orderedQty 합 기준으로 수행

## Preconditions

- 모든 라인이 `READY | DELAYED | DONE_ISSUE` 상태

## Main Flow

1) noteComplete() → `{ toDeduct(product→qty), delayedLines[], completedAt }`
2) toDeduct에 대해 onHand 재확인 후 decrease(원자적으로 적용)
3) noteStatus=`COMPLETED`
4) 지연 라인은 지연 목록/티켓으로 응답(저장 선택)

## Acceptance

- Given READY/DELAYED 혼재,
  When 완료를 요청하면,
  Then READY만 차감되고 note는 `DELAYED`, 지연 목록이 응답에 포함
- Given 모든 라인이 READY,
  When 완료를 요청하면,
  Then 모든 라인 차감되고 note는 `COMPLETED`, 지연 목록은 빈 배열


## I/O

- POST `/v1/shipping/{noteId}:complete`
- Response

```json
{
  "completedAt": "2025-01-01T00:00:00Z",
  "ordered": {
    "P-1001": 40,
    "P-1002": 40
  },
  "checked": {
    "P-1001": 30,
    "P-1002": 40
  },
  "delayed": [
    {
      "lineId": 3,
      "productNo": "P-1003",
      "missingQty": 10
    }
  ]
}
```
