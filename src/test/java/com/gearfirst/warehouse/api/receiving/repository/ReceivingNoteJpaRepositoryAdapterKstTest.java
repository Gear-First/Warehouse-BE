package com.gearfirst.warehouse.api.receiving.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.persistence.ReceivingNoteJpaRepository;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ReceivingNoteJpaRepositoryAdapterKstTest {

    @Test
    @DisplayName("findDone(date): KST 단일일 경계(UTC 포함 경계)로 필터링한다")
    void findDone_singleDate_filtersByKstBounds() {
        // Given KST day = 2025-11-02 → UTC bounds [2025-11-01T15:00:00Z, 2025-11-02T14:59:59.999999999Z]
        ReceivingNoteJpaRepository jpa = Mockito.mock(ReceivingNoteJpaRepository.class);
        ReceivingNoteJpaRepositoryAdapter adapter = new ReceivingNoteJpaRepositoryAdapter(jpa);

        var before = ReceivingNoteEntity.builder()
                .noteId(1L).status(ReceivingNoteStatus.COMPLETED_OK)
                .requestedAt(OffsetDateTime.of(2025, 11, 1, 14, 59, 59, 0, ZoneOffset.UTC)) // exclude
                .build();
        var start = ReceivingNoteEntity.builder()
                .noteId(2L).status(ReceivingNoteStatus.COMPLETED_OK)
                .requestedAt(OffsetDateTime.of(2025, 11, 1, 15, 0, 0, 0, ZoneOffset.UTC)) // include
                .build();
        var end = ReceivingNoteEntity.builder()
                .noteId(3L).status(ReceivingNoteStatus.COMPLETED_ISSUE)
                .requestedAt(OffsetDateTime.of(2025, 11, 2, 14, 59, 59, 0, ZoneOffset.UTC)) // include
                .build();
        var after = ReceivingNoteEntity.builder()
                .noteId(4L).status(ReceivingNoteStatus.COMPLETED_OK)
                .requestedAt(OffsetDateTime.of(2025, 11, 2, 15, 0, 0, 0, ZoneOffset.UTC)) // exclude
                .build();

        when(jpa.findAllByStatusIn(List.of(ReceivingNoteStatus.COMPLETED_OK, ReceivingNoteStatus.COMPLETED_ISSUE)))
                .thenReturn(List.of(before, start, end, after));

        // When
        var result = adapter.findDone("2025-11-02");

        // Then (only start & end remain)
        assertEquals(2, result.size());
        var ids = result.stream().map(n -> n.getNoteId()).toList();
        assertEquals(List.of(2L, 3L), ids);
    }

    @Test
    @DisplayName("findNotDone(range): KST 범위 경계(UTC 포함)로 필터링하고 warehouseCode는 옵션이다")
    void findNotDone_range_filtersAndWarehouseOptional() {
        ReceivingNoteJpaRepository jpa = Mockito.mock(ReceivingNoteJpaRepository.class);
        ReceivingNoteJpaRepositoryAdapter adapter = new ReceivingNoteJpaRepositoryAdapter(jpa);

        var n1 = ReceivingNoteEntity.builder()
                .noteId(10L).status(ReceivingNoteStatus.IN_PROGRESS).warehouseCode("서울")
                .requestedAt(OffsetDateTime.of(2025, 11, 1, 23, 0, 0, 0, ZoneOffset.UTC)) // KST 11/02 08:00
                .build();
        var n2 = ReceivingNoteEntity.builder()
                .noteId(11L).status(ReceivingNoteStatus.PENDING).warehouseCode("부산")
                .requestedAt(OffsetDateTime.of(2025, 11, 2, 23, 0, 0, 0, ZoneOffset.UTC)) // KST 11/03 08:00
                .build();
        var n3 = ReceivingNoteEntity.builder()
                .noteId(12L).status(ReceivingNoteStatus.IN_PROGRESS).warehouseCode("서울")
                .requestedAt(OffsetDateTime.of(2025, 11, 3, 15, 0, 0, 0, ZoneOffset.UTC)) // 상한 초과(UTC), KST 11/04 00:00 → 제외
                .build();

        when(jpa.findAllByStatusNotIn(List.of(ReceivingNoteStatus.COMPLETED_OK, ReceivingNoteStatus.COMPLETED_ISSUE)))
                .thenReturn(List.of(n1, n2, n3));

        // Range 2025-11-02..2025-11-03 (KST)
        var resultAll = adapter.findNotDone(null, "2025-11-02", "2025-11-03", null);
        assertEquals(2, resultAll.size()); // n1(KST 11/02), n2(KST 11/03)

        var resultSeoul = adapter.findNotDone(null, "2025-11-02", "2025-11-03", "서울");
        assertEquals(1, resultSeoul.size());
        assertEquals(10L, resultSeoul.get(0).getNoteId());
    }
}
