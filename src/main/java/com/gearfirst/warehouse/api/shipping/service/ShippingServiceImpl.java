package com.gearfirst.warehouse.api.shipping.service;

import static com.gearfirst.warehouse.common.response.ErrorStatus.CONFLICT_NOTE_STATUS_WHILE_COMPLETE_OR_DELAYED;

import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
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
import com.gearfirst.warehouse.common.sequence.NoteNumberGenerator;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShippingServiceImpl implements ShippingService {

    private final ShippingNoteRepository repository;
    private final OnHandProvider onHandProvider;
    private final InventoryService inventoryService;
    private final NoteNumberGenerator noteNumberGenerator;

    @Autowired
    public ShippingServiceImpl(ShippingNoteRepository repository,
                               OnHandProvider onHandProvider,
                               InventoryService inventoryService,
                               NoteNumberGenerator noteNumberGenerator) {
        this.repository = repository;
        this.onHandProvider = onHandProvider;
        this.inventoryService = inventoryService;
        this.noteNumberGenerator = noteNumberGenerator;
    }

    // Backward-compatible ctors for tests
    public ShippingServiceImpl(ShippingNoteRepository repository, OnHandProvider onHandProvider) {
        this.repository = repository;
        this.onHandProvider = onHandProvider;
        this.inventoryService = new NoOpInventoryService();
        this.noteNumberGenerator = null;
    }

    public ShippingServiceImpl(ShippingNoteRepository repository,
                               OnHandProvider onHandProvider,
                               InventoryService inventoryService) {
        this.repository = repository;
        this.onHandProvider = onHandProvider;
        this.inventoryService = inventoryService;
        this.noteNumberGenerator = null;
    }

    @Override
    public List<ShippingNoteSummaryResponse> getNotDone(String date) {
        return repository.findNotDone(date).stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSummary)
                .toList();
    }

    // Overload with optional warehouse filter (warehouseCode)
    public List<ShippingNoteSummaryResponse> getNotDone(String date, String warehouseCode) {
        var notes = repository.findNotDone(date);
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            notes = notes.stream().filter(n -> java.util.Objects.equals(warehouseCode, n.getWarehouseCode())).toList();
        }
        return notes.stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<ShippingNoteSummaryResponse> getDone(String date) {
        return repository.findDone(date).stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSummary)
                .toList();
    }

    // Overload with optional warehouse filter (warehouseCode)
    public List<ShippingNoteSummaryResponse> getDone(String date, String warehouseCode) {
        var notes = repository.findDone(date);
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            notes = notes.stream().filter(n -> java.util.Objects.equals(warehouseCode, n.getWarehouseCode())).toList();
        }
        return notes.stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public ShippingNoteDetailResponse getDetail(Long noteId) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));
        return toDetail(note);
    }

    @Override
    public ShippingNoteDetailResponse updateLine(Long noteId, Long lineId, ShippingUpdateLineRequest request) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));

        // DELAYED/COMPLETED 상태에서는 수정 차단 (409)
        if (note.getStatus() == NoteStatus.DELAYED || note.getStatus() == NoteStatus.COMPLETED) {
            throw new ConflictException(CONFLICT_NOTE_STATUS_WHILE_COMPLETE_OR_DELAYED);
        }

        // 대상 라인 조회
        ShippingNoteLine target = note.getLines().stream()
                .filter(l -> l.getLineId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Shipping line not found: " + lineId));

        // 유효성: 0 ≤ picked ≤ ordered
        if (request.pickedQty() > target.getOrderedQty()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_PICKED_QTY_EXCEEDS_ORDERED_QTY);
        }

        List<ShippingNoteLine> newLines = new ArrayList<>();
        for (var l : note.getLines()) {
            if (l.getLineId().equals(lineId)) {
                // 서버 도출(온핸드 반영) – allocation 제거 모델:
                // remainingNeeded = orderedQty - pickedQty
                // SHORTAGE if remainingNeeded > onHand
                // READY if pickedQty == orderedQty (onHand >= 0 is implied)
                // otherwise PENDING
                int onHand = onHandProvider.getOnHandQty(l.getProductId());
                int remainingNeeded = Math.max(0, l.getOrderedQty() - request.pickedQty());
                LineStatus derivedStatus;
                if (remainingNeeded > onHand) {
                    derivedStatus = LineStatus.SHORTAGE;
                } else if (request.pickedQty().equals(l.getOrderedQty())) {
                    derivedStatus = LineStatus.READY;
                } else {
                    derivedStatus = LineStatus.PENDING;
                }

                newLines.add(ShippingNoteLine.builder()
                        .lineId(l.getLineId())
                        .productId(l.getProductId())
                        .productLot(l.getProductLot())
                        .productCode(l.getProductCode())
                        .productName(l.getProductName())
                        .productImgUrl(l.getProductImgUrl())
                        .orderedQty(l.getOrderedQty())
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
                .branchName(note.getBranchName())
                .itemKindsNumber(note.getItemKindsNumber())
                .totalQty(note.getTotalQty())
                .warehouseCode(note.getWarehouseCode())
                .shippingNo(note.getShippingNo())
                .requestedAt(note.getRequestedAt())
                .expectedShipDate(note.getExpectedShipDate())
                .shippedAt(note.getShippedAt())
                .assigneeName(note.getAssigneeName())
                .assigneeDept(note.getAssigneeDept())
                .assigneePhone(note.getAssigneePhone())
                .remark(note.getRemark())
                .status(newStatus)
                .completedAt(completedAt)
                .lines(newLines)
                .build();
        repository.save(updated);
        return toDetail(updated);
    }

    @Override
    public ShippingCompleteResponse complete(Long noteId) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));
        // Require handler info before completion processing
        if (note.getAssigneeName() == null || note.getAssigneeName().isBlank()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_HANDLER_INFO_REQUIRED);
        }
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
                    inventoryService.decrease(note.getWarehouseCode() == null ? null : note.getWarehouseCode(),
                            l.getProductId(), shipped);
                }
            }
        }

        var updated = ShippingNote.builder()
                .noteId(note.getNoteId())
                .branchName(note.getBranchName())
                .itemKindsNumber(note.getItemKindsNumber())
                .totalQty(note.getTotalQty())
                .status(finalStatus)
                .completedAt(completedAt)
                .lines(note.getLines())
                .build();
        repository.save(updated);

        return new ShippingCompleteResponse(completedAt,
                finalStatus == NoteStatus.COMPLETED ? totalShipped : note.getTotalQty());
    }

    @Override
    public ShippingNoteDetailResponse create(ShippingCreateNoteRequest request) {
        // Generate simple ids (temporary). In real system, use sequence/UUID.
        List<ShippingNoteLine> lines = new ArrayList<>();
        int totalQty = 0;
        Set<Long> productIds = new HashSet<>();
        if (request != null && request.lines() != null) {
            int i = 0;
            for (var rl : request.lines()) {
                int ordered = rl.orderedQty() == null ? 0 : rl.orderedQty();
                totalQty += ordered;
                if (rl.productId() != null) {
                    productIds.add(rl.productId());
                }
                lines.add(ShippingNoteLine.builder()
                        .lineId(null)
                        .productId(rl.productId())
                        .productLot(null)
                        .productCode(null)
                        .productName(null)
                        .productImgUrl(null)
                        .orderedQty(ordered)
                        .pickedQty(0)
                        .status(LineStatus.PENDING)
                        .build());
            }
        }
        int itemKinds = productIds.size();
        // required fields: requestedAt, shippingNo
        String requestedAt = (request == null ? null : request.requestedAt());
        if (requestedAt == null || requestedAt.isBlank()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_REQUESTED_AT_INVALID);
        }
        String expectedShipDate = (request == null ? null : request.expectedShipDate());
        if (expectedShipDate == null || expectedShipDate.isBlank()) {
            expectedShipDate = OffsetDateTime.parse(requestedAt).plusDays(2).toString();
        }
        String shippingNo = (request == null ? null : request.shippingNo());
        if (shippingNo == null || shippingNo.isBlank()) {
            if (this.noteNumberGenerator != null) {
                // Auto-generate OUT number using warehouseCode + requestedAt
                var reqAtOd = OffsetDateTime.parse(requestedAt);
                shippingNo = noteNumberGenerator.generateShippingNo(
                        request == null ? null : request.warehouseCode(), reqAtOd);
            } else {
                // In tests constructed without generator, keep strict validation
                throw new BadRequestException(ErrorStatus.SHIPPING_NO_INVALID);
            }
        }

        var note = ShippingNote.builder()
                .noteId(null) // let JPA generate note id
                .branchName(request == null ? null : request.branchName())
                .itemKindsNumber(itemKinds)
                .totalQty(totalQty)
                .warehouseCode(request == null ? null : request.warehouseCode())
                .shippingNo(shippingNo)
                .requestedAt(requestedAt)
                .expectedShipDate(expectedShipDate)
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
        String status = note.getStatus() == null ? "PENDING" : note.getStatus().name();
        return new ShippingNoteSummaryResponse(
                note.getNoteId(),
                note.getShippingNo(),
                note.getBranchName(),
                note.getItemKindsNumber(),
                note.getTotalQty(),
                status,
                note.getWarehouseCode(),
                note.getRequestedAt(),
                note.getCompletedAt()
        );
    }

    private ShippingNoteDetailResponse toDetail(ShippingNote note) {
        String status = note.getStatus() == null ? "PENDING" : note.getStatus().name();
        return new ShippingNoteDetailResponse(
                note.getNoteId(),
                note.getBranchName(),
                note.getItemKindsNumber(),
                note.getTotalQty(),
                status,
                note.getCompletedAt(),
                note.getShippingNo(),
                note.getWarehouseCode(),
                note.getRequestedAt(),
                note.getExpectedShipDate(),
                note.getShippedAt(),
                note.getAssigneeName(),
                note.getAssigneeDept(),
                note.getAssigneePhone(),
                note.getRemark(),
                note.getLines().stream().map(l -> new ShippingNoteLineResponse(
                        l.getLineId(),
                        new ShippingProductResponse(l.getProductId(), l.getProductLot(), l.getProductCode(),
                                l.getProductName(), l.getProductImgUrl()),
                        l.getOrderedQty(),
                        l.getPickedQty(),
                        (l.getStatus() == null ? "PENDING" : l.getStatus().name())
                )).toList()
        );
    }

    private static final class NoOpInventoryService implements InventoryService {
        @Override
        public PageEnvelope<OnHandSummary> listOnHand(
                String warehouseCode, String partKeyword, String supplierName, Integer minQty, Integer maxQty,
                int page, int size, List<String> sort) {
            return PageEnvelope.of(List.of(), page, size, 0);
        }

        @Override
        public void increase(String warehouseCode, Long partId, int qty) { /* no-op */ }

        @Override
        public void decrease(String warehouseCode, Long partId, int qty) { /* no-op */ }
    }
}
