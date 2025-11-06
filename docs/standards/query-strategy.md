# Query Strategy (MVP)

- Status: Proposed (Pilot Ready)
- Date: 2025-11-05
- Deciders: 현희찬

## 결론 (현 단계)
- 기본 원칙: Spring Data 메서드 쿼리/간단 JPQL 유지
- Querydsl: 읽기 전용 복잡 조회에 한정하여 점진 도입. 1차 파일럿: Parts Read 검색
- 도입은 "추가" 방식으로 진행(기존 Repository 보존), 리스크/범위 최소화

## 근거
- 현재/근시일 질의 복잡도는 메서드 쿼리로 충분(상태/일자/페이징)
- 중기(Parts 검색, 향후 재고/멀티창고)에는 동적 조건/조인/정렬 조합 증가 -> Querydsl 적합(타입세이프/가독성/리팩터 안정성)

## 파일럿 범위(Parts Read)
- 필터: q(code|name), categoryId/name, carModelId/name, enabled
- 정렬: code, name, price, createdAt (asc/desc)
- 프로젝션: PartSummary(id, code, name, price, imageUrl, category{id,name})
- 페이지네이션: page/size 기본값 유지

## 개발자 준비(Gradle/IDE)
- Gradle 의존성: querydsl-jpa, querydsl-apt(jpa), jakarta.persistence/annotation-api(annotationProcessor)
- 생성 경로: build/generated/sources/annotationProcessor/java/main 를 sources root 로 추가(IDE)
- Bean: JPAQueryFactory 구성(예: @Configuration)

## 가드레일(공통)
- Repository 목록 조회는 파라미터 객체(SearchCondition)로 전달 -> Querydsl 전환 용이
- 허용 정렬 키 화이트리스트 운영(무효 키는 무시 또는 기본 정렬)
- KST 날짜 윈도 규칙/expected* 기본값(+2일) 유지(Receiving/Shipping)

## 향후 도입 체크리스트(파일럿/확장 시)
- Repository: 복잡 조회만 Querydsl 신규 구현, 나머지는 유지
- 카운트 성능: 별도 countQuery 구성 또는 fetchResults 대체 전략
- 테스트: Q-클래스 생성/동작 검증, 조건/정렬/페이지 조합 테스트, 커버리지 > 90%
- 성능: N+1 방지(LEFT JOIN + distinct/서브쿼리), 인덱스 점검

## 참고
- ADR-06-Querydsl-Adoption.md(본 문서의 ADR)
- ADR-05(UC 비권위), standards/acceptance-checklists.md


## Unified Search Parameter (`q`) — Convention
- Purpose: Enable one-string integrated search across primary and related fields while preserving precise field filters.
- Parameter: `q` (string, optional)
  - Matching scope (Parts pilot): case-insensitive contains over `part.code | part.name | category.name | carModel.name`
  - Normalization: trim, collapse multiple spaces, compare using lowercase
  - Precedence: `q` is ANDed with all other specific filters (e.g., `categoryId`, `enabled`)
- Mini-grammar (operators inside `q`, optional):
  - `id:123` (exact numeric id)
  - `code:P-1001` (contains by default; exact can be `code==P-1001` if needed later)
  - `name:패드`
  - `category:제동`
  - `model:아반떼`
- ID handling policy:
  - Do NOT implicitly interpret a bare numeric `q` value as `id`. Ambiguity with codes/names that include digits is risky.
  - Use explicit `partId` request parameter for programmatic exact targeting; or use `q=id:123` operator when using a single search box.
- Sorting & paging: unchanged; continue whitelist sorting and standard `page/size` semantics.
- Implementation notes (Querydsl):
  - LEFT JOIN related tables; use `distinct`/id-grouping; separate `countQuery` without heavy joins.
  - Tokenize `q`, detect operators, combine predicates with AND; free-text tokens match across the defined scope.
