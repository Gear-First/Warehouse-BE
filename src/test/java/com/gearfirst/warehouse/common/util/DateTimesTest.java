package com.gearfirst.warehouse.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateTimesTest {

    @Test
    @DisplayName("toKstString: UTC 입력을 KST(+09:00)로 변환한다")
    void toKstString_convertsUtcToKst() {
        // 2025-01-01T00:00:00Z -> 2025-01-01T09:00:00+09:00
        OffsetDateTime utc = OffsetDateTime.parse("2025-01-01T00:00:00Z");
        String kst = DateTimes.toKstString(utc);
        assertEquals("2025-01-01T09:00:00+09:00", kst);
    }

    @Test
    @DisplayName("toKstString: null 입력은 null 반환")
    void toKstString_nullReturnsNull() {
        assertNull(DateTimes.toKstString(null));
    }

    @Test
    @DisplayName("toKstString: 비-UTC 입력도 동일 시각을 KST 오프로 변환")
    void toKstString_convertsFromOtherOffsets() {
        OffsetDateTime odt = OffsetDateTime.parse("2025-01-01T12:34:56-04:00");
        String kst = DateTimes.toKstString(odt);
        // -04:00에서 +09:00로는 +13시간 이동 → 2025-01-02T01:34:56+09:00
        assertEquals("2025-01-02T01:34:56+09:00", kst);
    }
}
