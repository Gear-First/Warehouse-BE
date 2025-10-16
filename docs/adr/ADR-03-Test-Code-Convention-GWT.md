# ADR-003: 테스트 네이밍/스타일(Given-When-Then) 채택

- Status: Accepted
- Date: 2025-10-16(작성일)
- Deciders: BE 팀(기존 컨밴션 참고함)

## Context

- 테스트 가독성과 회귀 안정성 확보 필요.

## Decision

- 클래스명 OOOTest
- 메서드명 예시 shouldXxx_WhenYyy()
- GWT 패턴 고정

## Consequences

- 리뷰, 검색, 가독성 향상
- 일부 긴 테스트 메서드명 허용

## Links

- [standards/testing-convention.md](../standards/testing-convention.md)
