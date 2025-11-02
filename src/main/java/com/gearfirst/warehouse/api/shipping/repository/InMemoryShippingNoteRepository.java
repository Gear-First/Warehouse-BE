package com.gearfirst.warehouse.api.shipping.repository;

import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNoteLine;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryShippingNoteRepository implements ShippingNoteRepository {

    private static final Map<Long, ShippingNote> NOTES = new ConcurrentHashMap<>();

    static {
        // noteId=501 (IN_PROGRESS)
        NOTES.put(501L, ShippingNote.builder()
                .noteId(501L)
                .branchName("ACME Corp")
                .itemKindsNumber(3)
                .totalQty(90)
                .status(NoteStatus.IN_PROGRESS)
                .completedAt(null)
                .lines(List.of(
                        ShippingNoteLine.builder().lineId(1L).productId(1001L).productLot("LOT-S-1001")
                                .productCode("S-1001").productName("볼트").productImgUrl("/img/s1001").orderedQty(30)
                                .pickedQty(15).status(LineStatus.PENDING).build(),
                        ShippingNoteLine.builder().lineId(2L).productId(1002L).productLot("LOT-S-1002")
                                .productCode("S-1002").productName("너트").productImgUrl("/img/s1002").orderedQty(30)
                                .pickedQty(30).status(LineStatus.READY).build(),
                        ShippingNoteLine.builder().lineId(3L).productId(1003L).productLot("LOT-S-1003")
                                .productCode("S-1003").productName("와셔").productImgUrl("/img/s1003").orderedQty(30)
                                .pickedQty(25).status(LineStatus.SHORTAGE).build()
                ))
                .build());
        // noteId=502 (PENDING)
        NOTES.put(502L, ShippingNote.builder()
                .noteId(502L)
                .branchName("Beta Ltd")
                .itemKindsNumber(2)
                .totalQty(40)
                .status(NoteStatus.PENDING)
                .completedAt(null)
                .lines(List.of(
                        ShippingNoteLine.builder().lineId(10L).productId(2001L).productLot("LOT-S-2001")
                                .productCode("S-2001").productName("스페이서").productImgUrl("/img/s2001").orderedQty(20)
                                .pickedQty(0).status(LineStatus.PENDING).build(),
                        ShippingNoteLine.builder().lineId(11L).productId(2002L).productLot("LOT-S-2002")
                                .productCode("S-2002").productName("클립").productImgUrl("/img/s2002").orderedQty(20)
                                .pickedQty(0).status(LineStatus.PENDING).build()
                ))
                .build());
        // noteId=601 (COMPLETED)
        NOTES.put(601L, ShippingNote.builder()
                .noteId(601L)
                .branchName("ACME Corp")
                .itemKindsNumber(1)
                .totalQty(10)
                .status(NoteStatus.COMPLETED)
                .completedAt("2025-10-20-12:00:00")
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .lines(List.of(
                        ShippingNoteLine.builder().lineId(20L).productId(3001L).productLot("LOT-S-3001")
                                .productCode("S-3001").productName("가스켓").productImgUrl("/img/s3001").orderedQty(10)
                                .pickedQty(10).status(LineStatus.READY).build()
                ))
                .build());
        // noteId=602 (DELAYED)
        NOTES.put(602L, ShippingNote.builder()
                .noteId(602L)
                .branchName("ACME Corp")
                .itemKindsNumber(1)
                .totalQty(10)
                .status(NoteStatus.DELAYED)
                .completedAt("2025-10-20-15:00:00")
                .assigneeName("WAREHOUSE")
                .assigneeDept("DEFAULT")
                .assigneePhone("N/A")
                .lines(List.of(
                        ShippingNoteLine.builder().lineId(21L).productId(3002L).productLot("LOT-S-3002")
                                .productCode("S-3002").productName("와이어A").productImgUrl("/img/s3002").orderedQty(10)
                                .pickedQty(8).status(LineStatus.SHORTAGE).build()
                ))
                .build());
    }

    @Override
    public List<ShippingNote> findNotDone(String date) {
        return NOTES.values().stream()
                .filter(n -> n.getStatus() != NoteStatus.COMPLETED && n.getStatus() != NoteStatus.DELAYED)
                .sorted(Comparator.comparing(ShippingNote::getNoteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ShippingNote> findDone(String date) {
        return NOTES.values().stream()
                .filter(n -> n.getStatus() == NoteStatus.COMPLETED || n.getStatus() == NoteStatus.DELAYED)
                .sorted(Comparator.comparing(ShippingNote::getNoteId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ShippingNote> findById(Long noteId) {
        return Optional.ofNullable(NOTES.get(noteId));
    }

    @Override
    public ShippingNote save(ShippingNote note) {
        NOTES.put(note.getNoteId(), note);
        return note;
    }
}
