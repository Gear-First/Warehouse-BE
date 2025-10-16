# ADR-002: ApiResponse + BaseException 통일

- Status: Accepted
- Date: 2025-10-16(작성일)
- Deciders: BE 팀(기존 컨밴션 참고함)

## Context

- 응답 포맷/예외 처리 일관성 필요

## Decision

- 성공/실패 응답을 ApiResponse<T>로 통일하고, 예외는 BaseException 파생형으로만 노출
- 예외 메시지는 ErrorStatus enum으로 관리
- ControllerAdvice에서 일괄 처리(GlobalExceptionHandler.java)

## Consequences

- Swagger 문서/핸들러 일관성 향상, 학습비용 감소
- 프레임워크 의존적 설계에는 주의해야함

## Links

- standards/exception-and-response.md

## Follow-ups

- Swagger ApiResponse 어노테이션과 키워드가 중복되므로 추후 개선 필요
