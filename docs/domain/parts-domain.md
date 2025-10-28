# Parts Domain (MVP)

- Status: Draft (New)
- Date: 2025-10-24
- Deciders: 현희찬

## Scope

- 독립 도메인으로 문서화 우선. 현 단계에서는 입고/출고와 연동하지 않는다(재고 도메인 도입 이후 점진 통합 예정).
- 본 문서는 최소 스키마, 검색 기준과 함께 Part–CarModel 매핑(적용 차종) 관리 UC를 정의한다. 구현(API/엔티티)은 후속 PR에서 진행한다.
- CRUD 범위(결정): 이 라운드에서는 PartCategory/Part CRUD와 더불어 PartCarModel 매핑의 생성/수정/삭제 및 조회(양방향)를 문서화한다. CarModel 자체의 CRUD는 여전히 범위 밖(OOS). 사유: 차량 모델 마스터는 외부 카탈로그 연동 후보로, 내부 CRUD는 보류.

## ERD(개념)

- PartCategory (1) — (N) Part
- Part (N) — (M) CarModel via PartCarModel

## Tables (초안)

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
- code: String (필수, 유니크)
- name: String (필수, 2..100)
- price: Number (필수, >=0)
- categoryId: Long (FK → PartCategory.id, 필수)
- imageUrl: String (선택)
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
- partId: Long (PK, FK → Part.id)
- carModelId: Long (PK, FK → CarModel.id)
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
- 정렬: name ASC 기본, code ASC 보조
- 페이지네이션: page/size

## 예시 스키마(JSON Sketch)

```json
{
  "partCategory": { "id": 10, "name": "Filter", "description": "Oil/Air filters" },
  "part": {
    "id": 1001, "code": "P-1001", "name": "오일필터", "price": 12000,
    "categoryId": 10, "imageUrl": "/img/parts/p-1001.png"
  },
  "carModel": { "id": 501, "name": "Avante" },
  "partCarModel": { "partId": 1001, "carModelId": 501 }
}
```

## Query Strategy(향후)
- 현 단계: Spring Data/JPQL 유지.
- 파일럿 후보: Parts Read 검색에서 Querydsl 도입 검토(후속 PR).
