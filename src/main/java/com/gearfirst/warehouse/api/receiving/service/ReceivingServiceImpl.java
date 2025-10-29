package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteDetailResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteLineResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingProductResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingUpdateLineRequest;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteLineEntity;
import com.gearfirst.warehouse.api.receiving.repository.ReceivingNoteRepository;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceivingServiceImpl implements ReceivingService {

    private static final List<ReceivingNoteStatus> DONE_STATUSES = List.of(ReceivingNoteStatus.COMPLETED_OK, ReceivingNoteStatus.COMPLETED_ISSUE);

    private final ReceivingNoteRepository repository;
    private final com.gearfirst.warehouse.api.inventory.service.InventoryService inventoryService;

    @Override
    public List<ReceivingNoteSummaryResponse> getNotDone(String date) {
        return repository.findNotDone(date).stream()
                .sorted(Comparator.comparing(ReceivingNoteEntity::getNoteId))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<ReceivingNoteSummaryResponse> getDone(String date) {
        return repository.findDone(date).stream()
                .sorted(Comparator.comparing(ReceivingNoteEntity::getNoteId))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public ReceivingNoteDetailResponse getDetail(Long noteId) {
        var note = repository.findById(noteId).orElseThrow(() -> new NotFoundException("Receiving note not found: " + noteId));
        return toDetail(note);
    }

    @Override
    public ReceivingNoteDetailResponse updateLine(Long noteId, Long lineId, ReceivingUpdateLineRequest request) {
        var note = repository.findById(noteId).orElseThrow(() -> new NotFoundException("Receiving note not found: " + noteId));

        // Block when note already completed
        if (isDoneStatus(note.getStatus())) {
            throw new ConflictException(ErrorStatus.CONFLICT_RECEIVING_NOTE_ALREADY_COMPLETED);
        }

        // Find target line
        var maybe = note.getLines().stream().filter(l -> l.getLineId().equals(lineId)).findFirst();
        if (maybe.isEmpty()) {
            throw new NotFoundException("Receiving line not found: " + lineId);
        }
        ReceivingNoteLineEntity line = maybe.get();

        // Block when line already ACCEPTED/REJECTED
        if (line.getStatus() == ReceivingLineStatus.ACCEPTED || line.getStatus() == ReceivingLineStatus.REJECTED) {
            throw new ConflictException(ErrorStatus.CONFLICT_RECEIVING_LINE_ALREADY_DONE);
        }

        int ordered = line.getOrderedQty();
        int inspected = request.inspectedQty();
        if (inspected < 0 || inspected > ordered) {
            throw new BadRequestException(ErrorStatus.RECEIVING_ORDERED_QTY_EXCEEDS_INSPECTED_QTY);
        }

        int issueQty = Boolean.TRUE.equals(request.hasIssue()) ? Math.max(0, ordered - inspected) : 0;
        ReceivingLineStatus newLineStatus = Boolean.TRUE.equals(request.hasIssue()) ? ReceivingLineStatus.REJECTED : ReceivingLineStatus.ACCEPTED;

        // Apply changes to line
        line.setInspectedQty(inspected);
        line.setIssueQty(issueQty);
        line.setStatus(newLineStatus);

        // First update transitions PENDING -> IN_PROGRESS
        if (note.getStatus() == ReceivingNoteStatus.PENDING) {
            note.setStatus(ReceivingNoteStatus.IN_PROGRESS);
        }

        repository.save(note);
        return toDetail(note);
    }

    @Override
    public ReceivingCompleteResponse complete(Long noteId) {
        var note = repository.findById(noteId).orElseThrow(() -> new NotFoundException("Receiving note not found: " + noteId));

        if (isDoneStatus(note.getStatus())) {
            throw new ConflictException(ErrorStatus.CONFLICT_RECEIVING_NOTE_ALREADY_COMPLETED);
        }
        // All lines must be ACCEPTED/REJECTED
        boolean allFinal = note.getLines().stream()
                .allMatch(l -> l.getStatus() == ReceivingLineStatus.ACCEPTED || l.getStatus() == ReceivingLineStatus.REJECTED);
        if (!allFinal) {
            throw new ConflictException(ErrorStatus.CONFLICT_RECEIVING_CANNOT_COMPLETE_WHEN_NOT_DONE);
        }
        // MVP policy: acceptedQty = orderedQty for ACCEPTED lines, REJECTED contributes 0
        int appliedSum = note.getLines().stream()
                .filter(l -> l.getStatus() == ReceivingLineStatus.ACCEPTED)
                .mapToInt(ReceivingNoteLineEntity::getOrderedQty)
                .sum();

        // Apply inventory increases per product (use warehouseId when available)
        Long whId = note.getWarehouseId();
        note.getLines().stream()
                .filter(l -> l.getStatus() == ReceivingLineStatus.ACCEPTED)
                .forEach(l -> inventoryService.increase(whId, l.getProductId(), l.getOrderedQty()));

        boolean hasRejected = note.getLines().stream().anyMatch(l -> l.getStatus() == ReceivingLineStatus.REJECTED);
        ReceivingNoteStatus finalStatus = hasRejected ? ReceivingNoteStatus.COMPLETED_ISSUE : ReceivingNoteStatus.COMPLETED_OK;
        var completedAt = OffsetDateTime.now(ZoneOffset.UTC);

        note.setStatus(finalStatus);
        note.setCompletedAt(completedAt);
        repository.save(note);

        return new ReceivingCompleteResponse(completedAt.toString(), appliedSum);
    }

    private boolean isDoneStatus(ReceivingNoteStatus status) {
        return DONE_STATUSES.contains(status);
    }

    private ReceivingNoteSummaryResponse toSummary(ReceivingNoteEntity n) {
        String completedAt = n.getCompletedAt() != null ? n.getCompletedAt().toString() : null;
        return new ReceivingNoteSummaryResponse(
                n.getNoteId(),
                n.getSupplierName(),
                n.getItemKindsNumber(),
                n.getTotalQty(),
                n.getStatus().name(),
                completedAt
        );
    }

    private ReceivingNoteDetailResponse toDetail(ReceivingNoteEntity n) {
        String completedAt = n.getCompletedAt() != null ? n.getCompletedAt().toString() : null;
        var lines = n.getLines().stream().map(l -> new ReceivingNoteLineResponse(
                l.getLineId(),
                new ReceivingProductResponse(l.getProductId(), l.getProductLot(), l.getProductCode(), l.getProductName(), l.getProductImgUrl()),
                l.getOrderedQty(),
                l.getInspectedQty(),
                l.getIssueQty(),
                l.getStatus().name()
        )).toList();
        return new ReceivingNoteDetailResponse(
                n.getNoteId(),
                n.getSupplierName(),
                n.getItemKindsNumber(),
                n.getTotalQty(),
                n.getStatus().name(),
                completedAt,
                lines
        );
    }
}
