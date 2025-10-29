# UC-INV-001 재고 현황(On-hand) 목록 조회

> Status: Draft  
> Date: 2025-10-27  
> Deciders: 현희찬

> Non-Authoritative!! 정책/표준/CRC/코드/테스트가 우선. 본 UC는 그에 맞춰 갱신됨.

## Intent
- 창고/부품별 현재 On-hand 수량을 조회한다.

> 경고(중요): Inventory의 Create/Update/Delete 엔드포인트는 일반 운영에서 사용하지 않습니다. 이력 보정/초기 적재/오류 복구 등 특수 상황에서만 사용하며, 운영 화면/흐름은 입고(ACCEPTED)와 출고(COMPLETED)를 통해서만 재고가 변동됩니다.

## Preconditions
- 없음(읽기 UC)

## Filters
- warehouseId: = (필수 또는 단일 창고 환경에서는 생략 가능)
- keyword: partCode 또는 partName 부분 일치
- minQty / maxQty: onHandQty 범위 필터(선택)
- page: 기본 0, size: 기본 20

## Sorting
- 기본: partName ASC, 보조: partCode ASC
- 커스텀: `sort=partName,asc|onHandQty,desc` 등 (정의는 정책 참조)

## Main Flow
1) 시스템은 필터/정렬/페이지네이션 파라미터를 검증한다.
2) 정책에 정의된 컬럼으로 투영하여 PageEnvelope 형태로 반환한다.

## I/O
- GET `/v1/inventory/onhand?warehouseId=1&keyword=filter&page=0&size=20&sort=partName,asc`
- Response (PageEnvelope)
```json
{
  "items": [
    {
      "warehouseId": 1,
      "part": { "id": 1001, "code": "P-1001", "name": "오일필터" },
      "onHandQty": 128,
      "lastUpdatedAt": "2025-10-27T03:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 1
}
```

## Errors
- 400: 잘못된 파라미터(페이지/사이즈 범위, 정렬 키 등)

## References
- Policy: [inventory-overview.md](../../policy/inventory-overview.md)
- Standards: [exception-and-response.md](../../standards/exception-and-response.md)
- Context minutes: [minute-of-functional-spec.md](../../context/minute-of-functional-spec.md)
