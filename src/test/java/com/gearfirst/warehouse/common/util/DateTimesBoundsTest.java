package com.gearfirst.warehouse.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateTimesBoundsTest {

    @Test
    @DisplayName("kstDayBounds: KST 로컬일을 UTC 포함 경계로 변환한다")
    void kstDayBounds_singleDayToUtcInclusive() {
        // KST 2025-11-02 의 UTC 경계는 [2025-11-01T15:00:00Z, 2025-11-02T14:59:59.999999999Z]
        LocalDate kstDay = LocalDate.of(2025, 11, 2);
        var bounds = DateTimes.kstDayBounds(kstDay);
        assertNotNull(bounds);
        assertEquals(OffsetDateTime.of(2025, 11, 1, 15, 0, 0, 0, ZoneOffset.UTC), bounds.fromInclusive());
        assertEquals(OffsetDateTime.of(2025, 11, 2, 14, 59, 59, 999_999_999, ZoneOffset.UTC), bounds.toInclusive());
    }

    @Test
    @DisplayName("kstRangeBounds: from/to 가 null 혼합이거나 역전되면 보정한다")
    void kstRangeBounds_variousCases() {
        // (1) from=null, to=2025-11-02 → 단일일과 동일
        var b1 = DateTimes.kstRangeBounds(null, LocalDate.of(2025, 11, 2));
        assertEquals(OffsetDateTime.of(2025, 11, 1, 15, 0, 0, 0, ZoneOffset.UTC), b1.fromInclusive());
        assertEquals(OffsetDateTime.of(2025, 11, 2, 14, 59, 59, 999_999_999, ZoneOffset.UTC), b1.toInclusive());

        // (2) from=2025-11-02, to=null → 단일일과 동일
        var b2 = DateTimes.kstRangeBounds(LocalDate.of(2025, 11, 2), null);
        assertEquals(b1.fromInclusive(), b2.fromInclusive());
        assertEquals(b1.toInclusive(), b2.toInclusive());

        // (3) from>to → 스왑되어 정상 범위가 된다 (2025-11-02..2025-11-03)
        var b3 = DateTimes.kstRangeBounds(LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 2));
        assertEquals(OffsetDateTime.of(2025, 11, 1, 15, 0, 0, 0, ZoneOffset.UTC), b3.fromInclusive());
        assertEquals(OffsetDateTime.of(2025, 11, 3, 14, 59, 59, 999_999_999, ZoneOffset.UTC), b3.toInclusive());
    }
}
