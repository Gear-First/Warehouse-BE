# ADR-07: User Context via Gateway Headers -> Servlet Filter -> ThreadLocal

- 상태: Proposed
- 날짜: 2025-11-10
- 관련 문서: 
  - `docs/context/warehouse-user-contextmd`
  - `docs/standards/standard-user-contextmd`
  - 기존: `docs/adr/ADR-02-Uniformed-Responses-Exceptionsmd`, `docs/adr/ADR-01-Domain-First-Layered-Architecturemd`

## 1 컨텍스트(문제 정의)
- 인증/인가를 서비스마다 구현하면 책임 중복과 보안 리스크
- Gateway에서 JWT를 검증/파싱한 사용자 정보를 각 BE 서비스에 안전하게 전파 필요
- Warehouse-BE는 도메인 로직에서 일관된 사용자 컨텍스트(UserContext) 요구

## 2 결정(Decision)
- Gateway가 검증한 JWT에서 사용자 정보를 추출하여 커스텀 헤더(`X-User-*`)로 전달
- Warehouse-BE는 Servlet `Filter`에서 해당 헤더를 읽고, Base64 디코딩을 거쳐 `UserContext`를 생성/보관
- `UserContextHolder(ThreadLocal)`에 저장하여 요청 처리 스레드 범위에서 접근
- 요청 종료 시 반드시 `clear()`하여 누수/오염을 방지

## 3 근거(Rationale)
- 단순성: 각 서비스가 JWT 파싱/검증을 중복 구현하지 않는다
- 책임 분리: 인증/인가는 Gateway, 도메인 처리는 서비스에 집중
- 성능/호환: MVC 동기 요청에서 ThreadLocal로 최소 오버헤드
- 기존 응답/예외 표준(ADR-02)과 쉽게 통합된다

## 4 트레이드오프(Trade-offs)
- ThreadLocal 한계: 비동기(@Async), 별도 스레드풀, 배치/스케줄러로 전파되지 않는다 -> 전파 유틸/`TaskDecorator` 가이드 필요
- 헤더 신뢰: 내부 신뢰 경계 내에서만 사용 가능 Gateway 경유 보장 및 인그레스 통제 필요
- 보안/PII: 이름 등 개인 식별 정보는 로그 출력 금지 Base64는 보안 수단이 아님(전송 안전성은 TLS에 의존)

## 5 대안(Alternatives)
- 서비스 내부에서 JWT 직접 파싱/검증: 책임 중복, 회귀 리스크 증가
- Spring Security의 `Authentication` 공유: 인프라 복잡도 상승, 마이그레이션 비용
- 세션 기반: 스케일아웃/무상태성 저해

## 6 결과(Consequences)
- 구현 범위:
  - `commoncontext` 패키지: `UserContext`, `UserContextHolder`, 유틸
  - `JwtHeaderFilter`: 헤더 파싱(Base64), set/clear 보장
  - `ErrorStatus` 보강: `MISSING_USER_ID_HEADER`, `INVALID_USER_CONTEXT_HEADER`, `USER_CONTEXT_DECODE_FAILED`
  - 전역 예외 핸들러 매핑
- 테스트/운영:
  - 단위/통합 테스트로 헤더 누락/디코딩 실패/정상 플로우 검증
  - MDC에 `userId` 등 최소 정보 주입(PII 최소화)
- 롤백 전략:
  - Filter 등록 비활성화로 기능 끄기 가능
  - 향후 Spring Security로 이관 시 어댑터 계층을 통해 점진적 전환

## 7 링크
- 컨텍스트: `/context/warehouse-user-contextmd`
- 표준: `/standards/standard-user-contextmd`
