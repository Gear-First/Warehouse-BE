# ADR-06: Querydsl 점진적 도입 (Parts 읽기 전용 파일럿)

- 상태: 초안 (Draft)  
- 날짜: 2025-11-05  
- 결정자: 현희찬  
- 관련 문서: `standards/query-strategy.md`, `ADR-05 (UC are Non-Authoritative)`

---

## 배경 (Context)
프로젝트 전반에서 Parts, Inventory, Receiving, Shipping 등 여러 리스트 엔드포인트에 대해  
**동적 필터링, 조인, 조합 가능한 정렬**의 필요성이 커지고 있다.  
현재의 Spring Data 메서드 쿼리와 ad-hoc JPQL은 단순한 경우에는 충분하지만,  
중·장기적으로 **키워드 검색**, **Part–CarModel 다중 조인**, **유연한 정렬** 등을 고려하면  
타입 안정성이 높은 쿼리 빌더의 도입이 유지보수성과 리팩터링 안전성 측면에서 유리하다.

---

## 결정 (Decision)
Querydsl을 점진적으로 도입하며, 우선 **Parts 리스트/검색의 읽기 전용 파일럿**으로 시작한다.  
기존 Spring Data Repository는 CRUD 및 단순 쿼리에 그대로 사용하고,  
복잡한 조회 쿼리만 별도의 Querydsl 기반 Repository로 분리한다.

- **범위 (파일럿): Parts 읽기 전용 검색**
  - 필터: keyword(code/name), categoryId/name, carModelId/name, enabled
  - 정렬: code, name, price, createdAt (asc/desc)
  - 프로젝션: `PartSummary(id, code, name, price, imageUrl, category{id,name})`
  - 페이지네이션: 표준 `page` / `size`
- **비범위 (Non-goals):**
  - 쓰기(Write) 관련 로직은 대상 아님
  - Receiving / Shipping / Inventory는 파일럿 검증 완료 전까지 기존 로직 유지

---

## 고려된 대안 (Alternatives Considered)
- **Spring Data Specification / Example**
  - 장점: 설정이 간단하며 추가 APT 단계 없음
  - 단점: 타입 안정성 부족, 복합 조건 시 가독성 저하, 리팩터링 시 취약
- **커스텀 JPQL / Native Query**
  - 장점: 직접적인 제어 가능
  - 단점: 쿼리 문자열 분산, 유지보수 어려움, 동적 조합 시 오류 가능성 높음

---

## 결과 (Consequences)
- **빌드:** Querydsl 의존성과 애노테이션 프로세서를 추가하고,  
  생성된 Q-class를 소스 세트에 포함
- **코드:** `PartQueryRepository` 및 Predicate 빌더 헬퍼 추가, 기존 Repo 유지
- **리뷰:** 허용된 정렬 키, KST 날짜 정책, DTO 생성자 기반 프로젝션 등 가이드라인 적용
- **테스트:** Predicate 단위 테스트 + `@DataJpaTest` 슬라이스 테스트 추가  
  -> 신규 코드 커버리지 목표: **90% 이상**

---

## 구현 힌트 (Implementation Hints)

### Gradle (Java)
```groovy
dependencies {
    implementation "com.querydsl:querydsl-jpa:5.0.0"
    annotationProcessor "com.querydsl:querydsl-apt:5.0.0:jpa"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api:3.1.0"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api:2.1.1"
}

sourceSets {
    main {
        java {
            srcDirs += ["$buildDir/generated/sources/annotationProcessor/java/main"]
        }
    }
}

tasks.withType(JavaCompile) {
    options.annotationProcessorGeneratedSourcesDirectory = file("$buildDir/generated/sources/annotationProcessor/java/main")
}
```

- `JPAQueryFactory`를 작은 `@Configuration` 클래스에서 Bean으로 등록한다.
- Querydsl로 전환된 엔드포인트에서도 **KST 날짜 윈도우**와
  `expected* = requestedAt + 2 days` 규칙을 반드시 유지한다.

---

## 롤아웃 계획 (Rollout Plan)

1. Gradle 설정 추가 및 `PartQueryRepository` 스텁 + Q-class 생성 테스트 작성
2. 필터/정렬/페이지네이션 구현 및 테스트 작성
3. 팀 리뷰 및 Swagger QA 진행 -> 쿼리 플랜 분석 및 인덱스 보정
4. 다음 대상 결정 (Inventory On-hand, Receiving/Shipping 리스트) 또는 범위 유지 결정

---

## 결정 결과 (Decision Outcome)

- **리스크:** 빌드 복잡도 증가(APT), 학습 곡선 존재
  -> 범위를 제한하고 기존 Repo 유지로 완화
- **이점:** 타입 안정성, 가독성 향상, 확장성과 리팩터링 안전성 확보
  -> 장기적인 검색 기능 확장에 대비 가능
