package com.gearfirst.warehouse.common.util;

import static org.junit.jupiter.api.Assertions.*;

import com.gearfirst.warehouse.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateFilterTest {

    @Test
    @DisplayName("normalize: 단일 date만 주어지면 from=to=date, hasRange=false (범위 아님)")
    void normalize_singleDate() {
        var nf = DateFilter.normalize("2025-01-02", null, null);
        assertEquals("2025-01-02", nf.from());
        assertEquals("2025-01-02", nf.to());
        assertFalse(nf.hasRange());
    }

    @Test
    @DisplayName("normalize: 범위가 주어지면 범위가 우선된다")
    void normalize_rangeWins() {
        var nf = DateFilter.normalize("2025-01-02", "2025-01-01", "2025-01-03");
        assertEquals("2025-01-01", nf.from());
        assertEquals("2025-01-03", nf.to());
        assertTrue(nf.hasRange());
    }

    @Test
    @DisplayName("normalize: to<from이면 스왑된다")
    void normalize_swapWhenToBeforeFrom() {
        var nf = DateFilter.normalize(null, "2025-01-05", "2025-01-02");
        assertEquals("2025-01-02", nf.from());
        assertEquals("2025-01-05", nf.to());
        assertTrue(nf.hasRange());
    }

    @Test
    @DisplayName("normalize: 파싱 오류면 BadRequestException 던진다")
    void normalize_parseError() {
        assertThrows(BadRequestException.class, () -> DateFilter.normalize("2025-13-40", null, null));
    }
}
