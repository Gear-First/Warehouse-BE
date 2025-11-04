package com.gearfirst.warehouse.api.receiving.service;

import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteRequest;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCompleteResponse;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingCreateNoteRequest;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceivingServiceImpl implements ReceivingService {

    private static final List<ReceivingNoteStatus> DONE_STATUSES = List.of(ReceivingNoteStatus.COMPLETED_OK,
            ReceivingNoteStatus.COMPLETED_ISSUE);

    private final ReceivingNoteRepository repository;
    private final com.gearfirst.warehouse.common.sequence.NoteNumberGenerator noteNumberGenerator;
    private final InventoryService inventoryService;

    @Override
    public List<ReceivingNoteSummaryResponse> getNotDone(String date) {
        return repository.findNotDone(date).stream()
                .sorted(Comparator.comparing(ReceivingNoteEntity::getNoteId))
                .map(this::toSummary)
                .toList();
    }

    // Overload with optional warehouse filter (warehouseCode)
    public List<ReceivingNoteSummaryResponse> getNotDone(String date, String warehouseCode) {
        var list = repository.findNotDone(date);
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            list = list.stream().filter(n -> Objects.equals(n.getWarehouseCode(), warehouseCode)).toList();
        }
        return list.stream()
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

    // Overload with optional warehouse filter (warehouseCode)
    public List<ReceivingNoteSummaryResponse> getDone(String date, String warehouseCode) {
        var list = repository.findDone(date);
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            list = list.stream().filter(n -> Objects.equals(n.getWarehouseCode(), warehouseCode)).toList();
        }
        return list.stream()
                .sorted(Comparator.comparing(ReceivingNoteEntity::getNoteId))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<ReceivingNoteSummaryResponse> getNotDone(String date, String dateFrom, String dateTo, String warehouseCode) {
        var list = repository.findNotDone(date, dateFrom, dateTo, warehouseCode);
        return list.stream()
                .sorted(Comparator.comparing(ReceivingNoteEntity::getNoteId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<ReceivingNoteSummaryResponse> getDone(String date, String dateFrom, String dateTo, String warehouseCode) {
        var list = repository.findDone(date, dateFrom, dateTo, warehouseCode);
        return list.stream()
                .sorted(Comparator.comparing(ReceivingNoteEntity::getNoteId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public ReceivingNoteDetailResponse getDetail(Long noteId) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Receiving note not found: " + noteId));
        return toDetail(note);
    }

    @Override
    public ReceivingNoteDetailResponse updateLine(Long noteId, Long lineId, ReceivingUpdateLineRequest request) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Receiving note not found: " + noteId));

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

        boolean rejected = Boolean.TRUE.equals(request.rejected());
        ReceivingLineStatus newLineStatus = rejected ? ReceivingLineStatus.REJECTED : ReceivingLineStatus.ACCEPTED;

        // Apply changes to line
        line.setInspectedQty(inspected);
        line.setStatus(newLineStatus);

        // First update transitions PENDING -> IN_PROGRESS
        if (note.getStatus() == ReceivingNoteStatus.PENDING) {
            note.setStatus(ReceivingNoteStatus.IN_PROGRESS);
        }

        repository.save(note);
        return toDetail(note);
    }


    @Override
    public ReceivingCompleteResponse complete(Long noteId, ReceivingCompleteRequest req) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Receiving note not found: " + noteId));

        // Apply inspector info from request if provided (overrides existing null/blank)
        if (req != null) {
            if (req.inspectorName() != null && !req.inspectorName().isBlank()) note.setInspectorName(req.inspectorName());
            if (req.inspectorDept() != null && !req.inspectorDept().isBlank()) note.setInspectorDept(req.inspectorDept());
            if (req.inspectorPhone() != null && !req.inspectorPhone().isBlank()) note.setInspectorPhone(req.inspectorPhone());
        }
        // Require handler/inspector info before completion
        if (note.getInspectorName() == null || note.getInspectorName().isBlank()) {
            throw new BadRequestException(ErrorStatus.RECEIVING_HANDLER_INFO_REQUIRED);
        }

        if (isDoneStatus(note.getStatus())) {
            throw new ConflictException(ErrorStatus.CONFLICT_RECEIVING_NOTE_ALREADY_COMPLETED);
        }
        // All lines must be ACCEPTED/REJECTED
        boolean allFinal = note.getLines().stream()
                .allMatch(l -> l.getStatus() == ReceivingLineStatus.ACCEPTED
                        || l.getStatus() == ReceivingLineStatus.REJECTED);
        if (!allFinal) {
            throw new ConflictException(ErrorStatus.CONFLICT_RECEIVING_CANNOT_COMPLETE_WHEN_NOT_DONE);
        }
        // MVP policy: acceptedQty = orderedQty for ACCEPTED lines, REJECTED contributes 0
        int appliedSum = note.getLines().stream()
                .filter(l -> l.getStatus() == ReceivingLineStatus.ACCEPTED)
                .mapToInt(ReceivingNoteLineEntity::getOrderedQty)
                .sum();

        // Apply inventory increases per product (use warehouseCode when available)
        String whCode = note.getWarehouseCode();
        note.getLines().stream()
                .filter(l -> l.getStatus() == ReceivingLineStatus.ACCEPTED)
                .forEach(l -> inventoryService.increase(whCode, l.getProductId(), l.getOrderedQty()));

        boolean hasRejected = note.getLines().stream().anyMatch(l -> l.getStatus() == ReceivingLineStatus.REJECTED);
        ReceivingNoteStatus finalStatus =
                hasRejected ? ReceivingNoteStatus.COMPLETED_ISSUE : ReceivingNoteStatus.COMPLETED_OK;
        var completedAt = OffsetDateTime.now(ZoneOffset.UTC);

        note.setStatus(finalStatus);
        note.setCompletedAt(completedAt);
        repository.save(note);

        return new ReceivingCompleteResponse(
                com.gearfirst.warehouse.common.util.DateTimes.toKstString(completedAt),
                appliedSum
        );
    }

    @Override
    public ReceivingNoteDetailResponse create(ReceivingCreateNoteRequest request) {
        var builder = ReceivingNoteEntity.builder()
                .noteId(null) // let DB generate
                .supplierName(request == null ? null : request.supplierName())
                .warehouseCode(request == null ? null : request.warehouseCode())
                .remark(request == null ? null : request.remark())
                .status(ReceivingNoteStatus.PENDING)
                .completedAt(null);
        // parse dates if provided; set defaults: requestedAt must be provided; expected = requestedAt + 2 days if null
        OffsetDateTime reqAt = parseOffsetDateTime(request == null ? null : request.requestedAt());
        if (reqAt == null) {
            throw new BadRequestException(ErrorStatus.RECEIVING_REQUESTED_AT_INVALID);
        }
        OffsetDateTime expAt = parseOffsetDateTime(request == null ? null : request.expectedReceiveDate());
        if (expAt == null) {
            expAt = reqAt.plusDays(2);
        }
        builder.requestedAt(reqAt);
        builder.expectedReceiveDate(expAt);
        builder.receivedAt(null);
        String receivingNo = (request == null ? null : request.receivingNo());
        if (receivingNo == null || receivingNo.isBlank()) {
            // Auto-generate IN number using warehouseCode + requestedAt (UTC)
            if (builder.build().getWarehouseCode() == null || builder.build().getWarehouseCode().isBlank()) {
                throw new BadRequestException(ErrorStatus.RECEIVING_NO_INVALID);
            }
            receivingNo = noteNumberGenerator.generateReceivingNo(builder.build().getWarehouseCode(), reqAt);
        }
        builder.receivingNo(receivingNo);
        // Inspector info is set during inspection process, keep null on create
        builder.inspectorName(null);
        builder.inspectorDept(null);
        builder.inspectorPhone(null);

        int totalQty = 0;
        Set<Long> productIds = new HashSet<>();
        List<ReceivingNoteLineEntity> lineEntities = new ArrayList<>();
        if (request != null && request.lines() != null) {
            for (var rl : request.lines()) {
                int ordered = rl.orderedQty() == null ? 0 : rl.orderedQty();
                totalQty += ordered;
                if (rl.productId() != null) {
                    productIds.add(rl.productId());
                }
                // Determine productCode snapshot and LOT
                String productCode = rl.productId() == null ? null : ("P-" + rl.productId());
                String lot = rl.lotNo();
                if (lot == null || lot.isBlank()) {
                    // Generate LOT: factoryName(=supplierName) - (requestedAt-7d yyyyMMdd) - partCode
                    String factoryName = request == null ? null : request.supplierName();
                    java.time.LocalDate prodDate = reqAt.minusDays(7).toLocalDate();
                    String ymd = prodDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
                    lot = (factoryName == null ? "" : factoryName) + "-" + ymd + "-" + (productCode == null ? "" : productCode);
                }

                var line = ReceivingNoteLineEntity.builder()
                        .lineId(null) // let DB generate
                        .productId(rl.productId())
                        .productLot(lot)
                        .productCode(productCode)
                        .productName(null)
                        .productImgUrl(null)
                        .orderedQty(ordered)
                        .inspectedQty(0)
                        .status(ReceivingLineStatus.PENDING)
                        .remark(rl.lineRemark())
                        .build();
                lineEntities.add(line);
            }
        }
        int kinds = productIds.size();
        builder.itemKindsNumber(kinds);
        builder.totalQty(totalQty);
        var entity = builder.build();
        // link lines
        for (var le : lineEntities) {
            entity.addLine(le);
        }
        var saved = repository.save(entity);
        return toDetail(saved);
    }

    private OffsetDateTime parseOffsetDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isDoneStatus(ReceivingNoteStatus status) {
        return DONE_STATUSES.contains(status);
    }

    private ReceivingNoteSummaryResponse toSummary(ReceivingNoteEntity n) {
        String completedAt = com.gearfirst.warehouse.common.util.DateTimes.toKstString(n.getCompletedAt());
        return new ReceivingNoteSummaryResponse(
                n.getNoteId(),
                n.getReceivingNo(),
                n.getSupplierName(),
                n.getItemKindsNumber(),
                n.getTotalQty(),
                n.getStatus().name(),
                n.getWarehouseCode(),
                com.gearfirst.warehouse.common.util.DateTimes.toKstString(n.getRequestedAt()),
                completedAt
        );
    }

    private ReceivingNoteDetailResponse toDetail(ReceivingNoteEntity n) {
        String completedAt = com.gearfirst.warehouse.common.util.DateTimes.toKstString(n.getCompletedAt());
        var lines = n.getLines().stream().map(l -> new ReceivingNoteLineResponse(
                l.getLineId(),
                new ReceivingProductResponse(l.getProductId(), l.getProductLot(), l.getProductCode(),
                        l.getProductName(), l.getProductImgUrl()),
                l.getOrderedQty(),
                l.getInspectedQty(),
                l.getStatus().name()
        )).toList();
        return new ReceivingNoteDetailResponse(
                n.getNoteId(),
                n.getSupplierName(),
                n.getItemKindsNumber(),
                n.getTotalQty(),
                n.getStatus().name(),
                completedAt,
                n.getReceivingNo(),
                n.getWarehouseCode(),
                com.gearfirst.warehouse.common.util.DateTimes.toKstString(n.getRequestedAt()),
                com.gearfirst.warehouse.common.util.DateTimes.toKstString(n.getExpectedReceiveDate()),
                com.gearfirst.warehouse.common.util.DateTimes.toKstString(n.getReceivedAt()),
                n.getInspectorName(),
                n.getInspectorDept(),
                n.getInspectorPhone(),
                n.getRemark(),
                lines
        );
    }
}
