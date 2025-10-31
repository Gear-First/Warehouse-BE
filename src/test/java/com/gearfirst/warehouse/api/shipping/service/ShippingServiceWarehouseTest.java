package com.gearfirst.warehouse.api.shipping.service;

import static com.gearfirst.warehouse.common.response.PageEnvelope.*;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.*;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.PartRef;
import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNoteLine;
import com.gearfirst.warehouse.api.shipping.repository.InMemoryShippingNoteRepository;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies multi-warehouse behavior for Inventory decreases during Shipping completion.
 */
class ShippingServiceWarehouseTest {

    private InMemoryShippingNoteRepository repo;
    private InventoryBackedOnHandProvider provider;
    private InventoryService inventory;
    private ShippingServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryShippingNoteRepository();
        inventory = new FakeInventoryService();
        provider = new InventoryBackedOnHandProvider(inventory);
        service = new ShippingServiceImpl(repo, provider, inventory);
    }

    @Test
    @DisplayName("complete: decreases only the matching warehouse bucket")
    void complete_decreasesOnlyMatchingWarehouse() {
        // given: note in warehouse 1, one READY line with pickedQty=10
        var note = ShippingNote.builder()
                .noteId(8001L)
                .branchName("WH Test")
                .itemKindsNumber(1)
                .totalQty(10)
                .warehouseCode("1")
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .status(NoteStatus.IN_PROGRESS)
                .completedAt(null)
                .lines(List.of(ShippingNoteLine.builder()
                        .lineId(1L)
                        .productId(1001L)
                        .productLot("LOT")
                        .productSerial("SER")
                        .productName("Bolt")
                        .productImgUrl("/img")
                        .orderedQty(10)
                        .pickedQty(10)
                        .status(LineStatus.READY)
                        .build()))
                .build();
        repo.save(note);

        // seed inventory: +10 in WH1, +10 in WH2 (ensure we only consume WH1)
        inventory.increase("1", 1001L, 10);
        inventory.increase("2", 1001L, 10);

        // when: complete
        var resp = service.complete(8001L);

        // then: WH1 becomes 0, WH2 remains 10
        var wh1 = inventory.listOnHand("1", null, null, null, null, 0, 100, java.util.List.of()).getItems().stream()
                .filter(i -> i.part().id().equals(1001L))
                .findFirst().orElseThrow();
        assertEquals(0, wh1.onHandQty());

        var wh2 = inventory.listOnHand("2", null, null, null, null, 0, 100, java.util.List.of()).getItems().stream()
                .filter(i -> i.part().id().equals(1001L))
                .findFirst().orElseThrow();
        assertEquals(10, wh2.onHandQty());

        assertNotNull(resp.completedAt());
        assertEquals(10, resp.totalShippedQty());
    }

    @Test
    @DisplayName("complete: insufficient stock in that warehouse → CONFLICT_INVENTORY_INSUFFICIENT")
    void complete_conflict_whenInsufficientInWarehouse() {
        // given: note in warehouse 5, READY line pickedQty=7, but inventory has only 5 in WH5
        var note = ShippingNote.builder()
                .noteId(8002L)
                .branchName("WH Test")
                .itemKindsNumber(1)
                .totalQty(7)
                .warehouseCode("5")
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .status(NoteStatus.IN_PROGRESS)
                .completedAt(null)
                .lines(List.of(ShippingNoteLine.builder()
                        .lineId(1L)
                        .productId(2001L)
                        .productLot("LOT")
                        .productSerial("SER")
                        .productName("Nut")
                        .productImgUrl("/img")
                        .orderedQty(7)
                        .pickedQty(7)
                        .status(LineStatus.READY)
                        .build()))
                .build();
        repo.save(note);

        inventory.increase("5", 2001L, 5); // insufficient

        // when/then
        ConflictException ex = assertThrows(ConflictException.class, () -> service.complete(8002L));
        assertEquals(ErrorStatus.CONFLICT_INVENTORY_INSUFFICIENT.getMessage(), ex.getMessage());
    }

    /**
     * Simple OnHandProvider backed by InventoryService for derived status (not strictly needed in these tests).
     */
    private record InventoryBackedOnHandProvider(InventoryService inventory)
            implements OnHandProvider {
        @Override public int getOnHandQty(Long productId) {
            // Sum across all warehouses for simplicity in status derivation (not used in these tests)
            return inventory.listOnHand(null, null, null, null, null, 0, 100, java.util.List.of()).getItems().stream()
                    .filter(i -> i.part().id().equals(productId))
                    .mapToInt(i -> i.onHandQty()).sum();
        }
    }

    // Simple in-memory fake implementing InventoryService for unit tests
    private static class FakeInventoryService implements InventoryService {
        private static final class Key {
            final String wh; final Long part;
            Key(String wh, Long part){ this.wh = wh; this.part = part; }
            @Override public boolean equals(Object o){ if(this==o) return true; if(!(o instanceof Key k)) return false; return java.util.Objects.equals(wh,k.wh) && java.util.Objects.equals(part,k.part);} 
            @Override public int hashCode(){ return java.util.Objects.hash(wh,part);} }
        private final java.util.concurrent.ConcurrentHashMap<Key,Integer> map = new java.util.concurrent.ConcurrentHashMap<>();
        @Override
        public PageEnvelope<OnHandSummary> listOnHand(String warehouseCode, String partKeyword, String supplierName, Integer minQty, Integer maxQty, int page, int size, java.util.List<String> sort) {
            var now = OffsetDateTime.now(UTC).toString();
            var list = new ArrayList<OnHandSummary>();
            for (var e : map.entrySet()) {
                var key = e.getKey();
                if (warehouseCode != null && !warehouseCode.isBlank() && !java.util.Objects.equals(warehouseCode, key.wh)) continue;
                var part = new PartRef(key.part, "P-"+key.part, null);
                list.add(new OnHandSummary(key.wh, part, e.getValue(), now));
            }
            return of(list, 0, list.size(), list.size());
        }
        @Override
        public void increase(String warehouseCode, Long partId, int qty) {
            if (qty<=0||partId==null) return; var k=new Key(warehouseCode,partId); map.merge(k, qty, Integer::sum);
        }
        @Override
        public void decrease(String warehouseCode, Long partId, int qty) {
            if (qty<=0||partId==null) return; var k=new Key(warehouseCode,partId);
            map.compute(k,(kk,v)->{ int cur=v==null?0:v; if(cur<qty) throw new com.gearfirst.warehouse.common.exception.ConflictException(com.gearfirst.warehouse.common.response.ErrorStatus.CONFLICT_INVENTORY_INSUFFICIENT); return cur-qty;});
        }
    }
}
