### 시간대 정책 및 KST 경계 테스트

이 프로젝트는 다음과 같은 날짜/시간 처리 정책을 따른다.

- **저장 및 연산:** UTC 사용 (`OffsetDateTime`은 UTC 기준으로 저장됨)
- **API의 지역 날짜 필터링:** 클라이언트는 KST 기준 날짜(예: `2025-11-02`)를 전달하며, 서버는 이를 UTC 포함 구간으로 변환한다.

    - 단일 날짜 `YYYY-MM-DD` (KST) -> UTC 포함 범위: `[KST 일자 00:00 -> UTC, KST 일자 23:59:59.999999999 -> UTC]`
    - 구간은 양쪽 끝이 모두 포함된다.
    - `dateFrom > dateTo`인 경우 서버가 자동으로 순서를 교체하여 유효한 범위를 유지한다.
    - 한쪽만 존재할 경우, 반대쪽은 열린 범위(open-ended range)로 간주한다.
- **응답 직렬화:** 클라이언트가 KST 문자열을 기대하는 경우 `+09:00` 오프셋 문자열로 반환한다. (참조: `DateTimes.toKstString`)

#### 참조 유틸리티

- `DateTimes.kstDayBounds(LocalDate)`: KST 날짜를 UTC 포함 구간 `[fromInclusive, toInclusive]`로 변환한다.
- `DateTimes.kstRangeBounds(LocalDate from, LocalDate to)`: KST 날짜 범위를 UTC 포함 구간으로 변환하며, null 또는 순서 뒤바뀐 입력도 안전하게 처리한다.

#### 적용 위치

- Receiving 및 Shipping 리포지토리/어댑터는 `requestedAt` 기준으로 `DateTimes`가 반환하는 UTC 포함 구간을 이용하여 필터링한다.
- 컨트롤러는 `DateFilter`를 사용해 날짜 파라미터를 정규화하며, `dateFrom`/`dateTo`가 존재할 경우 단일 `date`보다 우선한다.

    - 단일 방향(dateFrom 또는 dateTo만 존재)도 지원한다. 한쪽이 비어 있으면 다른 쪽은 열린 범위로 처리된다.
    - 통합 Shipping 엔드포인트 `/api/v1/shipping/notes` 또한 동일한 규칙을 따른다. 범위가 주어지면 단일 `date` 파라미터는 무시되며 서비스 계층에는 null로 전달된다.

#### 테스트 구성

- **경계 변환 단위 테스트:**

    - `src/test/java/com/gearfirst/warehouse/common/util/DateTimesBoundsTest.java`
    - `src/test/java/com/gearfirst/warehouse/common/util/DateTimesTest.java`
- **리포지토리 어댑터 KST 필터링 테스트:**

    - Receiving: `src/test/java/com/gearfirst/warehouse/api/receiving/repository/ReceivingNoteJpaRepositoryAdapterKstTest.java`
    - Shipping: `src/test/java/com/gearfirst/warehouse/api/shipping/repository/ShippingNoteJpaRepositoryAdapterKstTest.java`
- **컨트롤러 정규화 테스트 (범위 우선 및 역순 범위 교체):**

    - Receiving: `src/test/java/com/gearfirst/warehouse/api/receiving/controller/ReceivingControllerKstNormalizationTest.java`
    - Shipping: `src/test/java/com/gearfirst/warehouse/api/shipping/controller/ShippingControllerKstNormalizationTest.java`

이 테스트들은 다음을 검증한다.

- KST 단일 날짜 구간이 예상된 UTC 포함 구간을 생성한다.
- KST 범위가 양쪽 끝을 포함하고, 상한선을 초과한 값은 제외된다.
- 컨트롤러 계층이 단일 날짜보다 범위를 우선 적용하며, 순서가 반대로 주어질 경우 자동으로 교체한다.

#### 테스트 데이터베이스 설정

- 테스트는 로컬 PostgreSQL 데이터베이스를 사용하며, 실행 시마다 스키마를 자동으로 생성/삭제한다.
- 테스트 DB 환경 변수(기본값 포함):

    - `TEST_DB_HOST` (기본값 `127.0.0.1`)
    - `TEST_DB_PORT` (기본값 `5432`)
    - `TEST_DB_NAME` (기본값 `warehousedb`)
    - `TEST_DB_USER` (기본값 `postgres`)
    - `TEST_DB_PASSWORD` (기본값 `1234`)

로컬 PostgreSQL 임시 실행 예시 (Docker):

```bash
  docker run --rm -e POSTGRES_PASSWORD=1234 -e POSTGRES_DB=warehousedb -p 5432:5432 postgres:16
```

테스트 프로필 설정 (`src/test/resources/application.yml`):

- `spring.jpa.hibernate.ddl-auto: create-drop`
- `hibernate.jdbc.time_zone: UTC`
- Kafka 자동 설정은 제외되며, 외부 브로커를 사용하지 않기 위해 테스트용 mock `KafkaTemplate`이 제공된다.

#### 운영 관련 메모

- 배포 환경에서는 CORS 처리를 단일 진입점(게이트웨이)에서 담당하도록 유지한다.
  로컬 개발 환경에서는 `http://localhost:5173`에 대한 애플리케이션 수준 CORS 허용 가능.
- 시간대 정책:

    - DB 저장 및 연산은 UTC
    - 클라이언트 요청은 KST 날짜 기준
    - UI 노출은 KST 문자열 사용

---

#### Completion API 요청 본문 및 멱등성(idempotency)

- **Receiving 완료 요청 본문 예시:**

```http
POST /api/v1/receiving/{noteId}:complete
{
  "inspectorName": "홍길동",
  "inspectorDept": "품질관리팀",
  "inspectorPhone": "010-1234-5678"
}
```

- **Shipping 완료 요청 본문 예시:**

```http
POST /api/v1/shipping/{noteId}:complete
{
  "assigneeName": "김담당",
  "assigneeDept": "물류팀",
  "assigneePhone": "010-9876-5432"
}
```

- **멱등성:**
  노트가 이미 종료 상태(Receiving: `COMPLETED_OK` / `COMPLETED_ISSUE`, Shipping: `COMPLETED` / `DELAYED`)인 경우,
  두 번째 완료 요청은 `409 Conflict`를 반환하며 재고 수량에는 아무런 변경도 발생하지 않는다.
