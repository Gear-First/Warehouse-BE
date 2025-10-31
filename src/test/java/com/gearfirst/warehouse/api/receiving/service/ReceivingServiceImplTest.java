package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteLineResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingUpdateLineRequest;
import com.gearfirst.warehouse.api.receiving.persistence.ReceivingNoteJpaRepository;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteLineEntity;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@ActiveProfiles("test-h2")
@Transactional
class ReceivingServiceImplTest {

    @Autowired
    private ReceivingService service;

    @Autowired
    private ReceivingNoteJpaRepository jpa;

    // Captured IDs after auto-generation
    private Long n101Id;
    private Long n101_lineRejectedId;
    private Long n101_lineAcceptedId;
    private Long n101_linePendingId;

    private Long n102Id;
    private Long n102_lineProd4Id;
    private Long n102_lineProd5Id;

    private Long n201Id;

    @BeforeEach
    void setUp() {
        // Clean and seed PostgreSQL with fixtures
        jpa.deleteAll();

        // IN_PROGRESS note with 3 lines: REJECTED(50->48), ACCEPTED(40), PENDING(30)
        var n101 = ReceivingNoteEntity.builder()
                .supplierName("ABC Supply")
                .itemKindsNumber(3)
                .totalQty(120)
                .status(ReceivingNoteStatus.IN_PROGRESS)
                .build();
        n101.addLine(ReceivingNoteLineEntity.builder()
                .productId(1L).productLot("LOT-P-1001").productCode("P-1001").productName("볼트").productImgUrl("/img/p1001")
                .orderedQty(50).inspectedQty(48).status(ReceivingLineStatus.REJECTED).build());
        n101.addLine(ReceivingNoteLineEntity.builder()
                .productId(2L).productLot("LOT-P-1001").productCode("P-1002").productName("너트").productImgUrl("/img/p1002")
                .orderedQty(40).inspectedQty(40).status(ReceivingLineStatus.ACCEPTED).build());
        n101.addLine(ReceivingNoteLineEntity.builder()
                .productId(3L).productLot("LOT-P-1001").productCode("P-1003").productName("와셔").productImgUrl("/img/p1003")
                .orderedQty(30).inspectedQty(0).status(ReceivingLineStatus.PENDING).build());
        n101 = jpa.save(n101);
        this.n101Id = n101.getNoteId();
        this.n101_lineRejectedId = n101.getLines().stream().filter(l -> l.getProductId().equals(1L)).findFirst().get().getLineId();
        this.n101_lineAcceptedId = n101.getLines().stream().filter(l -> l.getProductId().equals(2L)).findFirst().get().getLineId();
        this.n101_linePendingId  = n101.getLines().stream().filter(l -> l.getProductId().equals(3L)).findFirst().get().getLineId();

        // PENDING note with 2 PENDING lines
        var n102 = ReceivingNoteEntity.builder()
                .supplierName("BCD Parts")
                .itemKindsNumber(2)
                .totalQty(45)
                .status(ReceivingNoteStatus.PENDING)
                .build();
        n102.addLine(ReceivingNoteLineEntity.builder()
                .productId(4L).productLot("LOT-P-2001").productCode("P-2001").productName("스페이서").productImgUrl("/img/p2001")
                .orderedQty(20).inspectedQty(0).status(ReceivingLineStatus.PENDING).build());
        n102.addLine(ReceivingNoteLineEntity.builder()
                .productId(5L).productLot("LOT-P-2002").productCode("P-2002").productName("클립").productImgUrl("/img/p2002")
                .orderedQty(25).inspectedQty(0).status(ReceivingLineStatus.PENDING).build());
        n102 = jpa.save(n102);
        this.n102Id = n102.getNoteId();
        this.n102_lineProd4Id = n102.getLines().stream().filter(l -> l.getProductId().equals(4L)).findFirst().get().getLineId();
        this.n102_lineProd5Id = n102.getLines().stream().filter(l -> l.getProductId().equals(5L)).findFirst().get().getLineId();

        // COMPLETED_OK note
        var n201 = ReceivingNoteEntity.builder()
                .supplierName("ABC Supply")
                .itemKindsNumber(1)
                .totalQty(10)
                .status(ReceivingNoteStatus.COMPLETED_OK)
                .build();
        n201.addLine(ReceivingNoteLineEntity.builder()
                .productId(6L).productLot("LOT-P-3001").productCode("P-3001").productName("가스켓").productImgUrl("/img/p3001")
                .orderedQty(10).inspectedQty(10).status(ReceivingLineStatus.ACCEPTED).build());
        n201 = jpa.save(n201);
        this.n201Id = n201.getNoteId();
    }

    @Test
    @DisplayName("updateLine: hasIssue=false이면 ACCEPTED로 전이하고 note는 IN_PROGRESS")
    void updateLine_doneOk_success() {
        var resp = service.updateLine(n102Id, n102_lineProd4Id, new ReceivingUpdateLineRequest(18, false));
        assertEquals("IN_PROGRESS", resp.status());
        var line = findLineById(resp, n102_lineProd4Id);
        assertEquals(18, line.inspectedQty());
        assertEquals("ACCEPTED", line.status());
    }

    @Test
    @DisplayName("updateLine: hasIssue=true이면 REJECTED로 전이하고 issueQty=ordered-inspected")
    void updateLine_doneIssue_success() {
        var resp = service.updateLine(n102Id, n102_lineProd5Id, new ReceivingUpdateLineRequest(20, true));
        assertEquals("IN_PROGRESS", resp.status());
        var line = findLineById(resp, n102_lineProd5Id);
        assertEquals(20, line.inspectedQty());
        assertEquals("REJECTED", line.status());
    }

    @Test
    @DisplayName("updateLine: inspectedQty > orderedQty면 400 BadRequest")
    void updateLine_validationError_inspectedExceedsOrdered() {
        assertThrows(BadRequestException.class, () -> service.updateLine(n101Id, n101_linePendingId, new ReceivingUpdateLineRequest(1000, false)));
    }

    @Test
    @DisplayName("updateLine: 완료 라인 재수정 시 409 Conflict")
    void updateLine_conflict_whenLineAlreadyDone() {
        assertThrows(ConflictException.class, () -> service.updateLine(n101Id, n101_lineAcceptedId, new ReceivingUpdateLineRequest(40, false)));
    }

    @Test
    @DisplayName("updateLine: 완료 전표(201)에서 수정 시 409 Conflict")
    void updateLine_conflict_whenNoteAlreadyDone() {
        // use any line of the completed note n201
        assertThrows(ConflictException.class, () -> service.updateLine(n201Id, Long.MAX_VALUE, new ReceivingUpdateLineRequest(10, false)));
    }

    @Test
    @DisplayName("updateLine: 존재하지 않는 lineId면 404 NotFound")
    void updateLine_notFound_whenLineMissing() {
        assertThrows(NotFoundException.class, () -> service.updateLine(n102Id, Long.MAX_VALUE, new ReceivingUpdateLineRequest(1, false)));
    }

    private ReceivingNoteLineResponse findLineById(ReceivingNoteDetailResponse resp, Long lineId) {
        return resp.lines().stream().filter(l -> l.lineId().equals(lineId)).findFirst().orElseThrow();
    }
}
