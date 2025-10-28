package com.gearfirst.warehouse.api.shipping.service;

import static com.gearfirst.warehouse.common.response.ErrorStatus.CONFLICT_NOTE_STATUS_WHILE_COMPLETE_OR_DELAYED;

import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNoteLine;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteLineResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingProductResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.repository.ShippingNoteRepository;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingNoteRepository repository;
    private final OnHandProvider onHandProvider;

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
    public ShippingNoteDetailResponse updateLine(Long noteId, Long lineId, ShippingUpdateLineRequest request) {
        var note = repository.findById(noteId).orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));

        // DELAYED/COMPLETED 상태에서는 수정 차단 (409)
        if (note.getStatus() == NoteStatus.DELAYED || note.getStatus() == NoteStatus.COMPLETED) {
            throw new ConflictException(CONFLICT_NOTE_STATUS_WHILE_COMPLETE_OR_DELAYED);
        }

        // 대상 라인 조회
        ShippingNoteLine target = note.getLines().stream()
                .filter(l -> l.getLineId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Shipping line not found: " + lineId));

        // 유효성: 0 ≤ picked ≤ allocated ≤ ordered
        if (request.pickedQty() > request.allocatedQty()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_PICKED_QTY_EXCEEDS_ALLOCATED_QTY);
        }
        if (request.allocatedQty() > target.getOrderedQty()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_ALLOCATED_QTY_EXCEEDS_ORDERED_QTY);
        }
        if (request.pickedQty() > target.getOrderedQty()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_PICKED_QTY_EXCEEDS_ORDERED_QTY);
        }

        List<ShippingNoteLine> newLines = new ArrayList<>();
        for (var l : note.getLines()) {
            if (l.getLineId().equals(lineId)) {
                // 서버 도출(온핸드 반영):
                // SHORTAGE if onHand < allocated
                // READY if allocated > 0 and picked == allocated and onHand >= allocated
                // otherwise PENDING
                int onHand = onHandProvider.getOnHandQty(l.getProductId());
                LineStatus derivedStatus;
                if (request.allocatedQty() > onHand) {
                    derivedStatus = LineStatus.SHORTAGE;
                } else if (request.allocatedQty() > 0 && request.pickedQty().equals(request.allocatedQty())) {
                    derivedStatus = LineStatus.READY;
                } else {
                    derivedStatus = LineStatus.PENDING;
                }

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
                        .status(derivedStatus)
                        .build());
            } else {
                newLines.add(l);
            }
        }

        // 상태 계산: 라인 중 SHORTAGE가 하나라도 있으면 즉시 DELAYED로 전이하고 completedAt 기록
        var hasShortage = newLines.stream().anyMatch(l -> l.getStatus() == LineStatus.SHORTAGE);
        var newStatus = note.getStatus();
        String completedAt = note.getCompletedAt();
        if (hasShortage) {
            newStatus = NoteStatus.DELAYED;
            completedAt = OffsetDateTime.now(ZoneOffset.UTC).toString();
        } else if (newStatus == NoteStatus.PENDING) {
            newStatus = NoteStatus.IN_PROGRESS;
        }

        var updated = ShippingNote.builder()
                .noteId(note.getNoteId())
                .customerName(note.getCustomerName())
                .itemKindsNumber(note.getItemKindsNumber())
                .totalQty(note.getTotalQty())
                .status(newStatus)
                .completedAt(completedAt)
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

        if (!hasShortage && !allReady) {
            // READY만 아닌 상태가 섞여 있으면 완료 불가 (409)
            throw new ConflictException(ErrorStatus.CONFLICT_CANNOT_COMPLETE_WHEN_NOT_READY);
        }

        var finalStatus = hasShortage ? NoteStatus.DELAYED : NoteStatus.COMPLETED;
        var completedAt = OffsetDateTime.now(ZoneOffset.UTC).toString();

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
