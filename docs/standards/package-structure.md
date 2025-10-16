# 패키지 구조

원칙: 패키지 구조는 도메인 기준으로 한다.

## 비교

### 1) 도메인(DDD / Domain‑Centric, Domain-First-Layered)

```text
com.example.project
   ├ user
   │ ├ controller
   │ ├ domain
   │ │ ├ entity
   │ │ ├ enums
   │ │ └ vo
   │ ├ service
   │ └ repository
```

- 도메인 응집·가시성 우수, 대규모에서도 유리
- 경계 설정이 미흡하면 복잡해질 수 있음

### 2) 계층형(Layered)

```text
   com.example.project
   ├ controller
   ├ service
   ├ repository
   └ domain
```

- 흐름 직관적(Controller → Service → Repository → DB)
- 단점: 규모 커질수록 의존 복잡, DDD 적용 어려움

### 3) 헥사고날(Hexagonal)

```text
   com.example.project
   ├ domain (model, service)
   ├ application
   ├ adapter
   │ ├ in  (controller, api)
   │ └ out (repository, external API)
```

- 비즈니스 로직 격리, 외부 의존 최소화, 테스트 용이
- 구조 이해·구현 난이도 높음

## 선택 배경 및 결정

- 기본은 도메인 중심 + 계층 흐름 혼합을 채택
(도메인 하위에 controller/service/repository 배치)

## common 패키지 규칙

- 전역/공통만 위치(특정 도메인 종속 금지)
- common/config: 외부 프레임워크 설정(예: SwaggerConfig, JpaConfig)
- common/exception: BaseException, 파생 예외, ControllerExceptionAdvice
- common/response: ApiResponse, SuccessStatus, ErrorStatus
- common/entity: 전역 엔티티 또는 Auditing용 엔티티만 배치
