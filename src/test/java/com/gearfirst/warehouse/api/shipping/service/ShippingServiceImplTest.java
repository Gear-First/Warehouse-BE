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
        OnHandProvider provider = (wh, productId) -> Integer.MAX_VALUE / 2; // default: sufficient stock
        com.gearfirst.warehouse.api.inventory.service.InventoryService inv = new com.gearfirst.warehouse.api.inventory.service.InventoryService() {
            @Override
            public com.gearfirst.warehouse.common.response.PageEnvelope<com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary> listOnHand(String warehouseCode, String partKeyword, String supplierName, Integer minQty, Integer maxQty, int page, int size, java.util.List<String> sort) {
                return com.gearfirst.warehouse.common.response.PageEnvelope.of(java.util.List.of(), 0, 0, 0);
            }
            @Override public void increase(String warehouseCode, Long partId, int qty) { }
            @Override public void decrease(String warehouseCode, Long partId, int qty) { }
        };
        service = new ShippingServiceImpl(repo, provider, inv, null, null);
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
        // 비종단(IN_PROGRESS) 전표를 직접 시드: READY 라인만 존재
        var note = ShippingNote.builder()
                .noteId(9601L)
                .branchName("Temp")
                .itemKindsNumber(1)
                .totalQty(10)
                .warehouseCode("WH-T")
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .status(NoteStatus.IN_PROGRESS)
                .completedAt(null)
                .lines(List.of(
                        ShippingNoteLine.builder()
                                .lineId(960101L)
                                .productId(1234L)
                                .productLot("LOT")
                                .productCode("S-1234")
                                .productName("Bolt")
                                .productImgUrl("/img")
                                .orderedQty(10)
                                .pickedQty(10)
                                .status(LineStatus.READY)
                                .build()
                ))
                .build();
        repo.save(note);

        var resp = service.complete(9601L, ShippingCompleteRequest.builder().assigneeName("WAREHOUSE").assigneeDept("DEFAULT").assigneePhone("N/A").build());
        assertNotNull(resp.completedAt());
        assertEquals(10, resp.totalShippedQty());
    }

    @Test
    @DisplayName("complete: SHORTAGE 포함이면 409 CONFLICT_CANNOT_COMPLETE_WHEN_NOT_READY")
    void complete_delayed_conflict() {
        // 비종단(IN_PROGRESS) 전표를 직접 시드: SHORTAGE 라인 존재
        var note = ShippingNote.builder()
                .noteId(9602L)
                .branchName("Temp")
                .itemKindsNumber(1)
                .totalQty(10)
                .warehouseCode("WH-T")
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .status(NoteStatus.IN_PROGRESS)
                .completedAt(null)
                .lines(List.of(
                        ShippingNoteLine.builder()
                                .lineId(960201L)
                                .productId(2234L)
                                .productLot("LOT")
                                .productCode("S-2234")
                                .productName("Nut")
                                .productImgUrl("/img")
                                .orderedQty(10)
                                .pickedQty(5)
                                .status(LineStatus.SHORTAGE)
                                .build()
                ))
                .build();
        repo.save(note);

        assertThrows(com.gearfirst.warehouse.common.exception.ConflictException.class,
                () -> service.complete(9602L, ShippingCompleteRequest.builder().assigneeName("WAREHOUSE").assigneeDept("DEFAULT").assigneePhone("N/A").build()));
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
