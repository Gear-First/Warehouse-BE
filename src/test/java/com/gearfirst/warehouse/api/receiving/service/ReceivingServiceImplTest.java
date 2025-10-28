package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteLineResponse;
import com.gearfirst.warehouse.api.receiving.dto.UpdateLineRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@ActiveProfiles("test-h2")
@Transactional
class ReceivingServiceImplTest {

    @Autowired
    private ReceivingService service;

    @Autowired
    private ReceivingNoteJpaRepository jpa;

    @BeforeEach
    void setUp() {
        // Clean and seed H2 with fixtures equivalent to previous mock store
        jpa.deleteAll();

        // noteId=101 (IN_PROGRESS) with lines: 1 REJECTED(50->48 issue2), 2 ACCEPTED(40), 3 PENDING(30)
        var n101 = ReceivingNoteEntity.builder()
                .noteId(101L)
                .supplierName("ABC Supply")
                .itemKindsNumber(3)
                .totalQty(120)
                .status(ReceivingNoteStatus.IN_PROGRESS)
                .build();
        n101.addLine(ReceivingNoteLineEntity.builder()
                .lineId(1L).productId(1L).productLot("LOT-P-1001").productCode("P-1001").productName("볼트").productImgUrl("/img/p1001")
                .orderedQty(50).inspectedQty(48).issueQty(2).status(ReceivingLineStatus.REJECTED).build());
        n101.addLine(ReceivingNoteLineEntity.builder()
                .lineId(2L).productId(2L).productLot("LOT-P-1001").productCode("P-1002").productName("너트").productImgUrl("/img/p1002")
                .orderedQty(40).inspectedQty(40).issueQty(0).status(ReceivingLineStatus.ACCEPTED).build());
        n101.addLine(ReceivingNoteLineEntity.builder()
                .lineId(3L).productId(3L).productLot("LOT-P-1001").productCode("P-1003").productName("와셔").productImgUrl("/img/p1003")
                .orderedQty(30).inspectedQty(0).issueQty(0).status(ReceivingLineStatus.PENDING).build());
        jpa.save(n101);

        // noteId=102 (PENDING)
        var n102 = ReceivingNoteEntity.builder()
                .noteId(102L)
                .supplierName("BCD Parts")
                .itemKindsNumber(2)
                .totalQty(45)
                .status(ReceivingNoteStatus.PENDING)
                .build();
        n102.addLine(ReceivingNoteLineEntity.builder()
                .lineId(10L).productId(4L).productLot("LOT-P-2001").productCode("P-2001").productName("스페이서").productImgUrl("/img/p2001")
                .orderedQty(20).inspectedQty(0).issueQty(0).status(ReceivingLineStatus.PENDING).build());
        n102.addLine(ReceivingNoteLineEntity.builder()
                .lineId(11L).productId(5L).productLot("LOT-P-2002").productCode("P-2002").productName("클립").productImgUrl("/img/p2002")
                .orderedQty(25).inspectedQty(0).issueQty(0).status(ReceivingLineStatus.PENDING).build());
        jpa.save(n102);

        // noteId=201 (COMPLETED_OK)
        var n201 = ReceivingNoteEntity.builder()
                .noteId(201L)
                .supplierName("ABC Supply")
                .itemKindsNumber(1)
                .totalQty(10)
                .status(ReceivingNoteStatus.COMPLETED_OK)
                .build();
        n201.addLine(ReceivingNoteLineEntity.builder()
                .lineId(20L).productId(6L).productLot("LOT-P-3001").productCode("P-3001").productName("가스켓").productImgUrl("/img/p3001")
                .orderedQty(10).inspectedQty(10).issueQty(0).status(ReceivingLineStatus.ACCEPTED).build());
        jpa.save(n201);
    }

    @Test
    @DisplayName("updateLine: hasIssue=false이면 ACCEPTED로 전이하고 note는 IN_PROGRESS")
    void updateLine_doneOk_success() {
        // given noteId=102 (PENDING), lineId=10 (PENDING, ordered=20)
        var resp = service.updateLine(102L, 10L, new UpdateLineRequest(18, false));
        assertEquals("IN_PROGRESS", resp.status());
        var line = findLine(resp, 10L);
        assertEquals(18, line.inspectedQty());
        assertEquals(0, line.issueQty());
        assertEquals("ACCEPTED", line.status());
    }

    @Test
    @DisplayName("updateLine: hasIssue=true이면 REJECTED로 전이하고 issueQty=ordered-inspected")
    void updateLine_doneIssue_success() {
        // given noteId=102 (PENDING), lineId=11 (PENDING, ordered=25)
        var resp = service.updateLine(102L, 11L, new UpdateLineRequest(20, true));
        assertEquals("IN_PROGRESS", resp.status());
        var line = findLine(resp, 11L);
        assertEquals(20, line.inspectedQty());
        assertEquals(5, line.issueQty());
        assertEquals("REJECTED", line.status());
    }

    @Test
    @DisplayName("updateLine: inspectedQty > orderedQty면 400 BadRequest")
    void updateLine_validationError_inspectedExceedsOrdered() {
        assertThrows(BadRequestException.class, () -> service.updateLine(101L, 3L, new UpdateLineRequest(1000, false)));
    }

    @Test
    @DisplayName("updateLine: 완료 라인 재수정 시 409 Conflict")
    void updateLine_conflict_whenLineAlreadyDone() {
        assertThrows(ConflictException.class, () -> service.updateLine(101L, 2L, new UpdateLineRequest(40, false)));
    }

    @Test
    @DisplayName("updateLine: 완료 전표(201)에서 수정 시 409 Conflict")
    void updateLine_conflict_whenNoteAlreadyDone() {
        assertThrows(ConflictException.class, () -> service.updateLine(201L, 20L, new UpdateLineRequest(10, false)));
    }

    @Test
    @DisplayName("updateLine: 존재하지 않는 lineId면 404 NotFound")
    void updateLine_notFound_whenLineMissing() {
        assertThrows(NotFoundException.class, () -> service.updateLine(102L, 999L, new UpdateLineRequest(1, false)));
    }

    private ReceivingNoteLineResponse findLine(ReceivingNoteDetailResponse resp, Long lineId) {
        return resp.lines().stream().filter(l -> l.lineId().equals(lineId)).findFirst().orElseThrow();
    }
}
