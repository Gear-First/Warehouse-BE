package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNoteLine;
import com.gearfirst.warehouse.api.shipping.repository.InMemoryShippingNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Verifies that completing a note which is (or becomes) DELAYED does NOT call inventory.decrease(...).
 */
class ShippingServiceNoDecreaseOnDelayedTest {

    private InMemoryShippingNoteRepository repo;
    private InventoryService inventory;
    private ShippingServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryShippingNoteRepository();
        inventory = Mockito.mock(InventoryService.class);
        OnHandProvider provider = productId -> 0; // force shortages if needed, but we pre-seed status anyway
        service = new ShippingServiceImpl(repo, provider, inventory);
    }

    @Test
    @DisplayName("complete: DELAYED 경로에서는 inventory.decrease가 호출되지 않는다")
    void complete_doesNotDecreaseOnDelayed() {
        // given: a note with one SHORTAGE line and handler info present
        var note = ShippingNote.builder()
                .noteId(9901L)
                .branchName("Branch-X")
                .itemKindsNumber(1)
                .totalQty(10)
                .warehouseCode("WHX")
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .status(NoteStatus.IN_PROGRESS)
                .completedAt(null)
                .lines(java.util.List.of(
                        ShippingNoteLine.builder()
                                .lineId(1L)
                                .productId(7001L)
                                .productLot("LOT")
                                .productCode("SER")
                                .productName("Bolt")
                                .productImgUrl("/img")
                                .orderedQty(10)
                                .pickedQty(5)
                                .status(LineStatus.SHORTAGE)
                                .build()
                ))
                .build();
        repo.save(note);

        // when
        var resp = service.complete(9901L);

        // then: no inventory decrease should be called
        verify(inventory, never()).decrease(any(), any(), anyInt());
        assertNotNull(resp.completedAt());
        // ensure the note became DELAYED
        var updated = repo.findById(9901L).orElseThrow();
        assertEquals(NoteStatus.DELAYED, updated.getStatus());
    }
}
