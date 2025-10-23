package com.gearfirst.warehouse.api.shipping.service;

import com.gearfirst.warehouse.api.shipping.dto.*;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.api.shipping.domain.*;
import com.gearfirst.warehouse.api.shipping.repository.ShippingNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingNoteRepository repository;

    @Override
    public List<ShippingNoteSummaryResponse> getNotDone(String date) {
        return repository.findNotDone(date).stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<ShippingNoteSummaryResponse> getDone(String date) {
        return repository.findDone(date).stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public ShippingNoteDetailResponse getDetail(Long noteId) {
        var note = repository.findById(noteId).orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));
        return toDetail(note);
    }

    @Override
    public ShippingNoteDetailResponse updateLine(Long noteId, Long lineId, UpdateLineRequest request) {
        var note = repository.findById(noteId).orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));

        if (request.pickedQty() > request.allocatedQty()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_PICKED_QTY_EXCEEDS_ALLOCATED_QTY);
        }

        List<ShippingNoteLine> newLines = new ArrayList<>();
        for (var l : note.getLines()) {
            if (l.getLineId().equals(lineId)) {
                newLines.add(ShippingNoteLine.builder()
                        .lineId(l.getLineId())
                        .productId(l.getProductId())
                        .productLot(l.getProductLot())
                        .productSerial(l.getProductSerial())
                        .productName(l.getProductName())
                        .productImgUrl(l.getProductImgUrl())
                        .orderedQty(l.getOrderedQty())
                        .allocatedQty(request.allocatedQty())
                        .pickedQty(request.pickedQty())
                        .status(LineStatus.valueOf(request.status()))
                        .build());
            } else {
                newLines.add(l);
            }
        }

        var newStatus = note.getStatus();
        if (newStatus == NoteStatus.PENDING) newStatus = NoteStatus.IN_PROGRESS;

        var updated = ShippingNote.builder()
                .noteId(note.getNoteId())
                .customerName(note.getCustomerName())
                .itemKindsNumber(note.getItemKindsNumber())
                .totalQty(note.getTotalQty())
                .status(newStatus)
                .completedAt(note.getCompletedAt())
                .lines(newLines)
                .build();
        repository.save(updated);
        return toDetail(updated);
    }

    @Override
    public ShippingCompleteResponse complete(Long noteId) {
        var note = repository.findById(noteId).orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));
        boolean hasShortage = note.getLines().stream().anyMatch(l -> l.getStatus() == LineStatus.SHORTAGE);
        boolean allReady = note.getLines().stream().allMatch(l -> l.getStatus() == LineStatus.READY);

        var finalStatus = hasShortage ? NoteStatus.DELAYED : (allReady ? NoteStatus.COMPLETED : NoteStatus.IN_PROGRESS);
        var completedAt = (finalStatus == NoteStatus.COMPLETED || finalStatus == NoteStatus.DELAYED)
                ? OffsetDateTime.now(ZoneOffset.UTC).toString() : null;

        var updated = ShippingNote.builder()
                .noteId(note.getNoteId())
                .customerName(note.getCustomerName())
                .itemKindsNumber(note.getItemKindsNumber())
                .totalQty(note.getTotalQty())
                .status(finalStatus)
                .completedAt(completedAt)
                .lines(note.getLines())
                .build();
        repository.save(updated);

        return new ShippingCompleteResponse(completedAt, note.getTotalQty());
    }

    private ShippingNoteSummaryResponse toSummary(ShippingNote note) {
        return new ShippingNoteSummaryResponse(
                note.getNoteId(),
                note.getCustomerName(),
                note.getItemKindsNumber(),
                note.getTotalQty(),
                note.getStatus().name(),
                note.getCompletedAt()
        );
    }

    private ShippingNoteDetailResponse toDetail(ShippingNote note) {
        return new ShippingNoteDetailResponse(
                note.getNoteId(),
                note.getCustomerName(),
                note.getItemKindsNumber(),
                note.getTotalQty(),
                note.getStatus().name(),
                note.getCompletedAt(),
                note.getLines().stream().map(l -> new ShippingNoteLineResponse(
                        l.getLineId(),
                        new ShippingProductResponse(l.getProductId(), l.getProductLot(), l.getProductSerial(), l.getProductName(), l.getProductImgUrl()),
                        l.getOrderedQty(),
                        l.getAllocatedQty(),
                        l.getPickedQty(),
                        l.getStatus().name()
                )).toList()
        );
    }
}
