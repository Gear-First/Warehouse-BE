package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.inventory.persistence.InventoryOnHandJpaRepository;
import com.gearfirst.warehouse.api.inventory.persistence.entity.InventoryOnHandEntity;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.persistence.ReceivingNoteJpaRepository;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteLineEntity;
import com.gearfirst.warehouse.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReceivingInventoryIntegrationTest {

    @Autowired
    private ReceivingService service;

    @Autowired
    private ReceivingNoteJpaRepository noteRepo;

    @Autowired
    private InventoryOnHandJpaRepository onHandRepo;

    private Long noteId;

    @BeforeEach
    void setUp() {
        // clean tables that this test touches
        onHandRepo.deleteAll();
        noteRepo.deleteAll();

        // Seed receiving note with 2 ACCEPTED and 1 REJECTED line
        var note = ReceivingNoteEntity.builder()
                .noteId(8801L)
                .supplierName("PG-Supplier")
                .warehouseCode("WHPG")
                .itemKindsNumber(3)
                .totalQty(20)
                .status(ReceivingNoteStatus.PENDING)
                .requestedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1))
                .inspectorName("WAREHOUSE")
                .inspectorDept("DEFAULT")
                .inspectorPhone("N/A")
                .build();
        note.addLine(ReceivingNoteLineEntity.builder()
                .lineId(880101L).productId(501L).productLot("LOT-501").productCode("P-501").productName("품목501").productImgUrl("/img/501")
                .orderedQty(5).inspectedQty(5).status(ReceivingLineStatus.ACCEPTED).build());
        note.addLine(ReceivingNoteLineEntity.builder()
                .lineId(880102L).productId(502L).productLot("LOT-502").productCode("P-502").productName("품목502").productImgUrl("/img/502")
                .orderedQty(7).inspectedQty(7).status(ReceivingLineStatus.ACCEPTED).build());
        note.addLine(ReceivingNoteLineEntity.builder()
                .lineId(880103L).productId(503L).productLot("LOT-503").productCode("P-503").productName("품목503").productImgUrl("/img/503")
                .orderedQty(8).inspectedQty(0).status(ReceivingLineStatus.REJECTED).build());
        noteRepo.save(note);
        this.noteId = note.getNoteId();
    }

    @Test
    @DisplayName("complete: ACCEPTED 라인 합만큼 WHPG 버킷이 증가하고 lastUpdatedAt이 설정된다")
    void complete_increasesInventoryBuckets() {
        var resp = service.complete(noteId);
        assertNotNull(resp.completedAt());
        // Expect rows for (WHPG,501) and (WHPG,502)
        var e501 = onHandRepo.findByWarehouseCodeAndPartId("WHPG", 501L).orElse(null);
        var e502 = onHandRepo.findByWarehouseCodeAndPartId("WHPG", 502L).orElse(null);
        var e503 = onHandRepo.findByWarehouseCodeAndPartId("WHPG", 503L).orElse(null);
        assertNotNull(e501, "on-hand for part 501 should be created");
        assertNotNull(e502, "on-hand for part 502 should be created");
        assertNull(e503, "rejected part should not create on-hand row");
        assertEquals(5, e501.getOnHandQty());
        assertEquals(7, e502.getOnHandQty());
        assertNotNull(e501.getLastUpdatedAt());
        assertNotNull(e502.getLastUpdatedAt());
    }

    @Test
    @DisplayName("complete: 재호출 409이며 on-hand 수량은 변하지 않는다")
    void complete_idempotency_inventoryNotDoubled() {
        // first completion
        service.complete(noteId);
        var before501 = onHandRepo.findByWarehouseCodeAndPartId("WHPG", 501L).map(InventoryOnHandEntity::getOnHandQty).orElse(0);
        var before502 = onHandRepo.findByWarehouseCodeAndPartId("WHPG", 502L).map(InventoryOnHandEntity::getOnHandQty).orElse(0);
        assertEquals(5, before501);
        assertEquals(7, before502);
        // second completion must 409 and quantities unchanged
        assertThrows(ConflictException.class, () -> service.complete(noteId));
        var after501 = onHandRepo.findByWarehouseCodeAndPartId("WHPG", 501L).map(InventoryOnHandEntity::getOnHandQty).orElse(0);
        var after502 = onHandRepo.findByWarehouseCodeAndPartId("WHPG", 502L).map(InventoryOnHandEntity::getOnHandQty).orElse(0);
        assertEquals(before501, after501);
        assertEquals(before502, after502);
    }
}
