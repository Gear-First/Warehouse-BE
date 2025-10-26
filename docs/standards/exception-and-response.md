## 응답 표준

- 모든 API는 ApiResponse<T>를 사용
- 성공/실패 코드는 각각 SuccessStatus / ErrorStatus Enum으로 관리.

## 예외 표준

- 모든 컨트롤러 예외는 GlobalExceptionHandler에서 전역 처리
- 도메인/상황별 예외는 BaseException 상속, 네이밍은 XXXException
- 예) BadRequestException, NotFoundException, UnAuthorizedException.

> 상세 배경은 ADR-002 참조.

### HTTP 코드 가이드(문서 기준)
- 400 Bad Request: 입력 검증 실패(범위/포맷/누락 등)
- 401 Unauthorized: 인증 실패
- 404 Not Found: 대상 미존재(note/line/part 등)
- 409 Conflict: 도메인 규칙 위반으로 인한 진행 불가(예: DELAYED 이후 변경 차단, 완료 불가 상태)
- 422 Unprocessable Entity: 사용하지 않음(현 API는 400을 사용). 과거 UC에서 422 표기는 400으로 정규화한다

## ApiResponse<T> 및 관련 Enum 정리

- [ApiResponse.java](../../src/main/java/com/gearfirst/warehouse/common/response/ApiResponse.java)
    - 제네릭 응답 래퍼 클래스
- [SuccessStatus.java](../../src/main/java/com/gearfirst/warehouse/common/response/SuccessStatus.java)
    - 성공 응답 코드 Enum
- [ErrorStatus.java](../../src/main/java/com/gearfirst/warehouse/common/response/ErrorStatus.java)
    - 실패 응답 코드 Enum

## 응답 규격 예시

### 성공

```json
{
  "status": 200,
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 1,
    "name": "Sample Item"
  }
}
```

### 실패

```json
{
    "status": 400,
    "success": false,
    "message": "유효하지 않은 초대 토큰입니다."
}

```

## GlobalExceptionHandler 정리

- [GlobalExceptionHandler.java](../../src/main/java/com/gearfirst/warehouse/common/exception/GlobalExceptionHandler.java)
    - 모든 컨트롤러 예외를 전역 처리하는 @ControllerAdvice
    - BaseException 파생형은 해당 예외에 맞는 HTTP 상태 코드로 응답
    - 그 외 예외는 500 Internal Server Error로 응답
    - 예외 메시지는 ErrorStatus enum에서 관리

## BaseException 및 파생형 정리

- [BaseException.java](../../src/main/java/com/gearfirst/warehouse/common/exception/BaseException.java)
    - 기본 예외 추상 클래스
- [BadRequestException.java](../../src/main/java/com/gearfirst/warehouse/common/exception/BadRequestException.java)
    - 400 Bad Request
- [InternalServerException.java](../../src/main/java/com/gearfirst/warehouse/common/exception/InternalServerException.java)
    - 500 Internal Server Error
- [NotFoundException.java](../../src/main/java/com/gearfirst/warehouse/common/exception/NotFoundException.java)
    - 404 Not Found
- [UnAuthorizedException.java](../../src/main/java/com/gearfirst/warehouse/common/exception/UnAuthorizedException.java)
    - 401 Unauthorized
- 향후 필요 시 추가 파생형 생성 가능

## 

## PageEnvelope (List Response Standard)

- 모든 목록 응답은 페이징 래퍼를 권장한다.
- 스키마

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "total": 123
}
```

- 사용: ApiResponse<PageEnvelope<T>> 형태로 컨트롤러에서 래핑 (현 단계 일부 UC 문서에만 적용; 구현은 후속 PR에서 진행)
- 기본값 권장: page=0, size=20 (size 범위 1..100)

## 409 Conflict 상세 페이로드 가이드 (문서 설계)

- 에러 응답에도 선택적으로 `data`를 포함해, 실패 사유를 구체적으로 제시할 수 있다(호환 유지: 기존 필드는 그대로 유지, data는 선택 항목).
- 공통 틀(예시):

```json
{
  "status": 409,
  "success": false,
  "message": "도메인별 메시지",
  "data": {
    "noteId": 0,
    "noteStatus": "...",
    "problematicLines": [
      { "lineId": 0, "status": "...", "reason": "..." }
    ]
  }
}
```

- Shipping 완료 실패(UC-SHP-005): `problematicLines[].status = SHORTAGE`, `reason = onHand<ordered`
- Receiving 완료 실패(UC-REC-005): `problematicLines[]`는 `ACCEPTED/REJECTED` 외 상태(예: PENDING)를 포함하는 라인 스냅샷

> 구현 주의: 컨트롤러/핸들러에서 409 반환 시 `data`는 선택. 본 표준은 문서 단계이며, 실제 코드는 후속 PR에서 반영한다.
