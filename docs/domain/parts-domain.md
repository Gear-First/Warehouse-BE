# Parts Domain (MVP)

- Status: Draft (New)
- Date: 2025-10-24
- Deciders: 현희찬

## Scope

- 독립 도메인으로 문서화만 진행. 현 단계에서는 입고/출고와 연동하지 않는다(재고 도메인 도입 이후 점진 통합 예정).
- 본 문서는 최소 스키마와 검색 기준을 정의한다. 구현(API/엔티티)은 후속 PR에서 진행한다.

## ERD(개념)

- PartCategory (1) — (N) Part
- Part (N) — (M) CarModel via PartCarModel

## Tables (초안)

### PartCategory
- id: Long (PK)
- name: String (필수, 유니크)
- description: String (선택)

Indexes
- UQ_part_category_name(name)

### Part
- id: Long (PK)
- code: String (필수, 유니크)
- name: String (필수)
- unit: String (필수, 예: EA, BOX)
- price: Number (필수)
- categoryId: Long (FK → PartCategory.id, 필수)
- imageUrl: String (선택)

Indexes
- UQ_part_code(code)
- IDX_part_category(categoryId)

Notes
- price만 사용(통화 표기 없음)

### CarModel
- id: Long (PK)
- name: String (필수)
- maker: String (필수)
- yearRange: String (선택, 예: 2019-2024)

Indexes
- UQ_carmodel_maker_name(maker, name)

### PartCarModel (Mapping)
- partId: Long (PK, FK → Part.id)
- carModelId: Long (PK, FK → CarModel.id)

Indexes
- PK_part_car_model(partId, carModelId)
- IDX_pcm_carmodel(carmodelId)

## 검색 기준(초안)
- code(정확/부분), name(부분), categoryId(=), carModelId(=)
- 정렬: name ASC 기본, code ASC 보조
- 페이지네이션: page/size

## 예시 스키마(JSON Sketch)

```json
{
  "partCategory": { "id": 10, "name": "Filter", "description": "Oil/Air filters" },
  "part": {
    "id": 1001, "code": "P-1001", "name": "오일필터", "unit": "EA", "price": 12000,
    "categoryId": 10, "imageUrl": "/img/parts/p-1001.png"
  },
  "carModel": { "id": 501, "maker": "Hyundai", "name": "Avante", "yearRange": "2020-2024" },
  "partCarModel": { "partId": 1001, "carModelId": 501 }
}
```

## Query Strategy(향후)
- 현 단계: Spring Data/JPQL 유지.
- 파일럿 후보: Parts Read 검색에서 Querydsl 도입 검토(후속 PR).

## References
- ADR: docs/adr/ADR-05-Use-Cases-are-Non-Authoritative.md
- Standards(추가 예정): query-strategy, glossary, acceptance-checklists
