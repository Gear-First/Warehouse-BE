# OOP CRC – Shipping (MVP, Defensive Cancel Policy)

- Status: Accepted
- Date: 2025-10-21
- Deciders: 현희찬

> 본 문서는 객체의 **역할·책임·협력**만 기술함
> 재고 조회/차감, 지연 처리 등 I/O 오케스트레이션은 **Service/Repository** 책임

## 공통 상태(요약)

- **Note:** `PENDING => IN_PROGRESS => COMPLETED | DELAYED`
    - DELAYED = 정책상 “노트 전체 취소” 의미
    - DELAYED 노트 재시작 관련 정책 추가 필요 (TODO)
- **Line:** `PENDING => IN_PROGRESS => READY | SHORTAGE | ISSUE()`

## ShippingNote (Aggregate Root)

### 역할

출고 문서 한 건의 **상태·완료/취소 판단**과 **라인 집계**의 중심

### 책임

- **라인 갱신 관문:** 첫 갱신 시 `PENDING => IN_PROGRESS` 변경 보장
- **취소 변경:** 라인 갱신 결과가 `SHORTAGE` 또는 `ISSUE`이면 **즉시 `Note=DELAYED`**로 변경하며 이후 변경/완료를 거부
- **완료 가능 판단:** 전 라인이 `READY`이고 Note가 `DELAYED`가 아닐 때만 `canComplete=true`
- **완료 집계:**
    - `complete()` 호출
    - **전 라인 READY** 불변 점검
    - 제품별 `Σ orderedQty`를 **출하 계획(ShipmentPlan)**으로 산출
    - 완료 시각 기록
- **불변:** `COMPLETED` 이후에는 내용 변경 불가
    - 현재는 `DELAYED` 상태 시에도 변경/완료 불가, 추후 DELAYED 재시도 정책 검토 필요 (TODO)

### 협력

- 내부 **ShippingLine** 컬렉션과 협력하여 변경/집계를 수행
- 산출한 **ShipmentPlan(집계 요약)** 은 상위 레이어가 외부 시스템(I/O)과 협력할 때 사용


## ShippingLine

### 역할

출고 판단의 **최소 단위(품목/수량)**

### 책임

- **유효성:** `orderedQty > 0`, `inspectedQty ≥ 0`; 완료 상태 재수정 금지
- **진행 반영/변경:** `applyProgress(inspectedQty, onHand, issue?)`
    - `onHand < orderedQty` => `SHORTAGE`
    - `issue != null` => `ISSUE`
    - 위 두 조건이 아니고 진행 완료 => `READY`
- **완료 판정:** `isDone()`는 `READY | SHORTAGE` 중 하나일 때 true

### 협력

- `onHand` 값은 상위 레이어(서비스)가 조회하여 인자로 전달된다  
  라인은 외부 시스템을 알지 못하며, 지정된 입력만으로 변경를 결정

## Value Objects (VO)

### 식별자/수량/번호

- **NoteId / LineId / ProductId**: 타입 안전 식별자
- **Qty**: `value ≥ 0` 보장(합산/비교 제공)
- **ShippingNo**: `OUT/<warehouse>/<date>/<type>/<seq>` 형식 검증

### 집계 요약

- **ShipmentPlan**: `Map<ProductId, Qty>` (제품별 `Σ orderedQty`, 전 라인 READY 전제), `completedAt` 등

## 협력(요약 시퀀스)

### 라인 진행 갱신 (UC-SHP-004)

- 상위 레이어가 onHand를 조회하여 `applyProgress()`에 전달
- 라인이 `SHORTAGE` 또는 `ISSUE`로 변경되면 **노트는 즉시 `DELAYED`**로 전환된다

### 출고 완료 (UC-SHP-005)

- `canComplete()`가 true일 때만 `complete()` 실행이 가능하다
- `complete()`는 **READY 라인의 `orderedQty` 합**을 **ShipmentPlan**으로 반환
- 실제 차감/저장은 상위 레이어(Service/Repository)의 책임이다
