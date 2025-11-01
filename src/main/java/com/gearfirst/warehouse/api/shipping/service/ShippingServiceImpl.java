package com.gearfirst.warehouse.api.shipping.service;

import static com.gearfirst.warehouse.common.response.ErrorStatus.CONFLICT_NOTE_STATUS_WHILE_COMPLETE_OR_DELAYED;

import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNoteLine;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCreateNoteRequest;
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
import com.gearfirst.warehouse.common.response.PageEnvelope;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class ShippingServiceImpl implements ShippingService {

    private final ShippingNoteRepository repository;
    private final OnHandProvider onHandProvider;
    private final InventoryService inventoryService;

    @org.springframework.beans.factory.annotation.Autowired
    public ShippingServiceImpl(ShippingNoteRepository repository,
                               OnHandProvider onHandProvider,
                               InventoryService inventoryService) {
        this.repository = repository;
        this.onHandProvider = onHandProvider;
        this.inventoryService = inventoryService;
    }

    // Backward-compatible ctor for tests that don't provide InventoryService
    public ShippingServiceImpl(ShippingNoteRepository repository, OnHandProvider onHandProvider) {
        this.repository = repository;
        this.onHandProvider = onHandProvider;
        this.inventoryService = new NoOpInventoryService();
    }

    private static final class NoOpInventoryService implements InventoryService {
        @Override
        public PageEnvelope<com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary> listOnHand(Long warehouseId, String keyword, int page, int size) {
            return PageEnvelope.of(java.util.List.of(), page, size, 0);
        }
        @Override
        public void increase(Long warehouseId, Long partId, int qty) { /* no-op */ }
        @Override
        public void decrease(Long warehouseId, Long partId, int qty) { /* no-op */ }
    }

    @Override
    public List<ShippingNoteSummaryResponse> getNotDone(String date) {
        return repository.findNotDone(date).stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId))
                .map(this::toSummary)
                .toList();
    }

    // Overload with optional warehouse filter
    public List<ShippingNoteSummaryResponse> getNotDone(String date, Long warehouseId) {
        var notes = repository.findNotDone(date);
        if (warehouseId != null) {
            notes = notes.stream().filter(n -> java.util.Objects.equals(warehouseId, n.getWarehouseId())).toList();
        }
        return notes.stream()
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

    // Overload with optional warehouse filter
    public List<ShippingNoteSummaryResponse> getDone(String date, Long warehouseId) {
        var notes = repository.findDone(date);
        if (warehouseId != null) {
            notes = notes.stream().filter(n -> java.util.Objects.equals(warehouseId, n.getWarehouseId())).toList();
        }
        return notes.stream()
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

        // If completing, apply inventory decreases based on shippedQty (=pickedQty) per READY line
        int totalShipped = 0;
        if (finalStatus == NoteStatus.COMPLETED) {
            for (var l : note.getLines()) {
                if (l.getStatus() == LineStatus.READY) {
                    int shipped = l.getPickedQty();
                    totalShipped += shipped;
                    inventoryService.decrease(note.getWarehouseId(), l.getProductId(), shipped);
                }
            }
        }

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

        return new ShippingCompleteResponse(completedAt, finalStatus == NoteStatus.COMPLETED ? totalShipped : note.getTotalQty());
    }

    @Override
    public ShippingNoteDetailResponse create(ShippingCreateNoteRequest request) {
        // Generate simple ids (temporary). In real system, use sequence/UUID.
        long noteId = System.currentTimeMillis();
        List<ShippingNoteLine> lines = new java.util.ArrayList<>();
        int totalQty = 0;
        java.util.Set<Long> productIds = new java.util.HashSet<>();
        if (request != null && request.lines() != null) {
            int i = 0;
            for (var rl : request.lines()) {
                long lineId = noteId + (++i);
                int ordered = rl.orderedQty() == null ? 0 : rl.orderedQty();
                totalQty += ordered;
                if (rl.productId() != null) productIds.add(rl.productId());
                lines.add(ShippingNoteLine.builder()
                        .lineId(lineId)
                        .productId(rl.productId())
                        .productLot(null)
                        .productSerial(null)
                        .productName(null)
                        .productImgUrl(null)
                        .orderedQty(ordered)
                        .allocatedQty(0)
                        .pickedQty(0)
                        .status(LineStatus.PENDING)
                        .build());
            }
        }
        int itemKinds = productIds.size();
        var note = ShippingNote.builder()
                .noteId(noteId)
                .customerName(request == null ? null : request.customerName())
                .itemKindsNumber(itemKinds)
                .totalQty(totalQty)
                .warehouseId(request == null ? null : request.warehouseId())
                .shippingNo(request == null ? null : request.shippingNo())
                .requestedAt(request == null ? null : request.requestedAt())
                .expectedShipDate(request == null ? null : request.expectedShipDate())
                .shippedAt(null)
                .assigneeName(null)
                .assigneeDept(null)
                .assigneePhone(null)
                .remark(request == null ? null : request.remark())
                .status(NoteStatus.PENDING)
                .completedAt(null)
                .lines(lines)
                .build();
        var saved = repository.save(note);
        return toDetail(saved);
    }

    private ShippingNoteSummaryResponse toSummary(ShippingNote note) {
        return new ShippingNoteSummaryResponse(
                note.getNoteId(),
                note.getCustomerName(),
                note.getItemKindsNumber(),
                note.getTotalQty(),
                note.getStatus().name(),
                note.getRequestedAt(),
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
                note.getShippingNo(),
                note.getWarehouseId(),
                note.getRequestedAt(),
                note.getExpectedShipDate(),
                note.getShippedAt(),
                note.getAssigneeName(),
                note.getAssigneeDept(),
                note.getAssigneePhone(),
                note.getRemark(),
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
