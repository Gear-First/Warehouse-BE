# ADR-003-coding-convention-and-architecture

**Title:** 팀 코드 컨벤션 및 아키텍처 결정 (Bootcamp ERP User Service)

---

## 1. 결정(Decision)

> 프로젝트의 구조, 네이밍, 코드 스타일, 예외 처리 및 테스트 컨벤션을 명시적으로 통일한다.
> 도메인 중심 구조를 기반으로, `common` 패키지에만 공통 기능을 허용하며, Google Java Style Guide를 기본 준수한다.

---

## 2. 배경(Background)

팀 간 협업 시 코드 구조와 컨벤션 불일치로 인한 혼란이 발생했다.
특히 다음 영역에서 통일이 필요했다:

- 패키지 구조 (Layered vs Domain vs Hexagonal)
- Controller/Service/Repository 위치 일관성
- Exception/Response 통일
- DTO 및 Entity 네이밍 규칙
- 테스트 네이밍/구조(given-when-then 패턴)
- `common` 패키지의 범위 및 역할

---

## 3. 선택지(Options) 검토

| 구조 유형              | 설명                                        | 장점             | 단점                        |
|--------------------|-------------------------------------------|----------------|---------------------------|
| **Layered**        | Controller => Service => Repository => DB | 직관적, 소규모 적합    | 확장 시 의존 관계 복잡, DDD 적용 어려움 |
| **Domain-Centric** | 도메인별 패키지(user, order 등) 구성                | 응집도 높고 유지보수 용이 | 경계 설정 실패 시 복잡성 증가         |
| **Hexagonal**      | Domain/Service를 외부 의존성과 분리                | 테스트 용이, 확장성 높음 | 초기 학습 부담, 구현 복잡           |

---

## 4. 최종 결정(Chosen Option)

> **도메인 중심(Domain-Centric) 구조를 채택하되**,
> 도메인 내에서 Controller–Service–Repository 계층 흐름을 유지한다.
> “**도메인 중심 + 계층형 혼합형 구조**”로 한다.

### 패키지 구조 예시

```
com.gearfirst.user
 ├─ api
 │   ├─ auth
 │   │   ├─ controller
 │   │   ├─ service
 │   │   │   ├─ AuthService.java
 │   │   │   └─ impl/AuthServiceImpl.java
 │   │   ├─ repository
 │   │   └─ entity
 │   └─ user
 │       ├─ controller
 │       ├─ service
 │       └─ repository
 └─ common
     ├─ config/{SecurityConfig, SwaggerConfig...}
     ├─ advice/{ControllerExceptionAdvice}
     ├─ exception/{BaseException, ...}
     ├─ response/{ApiResponse, ErrorStatus, SuccessStatus}
     └─ entity/{BaseTimeEntity}
```

---

## 5. 세부 컨벤션(Implementation Rules)

### 패키지 네이밍

- 전부 **소문자**, 언더스코어(_) 금지
- 도메인 중심: `com.gearfirst.user.api.auth`, `com.gearfirst.user.api.user`

### 클래스/인터페이스/Enum

- **PascalCase**, 예: `AuthController`, `UserServiceImpl`, `UserRole`
- **DTO**: 도메인 + 목적 + `Request/Response`
  예: `UserCreateRequest`, `AuthLoginResponse`
    - 또는 CQRS 형식의 네이밍 사용 가능

### 메서드/변수

- **camelCase**, 동사 우선
  예: `getUserName()`, `createInviteToken()`
- JUnit 테스트 메서드에서는 `_` 허용 (`shouldThrow_WhenNotFound()`)

### 메서드 순서

- `public` => `protected` => `private`
- `private` 헬퍼는 호출 직후 아래에 둔다.
- 10~15줄 초과 시 분리 고려.

### 클래스 구조

1. 필드
2. 생성자
3. 메서드(오버로드/오버라이드 순서 유지)

---

## 6. 공통 패키지 규칙 (`common`)

| 서브패키지       | 용도                     | 예시 클래스                                        |
|-------------|------------------------|-----------------------------------------------|
| `config`    | 외부 프레임워크 설정            | `SwaggerConfig`, `SecurityConfigSession`      |
| `advice`    | 예외 처리 ControllerAdvice | `ControllerExceptionAdvice`                   |
| `exception` | 전역/도메인별 예외             | `BaseException`, `BadRequestException`        |
| `response`  | 공통 응답 구조               | `ApiResponse`, `ErrorStatus`, `SuccessStatus` |
| `entity`    | 전역 엔티티, Auditing       | `BaseTimeEntity`                              |

---

## 7. 엔티티 규칙 (Entity)

- `@Entity`, `@Table(name="...")` 명시 필수
- 컬럼명은 명시적(`@Column(name="...")`)으로 정의
- `@Embeddable` 내에서 `@Embedded` 중첩 금지
- 기본키:

  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  ```
- 공통 엔티티: `BaseTimeEntity` (등록/수정 시간)

---

## 8. 예외 처리 규칙

- 모든 컨트롤러 예외는 `ControllerExceptionAdvice`에서 전역 처리.
- `BaseException`을 상속한 커스텀 예외 사용.
  예: `NotFoundException`, `UnauthorizedException`
- 예외 명명 규칙: `XXXException`
- 성공/에러 응답 코드:

    - 성공 => `SuccessStatus` Enum
    - 에러 => `ErrorStatus` Enum
- 반환 타입: `ApiResponse<T>`

---

## 9. 테스트 컨벤션

- 테스트 클래스명: `TargetClassNameTest`
  예: `AuthServiceTest`, `UserControllerTest`
- `@Test` 메서드: `should<결과>When<조건>()`
- given–when–then 패턴 유지

  ```java
  @Test
  void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
      // given
      Long id = 999L;

      // when & then
      assertThatThrownBy(() -> userService.getUserById(id))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("해당 사용자를 찾을 수 없습니다.");
  }
  ```

---

## 10. 코드 스타일

- **기준 문서:** [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **참고 문서:** [팀 컨벤션 정리 문서](https://webcoding-start.tistory.com/53)
- **포매터:** IntelliJ Google Style XML 또는 EditorConfig (`.editorconfig`) 유지

---

## 11. 공통 API 응답 규칙

- 모든 API 응답은 `ApiResponse<T>` 형식을 따른다.
- 성공 시:

  ```java
    return ApiResponse.success(SuccessStatus.USER_CREATED, userResponse);
  ```
- 실패 시:

  ```java
    throw new BadRequestException(ErrorStatus.INVALID_INVITE_TOKEN);
  ```

---

## 12. 의사결정 근거 (Rationale)

- **팀 규모/기간**: 학습자 단계(3~4인), 3주 내 완성 => 복잡한 아키텍처 부적합
- **코드 가시성**: 도메인 중심 구조로 API 흐름을 추적하기 쉽게 유지
- **확장 가능성**: 향후 BFF/SSO 단계에서 모듈 단위 확장 용이
- **테스트 용이성**: `common` 모듈 격리로 재사용 가능

---

## 13. 후속 계획(Next Steps)

- Phase2/3 이후 헥사고날로의 리팩토링을 검토(포트/어댑터 구조 실험)
- `.editorconfig` 및 Checkstyle 규칙 추가 (CI에 통합 예정)
- README, ADR-003(테스트 정책) 추가 예정

---

## 14. 요약

- **목표:** “도메인 중심 + 계층형 혼합 구조”로 통일
- **코드 컨벤션:** Google Java Style + 내부 명명 규칙
- **예외/응답 통일:** `ApiResponse<T>`, `BaseException` 기반
- **테스트 컨벤션:** given–when–then, 클래스명 + `Test` 접미
- **공통 코드 분리:** `common/*` 하위로 제한

---
