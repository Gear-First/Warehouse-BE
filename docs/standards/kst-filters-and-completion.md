# KST 날짜 필터, 포함 경계(inclusive bounds), 완료 요청 본문 (Receiving / Shipping)

이 문서는 KST 로컬 날짜 필터 및 완료(Completion) API의 최종 동작과 Swagger 문서화 기준을 요약한 것임.
(25.11.04 기준)

---

## KST 날짜 필터 동작 규칙

- 클라이언트는 KST 로컬 날짜(`2025-11-02` 등)를 전달합니다.
- 서버는 `common.util.DateTimes` 유틸을 사용해 이를 UTC 포함 범위로 변환합니다.

    - 단일 날짜(KST) → UTC 포함 구간: `[00:00:00 .. 23:59:59.999999999]`
    - 범위(Range) 는 양쪽 끝이 모두 포함(inclusive) 입니다.
    - 역전된 범위(`from > to`) 는 자동으로 스왑(swap) 처리합니다.
    - 한쪽만 지정된 범위(`from` 또는 `to`만 존재)도 허용됩니다.
- 범위가 단일 날짜보다 우선합니다.

    - `dateFrom` 또는 `dateTo`가 존재할 경우, 컨트롤러는 단일 `date`를 무시하고
      `date=null`로 설정하여 범위 오버로드 서비스 메서드로 위임합니다.
- 필터 기준 필드는 Receiving / Shipping 공통으로 `requestedAt` 입니다.

---

## 검증 및 페이징 규칙

- 페이지/사이즈 제약:
  `page >= 0`, `1 <= size <= 100`
- 정렬(Sorting):
  컨트롤러는 페이징만 수행하며, 정렬은 서비스/어댑터 계층에서 수행됩니다.
  (기본 정렬: ID 기준 `nullsLast`)

---

## Inventory 정렬 화이트리스트

- 허용된 정렬 키:

    - `partName`
    - `partCode`
    - `onHandQty`
    - `lastUpdatedAt`
- 잘못된 정렬 키 또는 잘못된 수량 범위(`minQty > maxQty`)는 400 (Validation Error) 발생.

---

## 완료(Completion) API (본문 필수)

Receiving 완료 요청
`POST /api/v1/receiving/{noteId}:complete`

```json
{
  "inspectorName": "홍길동",
  "inspectorDept": "품질관리팀",
  "inspectorPhone": "010-1234-5678"
}
```

- 사전 조건: 모든 라인이 ACCEPTED 또는 REJECTED 상태이며, 검사자 정보 필수.
- 효과: ACCEPTED 라인별 `orderedQty`만큼 재고 증가.
  상태를 `COMPLETED_OK` 또는 `COMPLETED_ISSUE`로 설정.
- 멱등성(Idempotency): 동일 전표에 재호출 시 `409 Conflict`;
  재고 변화 없음.

---

Shipping 완료 요청
`POST /api/v1/shipping/{noteId}:complete`

```json
{
  "assigneeName": "김담당",
  "assigneeDept": "물류팀",
  "assigneePhone": "010-9876-5432"
}
```

- 사전 조건: 모든 라인이 READY이거나, 하나 이상 SHORTAGE 포함.
- 효과:

    - 모든 라인이 READY → 각 READY 라인의 `pickedQty`만큼 재고 차감 후 `COMPLETED`.
    - SHORTAGE 존재 → 재고 차감 없이 `DELAYED`로 설정.
- 멱등성(Idempotency):
  한 번 `COMPLETED` 또는 `DELAYED` 상태가 되면,
  이후 호출 시 `409 Conflict`; 재고 변화 없음.

---

#### 🧭 Swagger 어노테이션 요약

- Receiving / Shipping 목록 엔드포인트

    - 파라미터: `date`, `dateFrom`, `dateTo` → KST 로컬 날짜로 문서화.
    - 범위 우선 규칙, 포함 경계 명시.
    - 선택적 `warehouseCode`, `page`, `size`, `sort` 설명 포함.
- 완료 엔드포인트

    - `requestBody` 설명에 담당자/검사자 정보 필드 명시.
- Inventory 엔드포인트

    - 정렬 화이트리스트와 유효성 검증 규칙 포함.

---

#### 🧪 관련 테스트 목록

- 컨트롤러 정규화(KST) 테스트:
  `ReceivingControllerKstNormalizationTest`,
  `ShippingControllerKstNormalizationTest` (및 NotDone/Done 변형)
- Inventory 검증 테스트:
  `InventoryControllerTest`
- 완료 및 멱등성 테스트:

    - Receiving:
      `ReceivingServiceInventoryCallsTest`, `ReceivingInventoryIntegrationTest`
    - Shipping:
      `ShippingServiceIdempotencyTest`,
      `ShippingServiceNoDecreaseOnDelayedTest`,
      `ShippingServiceWarehouseTest`,
      `ShippingServiceImplTest`

---
