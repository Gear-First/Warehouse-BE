# Contributing to Docs

- Status: Draft
- Date: 2025-10-24
- Deciders: 현희찬

## 원칙
- ADR-05: UC 문서는 비권위. 정책/CRC/표준/코드/테스트가 우선
- 문서 우선 단계에서 합의 → 이슈/브랜치 → 관련 문서 반영 → 이후 코드 변경

## 브랜치/커밋/PR
- 브랜치 네이밍: feature/, fix/
- 커밋 메시지: docs(scope): 요약 (#issue)
- PR 설명: 배경/변경/영향/AC/검증 포함, 코드 변경 시 테스트/스냅샷 첨부

## 메타데이터/포맷
- 머리말: Status/Date/Deciders 필수, Date=YYYY-MM-DD
- 예시는 최소 필수 필드만 유지, 과도한 디테일은 각주 또는 후속 후보
- JSON/코드블록은 유효한 형식 유지(린터 통과)

## 표준 준수
- 응답 래퍼: ApiResponse<T>; 목록은 PageEnvelope({items,page,size,total}) 권장(문서 적용, 구현은 후속)
- 오류 예시: 최소 1개의 4xx(400/409) 포함 — 422는 사용하지 않음(standards/exception-and-response.md 참조)
- 상태/용어: glossary.md 참조, 정책과 일치

## 리뷰 체크리스트
- [ ] 정책/CRC/ADR와 용어/상태/전이 정합
- [ ] 링크/참조 유효
- [ ] Date 최신
- [ ] 예시 JSON 유효성
- [ ] 문서 변경 이력(CHANGELOG) 업데이트
