package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingUpdateLineRequest;
import com.gearfirst.warehouse.api.receiving.persistence.ReceivingNoteJpaRepository;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteLineEntity;
import com.gearfirst.warehouse.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class ReceivingServiceInventoryCallsTest {

    @Autowired
    private ReceivingService service;

    @Autowired
    private ReceivingNoteJpaRepository jpa;

    @MockBean
    private InventoryService inventory;

    private Long noteId;
    private Long acceptedPartId;
    private int acceptedOrderedQty;
    private Long rejectedPartId;

    @BeforeEach
    void setUp() {
        jpa.deleteAll();
        // Seed one note with warehouseCode + inspector present so completion precondition passes
        var note = ReceivingNoteEntity.builder()
                .noteId(777L)
                .supplierName("Supplier-X")
                .warehouseCode("WH-T")
                .itemKindsNumber(2)
                .totalQty(30)
                .status(ReceivingNoteStatus.PENDING)
                .inspectorName("WAREHOUSE")
                .inspectorDept("DEFAULT")
                .inspectorPhone("N/A")
                .build();
        // ACCEPTED candidate line
        acceptedPartId = 1001L;
        acceptedOrderedQty = 20;
        note.addLine(ReceivingNoteLineEntity.builder()
                .lineId(1L)
                .productId(acceptedPartId)
                .productLot("LOT-A")
                .productCode("P-A")
                .productName("부품A")
                .productImgUrl("/img/A")
                .orderedQty(acceptedOrderedQty)
                .inspectedQty(acceptedOrderedQty)
                .status(ReceivingLineStatus.ACCEPTED)
                .build());
        // REJECTED line
        rejectedPartId = 1002L;
        note.addLine(ReceivingNoteLineEntity.builder()
                .lineId(2L)
                .productId(rejectedPartId)
                .productLot("LOT-B")
                .productCode("P-B")
                .productName("부품B")
                .productImgUrl("/img/B")
                .orderedQty(10)
                .inspectedQty(0)
                .status(ReceivingLineStatus.REJECTED)
                .build());
        jpa.save(note);
        this.noteId = note.getNoteId();
    }

    @Test
    @DisplayName("complete: ACCEPTED 라인만 (warehouseCode, partId, orderedQty) 기준으로 increase 호출")
    void complete_callsInventoryIncreaseForAcceptedOnly() {
        // when
        var resp = service.complete(noteId);

        // then (verify inventory increase called exactly once for the ACCEPTED line)
        verify(inventory, times(1)).increase(eq("WH-T"), eq(acceptedPartId), eq(acceptedOrderedQty));
        // never for REJECTED line
        verify(inventory, never()).increase(eq("WH-T"), eq(rejectedPartId), anyInt());
        // no other calls
        verifyNoMoreInteractions(inventory);
    }

    @Test
    @DisplayName("complete: 재호출 시 409이며 추가 increase 호출이 없어야 한다")
    void complete_idempotency_noDoubleIncrease() {
        // first completion
        service.complete(noteId);
        // verify once
        verify(inventory, times(1)).increase(eq("WH-T"), eq(acceptedPartId), eq(acceptedOrderedQty));

        // reset interactions for clear counting of second call
        Mockito.clearInvocations(inventory);

        // second completion should throw ConflictException and not call increase
        assertThrows(ConflictException.class, () -> service.complete(noteId));
        verify(inventory, never()).increase(any(), any(), anyInt());
    }

    @Test
    @DisplayName("updateLine: 진행 중 라인이 있으면 complete는 409이며 increase 호출 없음")
    void complete_conflict_whenNotAllDone_noIncrease() {
        // seed another PENDING note with one PENDING line (so not all decided)
        var n = ReceivingNoteEntity.builder()
                .noteId(778L)
                .supplierName("Supplier-Y")
                .warehouseCode("WH-T")
                .itemKindsNumber(1)
                .totalQty(5)
                .status(ReceivingNoteStatus.PENDING)
                .inspectorName("WAREHOUSE")
                .inspectorDept("DEFAULT")
                .inspectorPhone("N/A")
                .build();
        n.addLine(ReceivingNoteLineEntity.builder()
                .lineId(10L).productId(2001L).productLot("LOT-X").productCode("P-X").productName("부품X").productImgUrl("/img/X")
                .orderedQty(5).inspectedQty(0).status(ReceivingLineStatus.PENDING).build());
        jpa.save(n);

        assertThrows(ConflictException.class, () -> service.complete(778L));
        verify(inventory, never()).increase(any(), any(), anyInt());
    }
}
