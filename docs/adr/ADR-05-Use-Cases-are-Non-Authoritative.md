# ADR-05: Use Cases are **Non-Authoritative*- (Reference Only)

- Status: Accepted
- Date: 2025-10-21
- Deciders: 현희찬

## Intent

유스케이스(UC) 문서는 **시나리오를 빠르게 공유하기 위한 참고 자료**일 뿐이며, **설계/구현의 절대 기준이 아니다**

## Problem

개발 중 UC가 가장 자주 변하는 문서임에도, UC의 예시 플로우/필드가 정책(Policy), CRC, Standards보다 우선되는 것처럼 오해될 수 있다

## Decision

1. **권위 우선순위(SoT)*- 를 고정한다
    - **Policy & CRC => Standards => ADR => 코드 & 테스트 => UC(참고용)**
    - 충돌 시 **항상 상위**를 따른다. UC는 **이를 반영해 수정**한다
2. **UC는 비권위 문서**로 명시한다
    - UC 내용(플로우·필드·상태)은 언제든 변경될 수 있으며, **구현은 UC에 종속되지 않는다**
3. **UC 라이프사이클**을 정의한다
    - `Draft => In-Progress => Updated => Aligned => Deprecated`
    - 병합/릴리스 전, 최소 `Updated`, 가능하면 `Aligned`
4. **운영 규칙**을 둔다
    - 구현·리팩토링·정책 변경 시 **UC를 동기화**한다
    - PR 체크리스트에 **“UC 정합성 확인/갱신”*- 항목을 추가한다

### UC 상태 정의

| 단계          | 목적/상태    | 입출조건(Exit Criteria)               |
|-------------|----------|-----------------------------------|
| Draft       | 러프 스케치   | 대략적 목표/흐름 기록                      |
| In-Progress | 구현 중 반영  | 진행하면서 주요 I/O·상태 업데이트              |
| Updated     | 구현 직후 반영 | 머지/릴리스 직후, 실제 동작과 맞춤              |
| Aligned     | 정합 검증 완료 | Policy/CRC/Standards/코드/테스트 일치 확인 |
| Deprecated  | 폐기/대체    | 새 UC/정책으로 대체됨 명시                  |

## Rationale

- Policy/CRC는 **규칙/책임/전이**를 고정한다. Standards는 **코딩/테스트 계약**을 제공한다
- UC는 **케이스 예시**로 빠른 구현을 보조하며, **정합성은 상위 문서와 코드/테스트가 결정**한다

## Consequences

- 장점: 충돌 시 의사결정 경로 명확, 실험/수정 보장, 품질 보존
- 단점: PR 단계에서 UC 동기화 비용 발생(체크리스트로 보완)

## Operational Rules

- **충돌 해결:*- UC ↔ (Policy/CRC/Standards/코드/테스트) 충돌 시 **UC가 하향 조정**된다
- **PR 체크리스트(선택):**
    - [ ] Policy/CRC 변경 여부 확인
    - [ ] UC 상태 `Updated/Aligned`로 갱신
    - [ ] UC 상단 메타데이터의 Links/Tests 최신화

## UC 공통 헤더(권장)

UC 문서 맨 위에 아래 경고 블록을 넣는다

```markdown
> **Non-Authoritative**: 이 UC 문서는 참고용입니다.  
> 충돌 시 Policy/CRC/Standards/코드/테스트가 우선하며, 본 문서는 그에 맞춰 갱신됩니다
**Status:*- Draft|In-Progress|Updated|Aligned|Deprecated  
**Date:*- YYYY-MM-DD  
**Deciders:*- …  
**Links:*- policy/…, domain/…, adr/…  
**Tests:*- ShippingNoteTest, CompleteShippingServiceTest
```
