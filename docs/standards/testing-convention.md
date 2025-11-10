# 테스트 코드 컨밴션

## 목적
일관된 테스트 코드 스타일 유지

## 테스트 관련 작명 규칙

- 클래스: 대상 + Test (예: UserServiceTest)
- 메서드: 자유 또는 shouldXxx_WhenYyy(); GWT 주석 권장(given-when-then)

스타일(GWT)

```java

@Test
void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
    // given
    Long nonExistentUserId = 999L;
    // when, then
    assertThatThrownBy(() -> userService.getUserById(nonExistentUserId)).isInstanceOf(NotFoundException.class)
            .hasMessage("해당 사용자를 찾을 수 없습니다.");
}
```

## 원칙

- 예외 케이스도 단위 테스트 작성
- 한 테스트는 한 규칙/행동만 검증
- 공용 픽스처/빌더로 중복 제거
