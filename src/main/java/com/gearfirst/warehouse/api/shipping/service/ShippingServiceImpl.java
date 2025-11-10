package com.gearfirst.warehouse.api.shipping.service;

import static com.gearfirst.warehouse.common.response.ErrorStatus.CONFLICT_NOTE_STATUS_WHILE_COMPLETE;

import com.gearfirst.warehouse.api.dto.NotificationDto;
import com.gearfirst.warehouse.api.inventory.dto.OnHandDtos.OnHandSummary;
import com.gearfirst.warehouse.api.inventory.service.InventoryService;
import com.gearfirst.warehouse.api.parts.persistence.PartJpaRepository;
import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import com.gearfirst.warehouse.api.shipping.domain.ShippingNoteLine;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteRequest;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCompleteResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingCreateNoteRequest;
import com.gearfirst.warehouse.api.shipping.dto.ShippingLineConfirmResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteDetailV2Response;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteLineResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummary;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummaryResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingProductResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingRecalcResponse;
import com.gearfirst.warehouse.api.shipping.dto.ShippingSearchCond;
import com.gearfirst.warehouse.api.shipping.dto.ShippingUpdateLineRequest;
import com.gearfirst.warehouse.api.shipping.persistence.ShippingQueryRepository;
import com.gearfirst.warehouse.api.shipping.repository.ShippingNoteRepository;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.exception.ConflictException;
import com.gearfirst.warehouse.common.exception.NotFoundException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import com.gearfirst.warehouse.common.response.PageEnvelope;
import com.gearfirst.warehouse.common.sequence.NoteNumberGenerator;
import com.gearfirst.warehouse.common.util.DateTimes;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingNoteRepository repository;
    private final OnHandProvider onHandProvider;
    private final InventoryService inventoryService;
    private final NoteNumberGenerator noteNumberGenerator;
    // Optional helper for product snapshot (nullable for tests)
    private final PartJpaRepository partRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Optional Querydsl repository for unified list queries (nullable for tests)
    @Autowired(required = false)
    private ShippingQueryRepository shippingQueryRepository;

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

    @Override
    public List<ShippingNoteSummaryResponse> getNotDone(String date, String dateFrom, String dateTo, String warehouseCode) {
        // Prefer Querydsl repository when available (range-first policy is handled at controller)
        if (shippingQueryRepository != null) {
            ShippingSearchCond cond = ShippingSearchCond.builder()
                    .status("not-done")
                    .date(date)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .warehouseCode(warehouseCode)
                    .shippingNo(null)
                    .branchName(null)
                    .build();
            List<ShippingNoteSummary> list = shippingQueryRepository.searchAll(cond);
            return list.stream().map(s -> toSummaryFromQuery(s)).toList();
        }
        // Fallback to legacy repository implementation
        return repository.findNotDone(date, dateFrom, dateTo, warehouseCode).stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<ShippingNoteSummaryResponse> getDone(String date, String dateFrom, String dateTo, String warehouseCode) {
        if (shippingQueryRepository != null) {
            ShippingSearchCond cond = ShippingSearchCond.builder()
                    .status("done")
                    .date(date)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .warehouseCode(warehouseCode)
                    .shippingNo(null)
                    .branchName(null)
                    .build();
            List<ShippingNoteSummary> list = shippingQueryRepository.searchAll(cond);
            return list.stream().map(this::toSummaryFromQuery).toList();
        }
        return repository.findDone(date, dateFrom, dateTo, warehouseCode).stream()
                .sorted(Comparator.comparing(ShippingNote::getNoteId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSummary)
                .toList();
    }

    // Extended overloads including shippingNo/branchName filters
    @Override
    public List<ShippingNoteSummaryResponse> getNotDone(String date, String dateFrom, String dateTo, String warehouseCode,
                                                        String shippingNo, String branchName) {
        if (shippingQueryRepository != null) {
            ShippingSearchCond cond = ShippingSearchCond.builder()
                    .status("not-done")
                    .date(date)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .warehouseCode(warehouseCode)
                    .shippingNo(shippingNo)
                    .branchName(branchName)
                    .build();
            List<ShippingNoteSummary> list = shippingQueryRepository.searchAll(cond);
            return list.stream().map(this::toSummaryFromQuery).toList();
        }
        // Legacy fallback ignores text filters
        return getNotDone(date, dateFrom, dateTo, warehouseCode);
    }

    @Override
    public List<ShippingNoteSummaryResponse> getDone(String date, String dateFrom, String dateTo, String warehouseCode,
                                                     String shippingNo, String branchName) {
        if (shippingQueryRepository != null) {
            ShippingSearchCond cond = ShippingSearchCond.builder()
                    .status("done")
                    .date(date)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .warehouseCode(warehouseCode)
                    .shippingNo(shippingNo)
                    .branchName(branchName)
                    .build();
            List<ShippingNoteSummary> list = shippingQueryRepository.searchAll(cond);
            return list.stream().map(this::toSummaryFromQuery).toList();
        }
        // Legacy fallback ignores text filters
        return getDone(date, dateFrom, dateTo, warehouseCode);
    }

    // Unified q overloads
    @Override
    public List<ShippingNoteSummaryResponse> getNotDone(String date, String dateFrom, String dateTo, String warehouseCode,
                                                        String shippingNo, String branchName, String q) {
        if (shippingQueryRepository != null) {
            ShippingSearchCond cond = ShippingSearchCond.builder()
                    .status("not-done")
                    .q(q)
                    .date(date)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .warehouseCode(warehouseCode)
                    .shippingNo(shippingNo)
                    .branchName(branchName)
                    .build();
            List<ShippingNoteSummary> list = shippingQueryRepository.searchAll(cond);
            return list.stream().map(this::toSummaryFromQuery).toList();
        }
        return getNotDone(date, dateFrom, dateTo, warehouseCode, shippingNo, branchName);
    }

    @Override
    public List<ShippingNoteSummaryResponse> getDone(String date, String dateFrom, String dateTo, String warehouseCode,
                                                     String shippingNo, String branchName, String q) {
        if (shippingQueryRepository != null) {
            ShippingSearchCond cond = ShippingSearchCond.builder()
                    .status("done")
                    .q(q)
                    .date(date)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .warehouseCode(warehouseCode)
                    .shippingNo(shippingNo)
                    .branchName(branchName)
                    .build();
            List<ShippingNoteSummary> list = shippingQueryRepository.searchAll(cond);
            return list.stream().map(this::toSummaryFromQuery).toList();
        }
        return getDone(date, dateFrom, dateTo, warehouseCode, shippingNo, branchName);
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
            throw new ConflictException(CONFLICT_NOTE_STATUS_WHILE_COMPLETE);
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
                int onHand = onHandProvider.getOnHandQty(note.getWarehouseCode(), l.getProductId());
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
                                .onHandQty(onHand)
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
            completedAt = DateTimes.toKstString(OffsetDateTime.now(ZoneOffset.UTC));
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
                .orderId(note.getOrderId())
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
    @Transactional
    public ShippingCompleteResponse complete(Long noteId, ShippingCompleteRequest req) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));

        // Idempotency: block if already in a terminal state
        // 기존 : COMPLETED || DELAYED 차단
//        if (note.getStatus() == NoteStatus.COMPLETED || note.getStatus() == NoteStatus.DELAYED) {
        // 현재 : COMPLETED 재시도만 방지, 그외 이후에 ready 및 재고 관련 검증 추가됨
        if (note.getStatus() == NoteStatus.COMPLETED) {
            throw new ConflictException(CONFLICT_NOTE_STATUS_WHILE_COMPLETE);
        }

        // Resolve handler info from request if provided
        String assigneeName = (req != null && req.assigneeName() != null && !req.assigneeName().isBlank())
                ? req.assigneeName() : note.getAssigneeName();
        String assigneeDept = (req != null && req.assigneeDept() != null && !req.assigneeDept().isBlank())
                ? req.assigneeDept() : note.getAssigneeDept();
        String assigneePhone = (req != null && req.assigneePhone() != null && !req.assigneePhone().isBlank())
                ? req.assigneePhone() : note.getAssigneePhone();
        // Backup: accept optional orderId during complete (temporary)
        Long incomingOrderId = (req == null ? null : req.orderId());
        Long orderIdToPersist = (note.getOrderId() != null) ? note.getOrderId() : incomingOrderId;
        // Require handler info before completion processing
        if (assigneeName == null || assigneeName.isBlank()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_HANDLER_INFO_REQUIRED);
        }

        // V2 policy: completion only when ALL lines are READY (no SHORTAGE/PENDING allowed)
        boolean allReady = note.getLines().stream().allMatch(l -> l.getStatus() == LineStatus.READY);
        if (!allReady) {
            throw new ConflictException(ErrorStatus.CONFLICT_CANNOT_COMPLETE_WHEN_NOT_READY);
        }

        // Re-validate inventory just-in-time to protect against races
        for (var l : note.getLines()) {
            int onHand = onHandProvider.getOnHandQty(note.getWarehouseCode(), l.getProductId());
            if (onHand < l.getOrderedQty()) {
                // Inventory changed between confirm and complete
                throw new ConflictException(ErrorStatus.CONFLICT_INVENTORY_INSUFFICIENT);
            }
        }

        // Apply inventory decreases: full-ship orderedQty for each READY line
        int totalShipped = 0;
        for (var l : note.getLines()) {
            int shipped = l.getOrderedQty();
            totalShipped += shipped;
            inventoryService.decrease(note.getWarehouseCode() == null ? null : note.getWarehouseCode(),
                    l.getProductId(), shipped);
        }

        var completedAt = DateTimes.toKstString(OffsetDateTime.now(ZoneOffset.UTC));
        var updated = ShippingNote.builder()
                .noteId(note.getNoteId())
                .branchName(note.getBranchName())
                .itemKindsNumber(note.getItemKindsNumber())
                .totalQty(note.getTotalQty())
                .warehouseCode(note.getWarehouseCode())
                .shippingNo(note.getShippingNo())
                .orderId(orderIdToPersist)
                .requestedAt(note.getRequestedAt())
                .expectedShipDate(note.getExpectedShipDate())
                .shippedAt(note.getShippedAt())
                .assigneeName(assigneeName)
                .assigneeDept(assigneeDept)
                .assigneePhone(assigneePhone)
                .remark(note.getRemark())
                .status(NoteStatus.COMPLETED)
                .completedAt(completedAt)
                .lines(note.getLines())
                .build();
        repository.save(updated);

        String topic = "notification";
        NotificationDto n = NotificationDto.builder()
                .id(1L)
                .eventId(UUID.randomUUID().toString())
                .type("부품 출고 완료")
                .message("부품 출고 요청이 완료되었습니다.")
                .receiver("본사")
                .build();

        kafkaTemplate.send(topic, n);

        return new ShippingCompleteResponse(completedAt, totalShipped);
    }

    @Override
    @Transactional
    public ShippingNoteDetailResponse create(ShippingCreateNoteRequest request) {
        // Generate simple ids (temporary). In real system, use sequence/UUID.
        List<ShippingNoteLine> lines = new ArrayList<>();
        int totalQty = 0;
        Set<Long> productIds = new HashSet<>();
        if (request != null && request.lines() != null) {
            int i = 0;
            // Pre-validate: all productIds must exist
            HashSet<Long> idsToValidate = new java.util.HashSet<>();
            for (var rl : request.lines()) {
                if (rl.productId() != null) idsToValidate.add(rl.productId());
            }
            if (!idsToValidate.isEmpty() && partRepository != null) {
                var found = partRepository.findAllById(idsToValidate).stream().map(p -> p.getId()).collect(java.util.stream.Collectors.toSet());
                for (Long pid : idsToValidate) {
                    if (!found.contains(pid)) {
                        throw new BadRequestException(ErrorStatus.PART_CODE_INVALID);
                    }
                }
            }
            for (var rl : request.lines()) {
                int ordered = rl.orderedQty() == null ? 0 : rl.orderedQty();
                totalQty += ordered;
                if (rl.productId() != null) {
                    productIds.add(rl.productId());
                }
                // Snapshot product info via Part if available
                String productCode = null;
                String productName = null;
                String productImgUrl = null;
                if (rl.productId() != null && partRepository != null) {
                    var partOpt = partRepository.findById(rl.productId());
                    if (partOpt.isPresent()) {
                        var part = partOpt.get();
                        productCode = part.getCode();
                        productName = part.getName();
                        productImgUrl = part.getImageUrl();
                    } else {
                        productCode = "P-" + rl.productId();
                    }
                } else if (rl.productId() != null) {
                    productCode = "P-" + rl.productId();
                }
                // LOT generation: use supplierName from Part (first 3 chars) when available; fallback to empty prefix
                String lot = null;
                String reqAtStr = request == null ? null : request.requestedAt();
                if (reqAtStr != null && !reqAtStr.isBlank()) {
                    var reqAt = OffsetDateTime.parse(reqAtStr);
                    String supplier3 = "";
                    if (rl.productId() != null && partRepository != null) {
                        var partOpt2 = partRepository.findById(rl.productId());
                        if (partOpt2.isPresent() && partOpt2.get().getSupplierName() != null) {
                            String sn = partOpt2.get().getSupplierName();
                            supplier3 = sn.substring(0, Math.min(3, sn.length()));
                        }
                    }
                    String ymd = reqAt.minusDays(3).toLocalDate().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
                    lot = supplier3 + "-" + ymd + "-" + (productCode == null ? "" : productCode);
                }
                lines.add(ShippingNoteLine.builder()
                        .lineId(null)
                        .productId(rl.productId())
                        .productLot(lot)
                        .productCode(productCode)
                        .productName(productName)
                        .productImgUrl(productImgUrl)
                        .orderedQty(ordered)
                        .pickedQty(0)
                        .status(LineStatus.PENDING)
                        .build());
            }
        }
        int itemKinds = productIds.size();
        // required fields: requestedAt
        String requestedAt = (request == null ? null : request.requestedAt());
        if (requestedAt == null || requestedAt.isBlank()) {
            throw new BadRequestException(ErrorStatus.SHIPPING_REQUESTED_AT_INVALID);
        }
        String expectedShipDate = (request == null ? null : request.expectedShipDate());
        if (expectedShipDate == null || expectedShipDate.isBlank()) {
            expectedShipDate = OffsetDateTime.parse(requestedAt).plusDays(2).toString();
        }
        // Always generate shippingNo on server side
        String shippingNo;
        if (this.noteNumberGenerator != null) {
            var reqAtOd = OffsetDateTime.parse(requestedAt);
            shippingNo = noteNumberGenerator.generateShippingNo(
                    request == null ? null : request.warehouseCode(), reqAtOd);
        } else {
            // Fallback simple generator for test environments without NoteNumberGenerator
            var reqAtOd = OffsetDateTime.parse(requestedAt);
            String ymd = reqAtOd.toLocalDate().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            String wh = (request == null || request.warehouseCode() == null || request.warehouseCode().isBlank()) ? "DEFAULT" : request.warehouseCode();
            shippingNo = "OUT-" + wh + "-" + ymd + "-001";
        }

        var note = ShippingNote.builder()
                .noteId(null) // let JPA generate note id
                .branchName(request == null ? null : request.branchName())
                .itemKindsNumber(itemKinds)
                .totalQty(totalQty)
                .warehouseCode(request == null ? null : request.warehouseCode())
                .shippingNo(shippingNo)
                .orderId(request == null ? null : request.orderId())
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

        String topic = "notification";
        NotificationDto n = NotificationDto.builder()
                .id(1L)
                .eventId(UUID.randomUUID().toString())
                .type("출고 요청 등록")
                .message("출고 요청이 등록되었습니다.")
                .receiver("본사")
                .build();

        kafkaTemplate.send(topic, n);

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
                note.getExpectedShipDate(),
                note.getCompletedAt()
        );
    }

    private ShippingNoteSummaryResponse toSummaryFromQuery(ShippingNoteSummary s) {
        String status = s.getStatus() == null ? "PENDING" : s.getStatus().name();
        return new ShippingNoteSummaryResponse(
                s.getNoteId(),
                s.getShippingNo(),
                s.getBranchName(),
                s.getItemKindsNumber(),
                s.getTotalQty(),
                status,
                s.getWarehouseCode(),
                s.getRequestedAt(),
                s.getExpectedShipDate(),
                s.getCompletedAt()
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
                note.getOrderId(),
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
        public PageEnvelope<OnHandSummary> listOnHandAdvanced(
                String q, Long partId, String partCode, String partName, String warehouseCode,
                String supplierName, Integer minQty, Integer maxQty, int page, int size, List<String> sort) {
            return PageEnvelope.of(List.of(), page, size, 0);
        }

        @Override
        public void increase(String warehouseCode, Long partId, int qty) { /* no-op */ }

        @Override
        public void increase(String warehouseCode, Long partId, int qty, String supplierName) { /* no-op */ }

        @Override
        public void decrease(String warehouseCode, Long partId, int qty) { /* no-op */ }
    }
    @Override
    public ShippingNoteDetailV2Response getDetailV2(Long noteId) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));
        String snapshotAt = OffsetDateTime.now(ZoneOffset.UTC).toString();
        var lines = note.getLines().stream().map(l -> {
            int onHand = onHandProvider.getOnHandQty(note.getWarehouseCode(), l.getProductId());
            String suggested = (onHand >= l.getOrderedQty()) ? LineStatus.READY.name() : LineStatus.SHORTAGE.name();
            return new ShippingNoteDetailV2Response.Line(
                    l.getLineId(),
                    l.getProductId(),
                    l.getProductCode(),
                    l.getProductName(),
                    l.getProductLot(),
                    l.getProductImgUrl(),
                    l.getOrderedQty(),
                    l.getPickedQty(),
                    (l.getStatus() == null ? "PENDING" : l.getStatus().name()),
                    onHand,
                    suggested
            );
        }).toList();
        return new ShippingNoteDetailV2Response(
                note.getNoteId(),
                (note.getStatus() == null ? "PENDING" : note.getStatus().name()),
                note.getCompletedAt(),
                null, // delayedAt not persisted yet
                snapshotAt,
                note.getShippingNo(),
                note.getOrderId(),
                note.getBranchName(),
                note.getWarehouseCode(),
                note.getRequestedAt(),
                note.getExpectedShipDate(),
                note.getShippedAt(),
                note.getItemKindsNumber(),
                note.getTotalQty(),
                note.getAssigneeName(),
                note.getAssigneeDept(),
                note.getAssigneePhone(),
                note.getRemark(),
                lines
        );
    }

    @Override
    @Transactional
    public ShippingRecalcResponse checkShippable(Long noteId, boolean apply, List<Long> lineIds) {

        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));
        String snapshotAt = OffsetDateTime.now(ZoneOffset.UTC).toString();
        // Guard: COMPLETED notes cannot be mutated; allow dry-run only
        if (note.getStatus() == NoteStatus.COMPLETED && apply) {
            throw new ConflictException(CONFLICT_NOTE_STATUS_WHILE_COMPLETE);
        }
        var targetLines = (lineIds == null || lineIds.isEmpty()) ? note.getLines() : note.getLines().stream()
                .filter(l -> lineIds.contains(l.getLineId())).toList();

        int readyCount = 0;
        int pendingCount = 0;
        boolean hasShortage = false;
        List<ShippingRecalcResponse.Line> outLines = new java.util.ArrayList<>();
        List<ShippingNoteLine> newLines = new java.util.ArrayList<>();
        for (var l : note.getLines()) {
            boolean inScope = targetLines.stream().anyMatch(t -> t.getLineId().equals(l.getLineId()));
            int onHand = onHandProvider.getOnHandQty(note.getWarehouseCode(), l.getProductId());
            String suggested = (onHand >= l.getOrderedQty()) ? LineStatus.READY.name() : LineStatus.SHORTAGE.name();
            if (inScope) {
                if (LineStatus.READY.name().equals(suggested)) readyCount++; else hasShortage = true;
                outLines.add(new ShippingRecalcResponse.Line(
                        l.getLineId(), l.getOrderedQty(), (l.getStatus() == null ? "PENDING" : l.getStatus().name()), onHand, suggested
                ));
                if (apply) {
                    var updatedLine = ShippingNoteLine.builder()
                            .lineId(l.getLineId())
                            .productId(l.getProductId())
                            .productLot(l.getProductLot())
                            .productCode(l.getProductCode())
                            .productName(l.getProductName())
                            .productImgUrl(l.getProductImgUrl())
                            .orderedQty(l.getOrderedQty())
                            .onHandQty(onHand)
                            .pickedQty(l.getPickedQty())
                            .status(LineStatus.valueOf(suggested))
                            .build();
                    newLines.add(updatedLine);
                } else {
                    newLines.add(l);
                }
            } else {
                // not in scope
                pendingCount += (l.getStatus() == LineStatus.PENDING) ? 1 : 0;
                newLines.add(l);
            }
        }
        // Suggested note status reflects what would happen after apply:
        String suggestedNote;
        if (note.getStatus() == NoteStatus.COMPLETED) {
            suggestedNote = NoteStatus.COMPLETED.name();
        } else if (hasShortage) {
            suggestedNote = NoteStatus.DELAYED.name();
        } else {
            // recovery path: DELAYED or PENDING -> IN_PROGRESS
            NoteStatus current = note.getStatus();
            if (current == null || current == NoteStatus.PENDING || current == NoteStatus.DELAYED) {
                suggestedNote = NoteStatus.IN_PROGRESS.name();
            } else {
                suggestedNote = current.name();
            }
        }
        var resp = new ShippingRecalcResponse(
                note.getNoteId(),
                (note.getStatus() == null ? "PENDING" : note.getStatus().name()),
                suggestedNote,
                hasShortage,
                readyCount,
                pendingCount,
                snapshotAt,
                apply,
                outLines
        );
        if (apply) {
            NoteStatus current = note.getStatus();
            NoteStatus newStatus;
            if (hasShortage) {
                newStatus = NoteStatus.DELAYED;
            } else if (current == null || current == NoteStatus.PENDING || current == NoteStatus.DELAYED) {
                // recovery to IN_PROGRESS when shortages resolved
                newStatus = NoteStatus.IN_PROGRESS;
            } else {
                newStatus = current;
            }
            var updated = ShippingNote.builder()
                    .noteId(note.getNoteId())
                    .branchName(note.getBranchName())
                    .itemKindsNumber(note.getItemKindsNumber())
                    .totalQty(note.getTotalQty())
                    .warehouseCode(note.getWarehouseCode())
                    .shippingNo(note.getShippingNo())
                    .orderId(note.getOrderId())
                    .requestedAt(note.getRequestedAt())
                    .expectedShipDate(note.getExpectedShipDate())
                    .shippedAt(note.getShippedAt())
                    .assigneeName(note.getAssigneeName())
                    .assigneeDept(note.getAssigneeDept())
                    .assigneePhone(note.getAssigneePhone())
                    .remark(note.getRemark())
                    .status(newStatus)
                    .completedAt(note.getCompletedAt())
                    .lines(newLines)
                    .build();
            repository.save(updated);
        }
        return resp;
    }

    @Override
    @Transactional
    public ShippingLineConfirmResponse confirmLine(Long noteId, Long lineId) {
        var note = repository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Shipping note not found: " + noteId));
        var line = note.getLines().stream().filter(l -> l.getLineId().equals(lineId))
                .findFirst().orElseThrow(() -> new NotFoundException("Shipping line not found: " + lineId));
        int onHand = onHandProvider.getOnHandQty(note.getWarehouseCode(), line.getProductId());
        String suggested = (onHand >= line.getOrderedQty()) ? LineStatus.READY.name() : LineStatus.SHORTAGE.name();
        LineStatus newLineStatus = LineStatus.valueOf(suggested);
        String prev = (line.getStatus() == null ? "PENDING" : line.getStatus().name());

        List<ShippingNoteLine> newLines = new java.util.ArrayList<>();
        for (var l : note.getLines()) {
            if (l.getLineId().equals(lineId)) {
                newLines.add(ShippingNoteLine.builder()
                        .lineId(l.getLineId())
                        .productId(l.getProductId())
                        .productLot(l.getProductLot())
                        .productCode(l.getProductCode())
                        .productName(l.getProductName())
                        .productImgUrl(l.getProductImgUrl())
                        .orderedQty(l.getOrderedQty())
                        .onHandQty(onHand)
                        .pickedQty(l.getPickedQty())
                        .status(newLineStatus)
                        .build());
            } else {
                newLines.add(l);
            }
        }
        NoteStatus newNoteStatus = note.getStatus();
        if (newLineStatus == LineStatus.SHORTAGE) {
            newNoteStatus = NoteStatus.DELAYED;
        } else if (newNoteStatus == NoteStatus.PENDING) {
            newNoteStatus = NoteStatus.IN_PROGRESS;
        }
        var updated = ShippingNote.builder()
                .noteId(note.getNoteId())
                .branchName(note.getBranchName())
                .itemKindsNumber(note.getItemKindsNumber())
                .totalQty(note.getTotalQty())
                .warehouseCode(note.getWarehouseCode())
                .shippingNo(note.getShippingNo())
                .orderId(note.getOrderId())
                .requestedAt(note.getRequestedAt())
                .expectedShipDate(note.getExpectedShipDate())
                .shippedAt(note.getShippedAt())
                .assigneeName(note.getAssigneeName())
                .assigneeDept(note.getAssigneeDept())
                .assigneePhone(note.getAssigneePhone())
                .remark(note.getRemark())
                .status(newNoteStatus)
                .completedAt(note.getCompletedAt())
                .lines(newLines)
                .build();
        repository.save(updated);
        String snapshotAt = OffsetDateTime.now(ZoneOffset.UTC).toString();
        return new ShippingLineConfirmResponse(
                note.getNoteId(), lineId, line.getOrderedQty(), onHand, prev, suggested, newLineStatus.name(), newNoteStatus.name(), snapshotAt
        );
    }
}
