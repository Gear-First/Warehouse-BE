# Standard: User Context (Gateway Headers -> Filter -> ThreadLocal)


## 1. 헤더 규격(Standard Headers)
- 명명 규칙(고정):
    - `X-User-Id` (필수, 평문)
    - `X-User-Name` (옵션, Base64)
    - `X-User-Rank` (옵션, Base64)
    - `X-User-Region` (옵션, Base64)
    - `X-User-WorkType` (옵션, Base64)
- 인코딩:
    - 문자열 값은 UTF-8 -> Base64 인코딩(PII는 로깅 금지)
    - Id는 평문으로 전달
- 게이트웨이 책임:
    - JWT 검증/파싱 및 위 헤더 주입
    - 외부로부터 직접 서비스 접근 차단(경유 보장)

## 2. 필터 구현 규칙(JwtHeaderFilter)
- 책임:
    - 헤더 읽기 -> Base64 디코딩 -> `UserContext` 생성 -> `UserContextHolder.set()`
    - `finally` 블록에서 `UserContextHolder.clear()` 보장
- 실패 처리(정책):
    - 필수 헤더(`X-User-Id`) 누락 -> 401 또는 400(보안 정책에 따름). 기본: 401 Unauthorized 권장
    - 디코딩 실패 -> 400 Bad Request(`USER_CONTEXT_DECODE_FAILED`)
    - 무효 포맷 -> 400 Bad Request(`INVALID_USER_CONTEXT_HEADER`)
- 제외 경로(권장):
    - `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`, 정적 리소스
- 설정 위치:
    - `@Component` + `FilterRegistrationBean` 또는 `WebSecurityCustomizer` 유사 구성(현재 MVC)

## 3. 컨텍스트 접근 규칙
- 서비스/컨트롤러는 직접 ThreadLocal을 다루지 않는다.
- `UserContextUtils.getRequired()` / `getOptional()` 헬퍼를 통해 접근하고, 예외는 `ErrorStatus`로 표준화한다.
- PII(예: Name)는 로그/메시지에 포함하지 않는다.

## 4. 예외/응답 표준 매핑
- 성공 응답: `CommonApiResponse.success(SuccessStatus, data)`
- 실패 응답: 전역 예외 핸들러에서 다음 항목 매핑(예시):
    - `MISSING_USER_ID_HEADER` -> 401 Unauthorized
    - `INVALID_USER_CONTEXT_HEADER` -> 400 Bad Request
    - `USER_CONTEXT_DECODE_FAILED` -> 400 Bad Request
- 메시지는 한국어/영문 혼용 허용. 운영 메시지는 간결하게.

## 5. 로깅/관측
- MDC 주입(권장 최소): `userId`, 선택적으로 `region`, `workType`
- PII(이름 등)는 로그 출력 금지. 필요 시 마스킹/부분 해시 사용.
- 실패 케이스는 WARN 이상으로 로깅, 스파이크 방지를 위해 샘플링 고려.

## 6. 테스트 표준
- 단위 테스트(`JwtHeaderFilterTest`):
    - 정상 플로우(모든 헤더 존재)
    - 필수 누락(`X-User-Id` 없음)
    - Base64 잘못된 값
    - 옵션 헤더 누락 시 Null/빈 값 처리
    - clear() 보장 확인
- 통합 테스트(`@WebMvcTest`):
    - 모킹 헤더로 컨트롤러에서 컨텍스트 접근(요청 수명주기 내 유효성)
    - 제외 경로에서 필터 미적용 확인
- 회귀 테스트: 주요 도메인 흐름(Shipping 확정, Parts 생성)에 감사/로그 반영 확인

## 7. 비동기/배치 전파
- 기본 MVC 동기 요청은 ThreadLocal로 충분
- `@Async`, 별도 스레드풀 사용 시 `TaskDecorator` 기반 전파 구현 표준화
- 스케줄러/배치 작업은 별도 컨텍스트 합성(시스템 계정) 또는 명시적 파라미터 전달

## 8. 도메인 적용 체크리스트
- 감사 필드와의 연계: `createdBy`, `updatedBy`에 `userId`
- 권한/범위 검증이 필요한 API에서 `region`, `workType` 사용 정책 문서화 및 테스트
- 이벤트/감사 로그에 `userId` 포함(PII 제외)

## 9. 문서/링크
- 컨텍스트: `../context/warehouse-user-context.md`
- ADR: `../adr/ADR-07-User-Context-Via-Gateway-Headers.md`
- 기존 표준: `exception-and-response.md`, `coding-convention.md`, `testing-convention.md`
