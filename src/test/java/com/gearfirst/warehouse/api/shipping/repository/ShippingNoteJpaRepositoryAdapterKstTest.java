package com.gearfirst.warehouse.api.shipping.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.persistence.ShippingNoteJpaRepository;
import com.gearfirst.warehouse.api.shipping.persistence.entity.ShippingNoteEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ShippingNoteJpaRepositoryAdapterKstTest {

    @Test
    @DisplayName("findDone(date): KST 단일일 경계(UTC 포함 경계)로 필터링한다")
    void findDone_singleDate_filtersByKstBounds() {
        // Given KST day = 2025-11-02 → UTC bounds [2025-11-01T15:00:00Z, 2025-11-02T14:59:59.999999999Z]
        ShippingNoteJpaRepository jpa = Mockito.mock(ShippingNoteJpaRepository.class);
        ShippingNoteJpaRepositoryAdapter adapter = new ShippingNoteJpaRepositoryAdapter(jpa);

        var before = ShippingNoteEntity.builder()
                .noteId(1L).status(NoteStatus.COMPLETED)
                .requestedAt(OffsetDateTime.of(2025, 11, 1, 14, 59, 59, 0, ZoneOffset.UTC)) // exclude
                .build();
        var start = ShippingNoteEntity.builder()
                .noteId(2L).status(NoteStatus.COMPLETED)
                .requestedAt(OffsetDateTime.of(2025, 11, 1, 15, 0, 0, 0, ZoneOffset.UTC)) // include
                .build();
        var end = ShippingNoteEntity.builder()
                .noteId(3L).status(NoteStatus.DELAYED)
                .requestedAt(OffsetDateTime.of(2025, 11, 2, 14, 59, 59, 0, ZoneOffset.UTC)) // include
                .build();
        var after = ShippingNoteEntity.builder()
                .noteId(4L).status(NoteStatus.COMPLETED)
                .requestedAt(OffsetDateTime.of(2025, 11, 2, 15, 0, 0, 0, ZoneOffset.UTC)) // exclude
                .build();

        when(jpa.findAllByStatusIn(List.of(NoteStatus.COMPLETED, NoteStatus.DELAYED)))
                .thenReturn(List.of(before, start, end, after));

        // When
        var result = adapter.findDone("2025-11-02");

        // Then (only start & end remain)
        assertEquals(2, result.size());
        var ids = result.stream().map(n -> n.getNoteId()).toList();
        assertEquals(List.of(2L, 3L), ids);
    }

    @Test
    @DisplayName("findDone(range): KST 범위 경계(UTC 포함)로 필터링하고 warehouseCode는 옵션이다")
    void findDone_range_filtersAndWarehouseOptional() {
        ShippingNoteJpaRepository jpa = Mockito.mock(ShippingNoteJpaRepository.class);
        ShippingNoteJpaRepositoryAdapter adapter = new ShippingNoteJpaRepositoryAdapter(jpa);

        var d1 = ShippingNoteEntity.builder()
                .noteId(10L).status(NoteStatus.COMPLETED).warehouseCode("서울")
                .requestedAt(OffsetDateTime.of(2025, 11, 1, 23, 0, 0, 0, ZoneOffset.UTC)) // KST 11/02 08:00
                .build();
        var d2 = ShippingNoteEntity.builder()
                .noteId(11L).status(NoteStatus.COMPLETED).warehouseCode("부산")
                .requestedAt(OffsetDateTime.of(2025, 11, 2, 23, 0, 0, 0, ZoneOffset.UTC)) // KST 11/03 08:00
                .build();
        var d3 = ShippingNoteEntity.builder()
                .noteId(12L).status(NoteStatus.DELAYED).warehouseCode("서울")
                .requestedAt(OffsetDateTime.of(2025, 11, 3, 15, 0, 0, 0, ZoneOffset.UTC)) // 상한 초과(UTC), KST 11/04 00:00 → 제외
                .build();

        when(jpa.findAllByStatusIn(List.of(NoteStatus.COMPLETED, NoteStatus.DELAYED)))
                .thenReturn(List.of(d1, d2, d3));

        // Range 2025-11-02..2025-11-03 (KST)
        var resultAll = adapter.findDone(null, "2025-11-02", "2025-11-03", null);
        assertEquals(2, resultAll.size()); // d1(KST 11/02), d2(KST 11/03)만 포함

        var resultSeoul = adapter.findDone(null, "2025-11-02", "2025-11-03", "서울");
        assertEquals(1, resultSeoul.size());
        assertEquals(10L, resultSeoul.get(0).getNoteId());
    }
}
