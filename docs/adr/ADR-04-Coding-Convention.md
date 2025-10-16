# ADR-04: Coding Convention

- Status: Accepted
- Date: 2025-10-16(작성일)
- Deciders: BE 팀(기존 컨밴션 참고함)

## Context

- MVP에서 일관성은 유지하되, 세팅은 최소화하기 위함
- 기존 컨벤션 문서와 충돌하지 않는 범위 내에서 채택

## Decision

1. 기본 스타일: Google Java Style Guide를 베이스로 한다.
2. 경량 강제 전략만 채택한다.
    - 필수: .editorconfig로 핵심(줄 길이 120, 개행, 인덴트)만 고정
    - 권장: IDE 저장 시 자동 포맷(Reformat on Save), import 정렬 규칙 설정
    - 권장: PR 템플릿 체크리스트(포맷 적용, 테스트 네이밍 확인)
3. CI 단계에 강제성이 필요해지면 Spotless/Checkstyle/GJF 도입 검토

## Consequences

- 개발자별 IDE 설정 차이로 인한 미세한 스타일 차이 가능
- 핵심 규칙만 강제해 초기 세팅 부담 감소
- 일관된 코드 스타일로 가독성 향상

## Links

- [standards/coding-convention.md](../standards/coding-convention.md)
- [standards/testing-convention.md](../standards/testing-convention.md)
- https://google.github.io/styleguide/javaguide.html

## Follow-ups

- `/docs/standards/coding-convention.md`에 IDE 설정 절차와 `.editorconfig` 스니펫 명시
