# Parts Domain (MVP)

- Status: Updated
- Date: 2025-11-05
- Deciders: 현희찬

## Scope

- 독립 도메인으로 문서화 우선. 현 단계에서는 입고/출고와 연동하지 않는다(재고 도메인 도입 이후 점진 통합 예정).
- 본 문서는 최소 스키마, 검색 기준과 함께 Part–CarModel 매핑(적용 차종) 관리 UC를 정의한다. 구현(API/엔티티)은 순차 반영 중.
- CRUD 범위(결정): PartCategory/Part CRUD와 PartCarModel 매핑의 생성/수정/삭제 및 조회(양방향)를 포함. CarModel 자체 CRUD는 OOS.

## ERD(개념)

- PartCategory (1) — (N) Part
- Part (N) — (M) CarModel via PartCarModel

## Tables (정합화)

### PartCategory
- id: Long (PK)
- name: String (필수, 유니크, 2..50)
- description: String (선택, 0..200)
- createdAt/updatedAt: datetime
- enabled: boolean (soft delete 용, 기본 true)

Indexes
- UQ_part_category_name(name)

### Part
- id: Long (PK)
- code: String (필수, 유니크) — 형식: `{카테고리명 앞 두 글자}-{NNN}` (한글 허용, 예: `필터-001`, `볼트-002`)
- name: String (필수, 2..100)
- price: Number (필수, >=0)
- categoryId: Long (FK -> PartCategory.id, 필수)
- imageUrl: String (선택)
- safetyStockQty: Integer (필수, 기본 0)
- enabled: boolean (soft delete 용, 기본 true)
- createdAt/updatedAt: datetime

Indexes
- UQ_part_code(code)
- IDX_part_category(categoryId)

### CarModel
- id: Long (PK)
- name: String (필수)
- enabled: boolean (soft delete 용, 기본 true)
- createdAt/updatedAt: datetime

Indexes
- UQ_carmodel_name(name)

### PartCarModel (Mapping)
- partId: Long (PK, FK -> Part.id)
- carModelId: Long (PK, FK -> CarModel.id)
- note: String (선택, 0..200)
- enabled: boolean (soft delete 용, 기본 true)
- createdAt/updatedAt: datetime

Constraints
- UQ_part_car_model(partId, carModelId)

Indexes
- PK_part_car_model(partId, carModelId)
- IDX_pcm_carmodel(carModelId)

## 검색 기준(초안)
- code(정확/부분), name(부분), categoryId(=), carModelId(=)
- 정렬: name ASC 기본, code ASC 보조 (허용 정렬 키 화이트리스트 운영)
- 페이지네이션: page/size

## 예시 스키마(JSON Sketch)

```json
{
  "partCategory": { "id": 1, "name": "필터", "description": "Oil/Air filters" },
  "part": {
    "id": 1, "code": "필터-001", "name": "오일 필터", "price": 12000,
    "categoryId": 1, "imageUrl": "/img/filter-001.png", "safetyStockQty": 5
  },
  "carModel": { "id": 1, "name": "아반떼" },
  "partCarModel": { "partId": 1, "carModelId": 1 }
}
```

## Query Strategy(향후)
- 기본: Spring Data/JPQL 유지.
- 파일럿: Parts Read 검색에서 Querydsl 도입(ADR-06 참조).
