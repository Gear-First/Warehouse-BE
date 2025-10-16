## 응답 표준

- 모든 API는 ApiResponse<T>를 사용
- 성공/실패 코드는 각각 SuccessStatus / ErrorStatus Enum으로 관리.

## 예외 표준

- 모든 컨트롤러 예외는 ControllerExceptionAdvice에서 전역 처리
- 도메인/상황별 예외는 BaseException 상속, 네이밍은 XXXException
- 예) BadRequestException, NotFoundException, UnAuthorizedException.

> 상세 배경은 ADR-002 참조.

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
  "status": 200, // HTTP 상태 코드
  "success": true,        // 성공 여부
  "message": "요청이 성공적으로 처리되었습니다.", // 상태 메시지
  "data": {          // 데이터 필드, 성공 시 포함
    "id": 1,
    "name": "Sample Item",
    ... // 기타 데이터 필드
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

## ControllerExceptionAdvice 정리

- [ControllerExceptionAdvice.java](../../src/main/java/com/gearfirst/warehouse/common/exception/ControllerExceptionAdvice.java)
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
