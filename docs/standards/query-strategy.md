# Query Strategy (MVP)

- Status: Draft
- Date: 2025-10-24
- Deciders: 현희찬

## 결론 (현 단계)
- Spring Data 메서드 쿼리/간단 JPQL 유지
- Querydsl는 "준비만" 단계: 문서/가드레일만 정의, 설치/리팩터 없음
- 파일럿 재평가 시점: Parts Read 검색(후속 PR)

## 근거
- 현재/근시일 질의 복잡도는 메서드 쿼리로 충분(상태/일자/페이징)
- 중기(Parts 검색, 향후 재고/멀티창고)에는 동적 조건/조인이 증가 → Querydsl 적합

## 가드레일(지금부터 준수)
- Repository 목록 조회는 파라미터 객체(SearchCondition/Spec)로 전달(향후 동적 조건 추가 용이)
- Service 계층은 정렬/페이징/필터 파라미터를 객체로 통일해 전달
- UC 문서에 선택적 필터(dateFrom/dateTo/keyword/status[]) 사전 고지(미구현 표기 가능)

## 향후 도입 체크리스트(파일럿 시)
- Gradle: querydsl-jpa + annotationProcessor 설정
- Bean: JPAQueryFactory 구성, Q-클래스 생성 경로 IDE 제외 설정
- Repository: 복잡 조회만 Querydsl로 신규 구현, 나머지는 유지
- 카운트 성능: 별도 countQuery 구성
- 테스트: H2 환경에서 Q-클래스 생성/동작 검증, 변경 라인 diff 커버리지 > 90%

## 참고
- ADR-05(UC 비권위), standards/acceptance-checklists.md
