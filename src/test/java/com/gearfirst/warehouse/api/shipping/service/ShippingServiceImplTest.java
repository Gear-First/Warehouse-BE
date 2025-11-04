package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteRequest;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNoteLine;
import com.gearfirst.warehouse.api.shipping.repository.InMemoryShippingNoteRepository;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class ShippingServiceImplTest {

    private InMemoryShippingNoteRepository repo;
    private ShippingServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryShippingNoteRepository();
        OnHandProvider provider = productId -> Integer.MAX_VALUE / 2; // default: sufficient stock
        service = new ShippingServiceImpl(repo, provider);
    }

    @Test
    @DisplayName("getNotDone: PENDING/IN_PROGRESS만 반환한다")
    void getNotDone_returnsNotDone() {
        var list = service.getNotDone(null);
        assertThat(list, hasSize(greaterThanOrEqualTo(2)));
        assertThat(list.stream().map(l -> l.status()).toList(), everyItem(is(in(List.of("PENDING", "IN_PROGRESS")))));
    }

    @Test
    @DisplayName("getDone: COMPLETED/DELAYED만 반환한다")
    void getDone_returnsDone() {
        var list = service.getDone(null);
        assertThat(list, hasSize(greaterThanOrEqualTo(2)));
        assertThat(list.stream().map(l -> l.status()).toList(), everyItem(is(in(List.of("COMPLETED", "DELAYED")))));
    }

    @Test
    @DisplayName("updateLine: PENDING 노트는 IN_PROGRESS로 전이되고 라인 값이 반영된다")
    void updateLine_transitionsAndUpdates() {
        // noteId=502 (PENDING), lineId=10 존재
        var updated = service.updateLine(502L, 10L, new ShippingUpdateLineRequest(3));
        assertEquals("IN_PROGRESS", updated.status());
        var line = updated.lines().stream().filter(l -> l.lineId().equals(10L)).findFirst().orElseThrow();
        assertEquals(3, line.pickedQty());
        assertEquals("PENDING", line.status());
    }

    @Test
    @DisplayName("updateLine: pickedQty > orderedQty면 BadRequestException")
    void updateLine_ruleViolation() {
        assertThrows(BadRequestException.class, () -> service.updateLine(501L, 1L, new ShippingUpdateLineRequest(999)));
    }

    @Test
    @DisplayName("complete: 모든 라인이 READY면 COMPLETED와 completedAt 반환")
    void complete_completed() {
        // noteId=601 은 이미 모든 라인이 READY
        var resp = service.complete(601L, ShippingCompleteRequest.builder().assigneeName("WAREHOUSE").assigneeDept("DEFAULT").assigneePhone("N/A").build());
        assertNotNull(resp.completedAt());
        assertEquals(10, resp.totalShippedQty());
    }

    @Test
    @DisplayName("complete: SHORTAGE 포함이면 DELAYED와 completedAt 반환")
    void complete_delayed() {
        var resp = service.complete(602L, ShippingCompleteRequest.builder().assigneeName("WAREHOUSE").assigneeDept("DEFAULT").assigneePhone("N/A").build());
        assertNotNull(resp.completedAt());
    }

    @Test
    @DisplayName("complete: SHORTAGE 없고 READY만 아님 -> 409 Conflict 발생")
    void complete_conflict_whenNotAllReady() {
        // 임시 노트 생성: READY + PENDING (SHORTAGE 없음)
        var temp = ShippingNote.builder()
                .noteId(9001L)
                .branchName("Temp")
                .itemKindsNumber(2)
                .totalQty(20)
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .status(NoteStatus.IN_PROGRESS)
                .completedAt(null)
                .lines(List.of(
                        ShippingNoteLine.builder().lineId(900101L).productId(1L).productLot("L").productCode("S").productName("A").productImgUrl("/")
                                .orderedQty(10).pickedQty(10).status(LineStatus.READY).build(),
                        ShippingNoteLine.builder().lineId(900102L).productId(2L).productLot("L").productCode("S").productName("B").productImgUrl("/")
                                .orderedQty(10).pickedQty(0).status(LineStatus.PENDING).build()
                ))
                .build();
        repo.save(temp);

        assertThrows(ConflictException.class, () -> service.complete(9001L, ShippingCompleteRequest.builder().assigneeName("WAREHOUSE").assigneeDept("DEFAULT").assigneePhone("N/A").build()));
    }

    @Test
    @DisplayName("updateLine: pickedQty가 orderedQty를 초과하면 BadRequestException")
    void updateLine_pickedExceedsOrdered() {
        // picked>ordered(20) 위반이므로 400을 기대
        assertThrows(BadRequestException.class, () -> service.updateLine(502L, 10L, new ShippingUpdateLineRequest(999)));
    }

    @Test
    @DisplayName("updateLine: 존재하지 않는 lineId면 NotFoundException")
    void updateLine_lineNotFound() {
        assertThrows(NotFoundException.class, () -> service.updateLine(502L, 999L, new ShippingUpdateLineRequest(1)));
    }
}
