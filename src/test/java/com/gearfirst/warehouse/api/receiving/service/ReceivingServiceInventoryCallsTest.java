package com.gearfirst.warehouse.api.receiving.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteRequest;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
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
        var resp = service.complete(noteId, ReceivingCompleteRequest.builder().inspectorName("WAREHOUSE").inspectorDept("DEFAULT").inspectorPhone("N/A").build());

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
        service.complete(noteId, ReceivingCompleteRequest.builder().inspectorName("WAREHOUSE").inspectorDept("DEFAULT").inspectorPhone("N/A").build());
        // verify once
        verify(inventory, times(1)).increase(eq("WH-T"), eq(acceptedPartId), eq(acceptedOrderedQty));

        // reset interactions for clear counting of second call
        Mockito.clearInvocations(inventory);

        // second completion should throw ConflictException and not call increase
        assertThrows(ConflictException.class, () -> service.complete(noteId, ReceivingCompleteRequest.builder().inspectorName("WAREHOUSE").inspectorDept("DEFAULT").inspectorPhone("N/A").build()));
        verify(inventory, never()).increase(any(), any(), anyInt());
    }

    @Test
    @DisplayName("updateLine: 진행 중 라인이 있으면 complete는 409이며 increase 호출 없음")
    void complete_conflict_whenNotAllDone_noIncrease() {
        // seed another PENDING note with one PENDING line (so not all decided)
        var n = ReceivingNoteEntity.builder()
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
                .productId(2001L).productLot("LOT-X").productCode("P-X").productName("부품X").productImgUrl("/img/X")
                .orderedQty(5).inspectedQty(0).status(ReceivingLineStatus.PENDING).build());
        n = jpa.save(n);
        Long anotherId = n.getNoteId();

        assertThrows(ConflictException.class, () -> service.complete(anotherId, ReceivingCompleteRequest.builder().inspectorName("WAREHOUSE").inspectorDept("DEFAULT").inspectorPhone("N/A").build()));
        verify(inventory, never()).increase(any(), any(), anyInt());
    }
}
