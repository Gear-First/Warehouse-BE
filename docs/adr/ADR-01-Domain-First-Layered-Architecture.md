# ADR-001: 도메인 중심 + 계층형 혼합 구조 채택

- Status: Accepted
- Date: 2025-10-16(작성일)
- Deciders: BE 팀(기존 컨밴션 참고함)

## Context

- 헥사고날의 학습/구현 부담은 과다
- 그러나 도메인별 정리 및 계층 분리는 필요

## Decision

도메인 중심 패키징을 채택하되, 도메인 내부에서는 
`Controller` =>  `Service(interface) <= ServiceImpl` => `Repository(Spring Data)` => `Entity` 계층 흐름을 유지한다.

## Alternatives Considered

- Layered only: 단순하지만 도메인 응집 약함
- Hexagonal: 테스트/확장 우수하나 초기 복잡도↑

## Consequences

- 장점: 탐색 용이, 도메인 응집, 온보딩 속도 빠름
- 단점: 외부 의존 분리 부족(추후 포트/어댑터로 마이그레이션 필요)

## Follow-ups

- standards/package-structure.md에 예시와 네이밍 수록

## Links
- [standards/package-structure.md](../standards/package-structure.md)
