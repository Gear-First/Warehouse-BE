package com.gearfirst.warehouse.api.shipping.repository;

import com.gearfirst.warehouse.api.shipping.domain.*;
import com.gearfirst.warehouse.api.shipping.persistence.ShippingNoteJpaRepository;
import com.gearfirst.warehouse.api.shipping.persistence.entity.ShippingNoteEntity;
import com.gearfirst.warehouse.api.shipping.persistence.entity.ShippingNoteLineEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShippingNoteJpaRepositoryAdapter implements ShippingNoteRepository {

    private final ShippingNoteJpaRepository jpaRepository;

    @Override
    public List<ShippingNote> findNotDone(String date) {
        var notDone = jpaRepository.findAllByStatusNotIn(List.of(NoteStatus.COMPLETED, NoteStatus.DELAYED));
        // Optional date filter (yyyy-MM-dd) on createdAt; ignore if parsing fails or createdAt is null
        if (date != null && !date.isBlank()) {
            try {
                var target = java.time.LocalDate.parse(date);
                notDone = notDone.stream()
                        .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().toLocalDate().isEqual(target))
                        .toList();
            } catch (Exception ignored) { /* keep unfiltered on parse error */ }
        }
        return notDone.stream().map(this::toDomain).toList();
    }

    @Override
    public List<ShippingNote> findDone(String date) {
        var done = jpaRepository.findAllByStatusIn(List.of(NoteStatus.COMPLETED, NoteStatus.DELAYED));
        if (date != null && !date.isBlank()) {
            try {
                var target = java.time.LocalDate.parse(date);
                done = done.stream()
                        .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().toLocalDate().isEqual(target))
                        .toList();
            } catch (Exception ignored) { }
        }
        return done.stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ShippingNote> findById(Long noteId) {
        return jpaRepository.findById(noteId).map(this::toDomain);
    }

    @Override
    public ShippingNote save(ShippingNote note) {
        var entity = toEntity(note);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private ShippingNote toDomain(ShippingNoteEntity e) {
        return ShippingNote.builder()
                .noteId(e.getNoteId())
                .customerName(e.getCustomerName())
                .itemKindsNumber(e.getItemKindsNumber())
                .totalQty(e.getTotalQty())
                .status(e.getStatus())
                .completedAt(e.getCompletedAt() == null ? null : e.getCompletedAt().toString())
                .lines(e.getLines().stream().map(this::toDomainLine).toList())
                .build();
    }

    private ShippingNoteLine toDomainLine(ShippingNoteLineEntity le) {
        return ShippingNoteLine.builder()
                .lineId(le.getLineId())
                .productId(le.getProductId())
                .productLot(le.getProductLot())
                .productSerial(le.getProductSerial())
                .productName(le.getProductName())
                .productImgUrl(le.getProductImgUrl())
                .orderedQty(le.getOrderedQty())
                .allocatedQty(le.getAllocatedQty())
                .pickedQty(le.getPickedQty())
                .status(le.getStatus())
                .build();
    }

    private ShippingNoteEntity toEntity(ShippingNote d) {
        var builder = ShippingNoteEntity.builder()
                .noteId(d.getNoteId())
                .customerName(d.getCustomerName())
                .itemKindsNumber(d.getItemKindsNumber())
                .totalQty(d.getTotalQty())
                .status(d.getStatus())
                .completedAt(d.getCompletedAt() == null ? null : OffsetDateTime.parse(d.getCompletedAt()));
        var entity = builder.build();
        if (d.getLines() != null) {
            for (var dl : d.getLines()) {
                var le = ShippingNoteLineEntity.builder()
                        .lineId(dl.getLineId())
                        .productId(dl.getProductId())
                        .productLot(dl.getProductLot())
                        .productSerial(dl.getProductSerial())
                        .productName(dl.getProductName())
                        .productImgUrl(dl.getProductImgUrl())
                        .orderedQty(dl.getOrderedQty())
                        .allocatedQty(dl.getAllocatedQty())
                        .pickedQty(dl.getPickedQty())
                        .status(dl.getStatus())
                        .note(entity)
                        .build();
                entity.getLines().add(le);
            }
        }
        return entity;
    }
}
