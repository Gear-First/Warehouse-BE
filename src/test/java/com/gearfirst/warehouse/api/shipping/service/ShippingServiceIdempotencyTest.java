package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNoteLine;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteRequest;
import com.gearfirst.warehouse.api.shipping.repository.InMemoryShippingNoteRepository;
import com.gearfirst.warehouse.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Verifies idempotency guard on Shipping completion: once a note is in a terminal
 * state (COMPLETED/DELAYED), subsequent complete() calls must 409 and must not
 * call inventory.decrease again.
 */
class ShippingServiceIdempotencyTest {

    private InMemoryShippingNoteRepository repo;
    private InventoryService inventory;
    private ShippingServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryShippingNoteRepository();
        inventory = Mockito.mock(InventoryService.class);
        OnHandProvider provider = (wh, productId) -> Integer.MAX_VALUE / 2; // plenty in stock; not used in completion
        KafkaTemplate<String, Object> kafka = Mockito.mock(KafkaTemplate.class);
        service = new ShippingServiceImpl(repo, provider, inventory, null, null, kafka);
    }

    @Test
    @DisplayName("complete: 두 번째 호출은 409이고 inventory.decrease는 추가 호출되지 않는다")
    void complete_secondCall409_noAdditionalDecrease() {
        // given: an IN_PROGRESS note with one READY line and handler info present
        var note = ShippingNote.builder()
                .noteId(7101L)
                .branchName("QA")
                .itemKindsNumber(1)
                .totalQty(5)
                .warehouseCode("WH-QA")
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .status(NoteStatus.IN_PROGRESS)
                .completedAt(null)
                .lines(java.util.List.of(
                        ShippingNoteLine.builder()
                                .lineId(1L)
                                .productId(3001L)
                                .productLot("LOT")
                                .productCode("SER")
                                .productName("Bolt")
                                .productImgUrl("/img")
                                .orderedQty(5)
                                .pickedQty(5)
                                .status(LineStatus.READY)
                                .build()
                ))
                .build();
        repo.save(note);

        // when: first completion succeeds and calls inventory.decrease once
        var req = ShippingCompleteRequest.builder().assigneeName("WAREHOUSE").assigneeDept("DEFAULT").assigneePhone("N/A").build();
        var resp1 = service.complete(7101L, req);
        assertNotNull(resp1.completedAt());
        verify(inventory, times(1)).decrease(eq("WH-QA"), eq(3001L), eq(5));

        // reset interactions for a clear count on the second call
        Mockito.clearInvocations(inventory);

        // then: second completion should 409 and must NOT call decrease
        assertThrows(ConflictException.class, () -> service.complete(7101L, req));
        verify(inventory, never()).decrease(any(), any(), anyInt());
    }
}
