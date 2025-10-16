# 코드 스타일 및 컨벤션

기준: Google Java Style Guide 준수. 팀 커스텀은 아래 항목만.

## 네이밍

- 클래스/인터페이스/Enum: PascalCase 
- 메서드/변수: camelCase(동사 우선)
- 패키지: 소문자+숫자(언더스코어 X)
- DTO: 도메인 + 목적 + Request/Response 예) UserRequest, UserResponse

## 코드 레이아웃

- 파일 구성: 필드 => 생성자 => 메서드(public => protected => private)
- 오버로드/생성자는 연속 블록으로 정렬
- 메서드가 10~15줄을 넘기면 분리 고려

## Entity

- @Table, @Column으로 테이블/컬럼명 명시
- 엔티티 네이밍: 도메인 단위(예: User)
- @Embeddable 내부에 @Embedded 중첩 사용 금지

## 공통 응답/예외

- 응답 래퍼: ApiResponse<T>
- 성공/실패 코드는 SuccessStatus / ErrorStatus Enum
- 예외: BaseException 상속, ControllerExceptionAdvice에서 전역 처리
- 예외 클래스명: XXXException (예: NotFoundException)

## 테스트

- 클래스명: TargetClassNameTest(반드시 Test로 끝남)
- 예외 케이스 포함, Given–When–Then 패턴 사용
- 테스트 메서드: 필요 시 언더스코어 허용
- 세부 결정/배경은 ADR와 관련 컨밴션 문서 참조
