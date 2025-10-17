package com.gearfirst.warehouse.api.receiving;

import com.gearfirst.warehouse.api.receiving.dto.*;
import com.gearfirst.warehouse.common.exception.NotFoundException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ReceivingMockStore {
    public static final Map<Long, ReceivingNoteDetailResponse> NOTES = new ConcurrentHashMap<>();

    static {
        // noteId=101 (IN_PROGRESS)
        NOTES.put(101L, new ReceivingNoteDetailResponse(
                101L, "ABC Supply", 3, 120, "IN_PROGRESS", null,
                List.of(
                        new ReceivingNoteLineDto(1L, new ReceivingProductDto(1L, "LOT-P-1001", "P-1001", "볼트", "/img/p1001"), 50, 48, 2, "DONE_ISSUE"),
                        new ReceivingNoteLineDto(2L, new ReceivingProductDto(2L, "LOT-P-1001", "P-1002", "너트", "/img/p1002"), 40, 40, 0, "DONE_OK"),
                        new ReceivingNoteLineDto(3L, new ReceivingProductDto(3L, "LOT-P-1001", "P-1003", "와셔", "/img/p1003"), 30, 0, 0, "NOT_STARTED")
                )
        ));
        // noteId=102 (NOT_STARTED)
        NOTES.put(102L, new ReceivingNoteDetailResponse(
                102L, "BCD Parts", 2, 45, "NOT_STARTED", null,
                List.of(
                        new ReceivingNoteLineDto(10L, new ReceivingProductDto(4L, "LOT-P-2001", "P-2001", "스페이서", "/img/p2001"), 20, 0, 0, "NOT_STARTED"),
                        new ReceivingNoteLineDto(11L, new ReceivingProductDto(5L, "LOT-P-2002", "P-2002", "클립", "/img/p2002"), 25, 0, 0, "NOT_STARTED")
                )
        ));
        // noteId=201 (Done_OK)  NOT_STARTED | IN_PROGRESS | DONE_OK | DONE_ISSUE
        NOTES.put(201L, new ReceivingNoteDetailResponse(
                201L, "ABC Supply", 1, 10, "DONE_OK", "2025-10-16-12:00:00",
                List.of(
                        new ReceivingNoteLineDto(20L, new ReceivingProductDto(6L, "LOT-P-3001","P-3001", "가스켓", "/img/p3001"), 10, 10, 0, "DONE_OK")
                )
        ));
        NOTES.put(202L, new ReceivingNoteDetailResponse(
                202L, "ABC Supply", 1, 10, "DONE_ISSUE", "2025-10-16-15:00:00",
                List.of(
                        new ReceivingNoteLineDto(21L, new ReceivingProductDto(7L, "LOT-P-3002","P-3002", "와이어A", "/img/p3002"), 10, 8, 2, "DONE_ISSUE")
                )
        ));
    }

    private ReceivingMockStore() {
    }

    public static List<ReceivingNoteSummaryResponse> findNotDone(String date) {
        // TODO: date 필터링 로직 추가
        // DONE_* 상태가 아닌 것들만 반환
        return NOTES.values().stream()
                .filter(n -> !n.status().startsWith("DONE"))
                .map(ReceivingMockStore::toSummary)
                .sorted(Comparator.comparing(ReceivingNoteSummaryResponse::noteId))
                .toList();
    }

    public static List<ReceivingNoteSummaryResponse> findDone(String date) {
        // DONE_* 상태인 것들만 반환
        return NOTES.values().stream()
                .filter(n -> n.status().startsWith("DONE"))
                .map(n -> new ReceivingNoteSummaryResponse(n.noteId(), n.supplierName(), n.itemKindsNumber(), n.totalQty(), n.status(),
                        // 데모용: 완료건은 지금 시각으로 채움
                        OffsetDateTime.now(ZoneOffset.UTC).toString()))
                .sorted(Comparator.comparing(ReceivingNoteSummaryResponse::noteId))
                .toList();
    }

    public static ReceivingNoteDetailResponse getNoteDetail(Long noteId) {
        var n = NOTES.get(noteId);
        if (n == null) throw new NotFoundException("Receiving note not found: " + noteId);
        return n;
    }

    public static ReceivingNoteDetailResponse updateNoteLine(Long noteId, Long lineId, int inspected, int issue, String status) {
        var n = getNoteDetail(noteId);
        ArrayList<ReceivingNoteLineDto> updatedLines = new ArrayList<>();
        for (var l : n.lines()) {
            if (Objects.equals(l.lineId(), lineId)) {
                updatedLines.add(new ReceivingNoteLineDto(lineId, l.product(), l.orderedQty(), inspected, issue, status));
            } else updatedLines.add(l);
        }
        // note 상태 갱신: NOT_STARTED, PENDING -> IN_PROGRESS
        var newStatus = n.status();
        if (newStatus.equals("NOT_STARTED") || newStatus.equals("PENDING")) {
            newStatus = "IN_PROGRESS";
        }
        var updated = new ReceivingNoteDetailResponse(n.noteId(), n.supplierName(), n.itemKindsNumber(), n.totalQty(), newStatus, n.completedAt(), updatedLines);
        NOTES.put(noteId, updated);
        return updated;
    }

    public static ReceivingCompleteResponse complete(Long noteId) {
        var n = getNoteDetail(noteId);
        // 모든 라인이 DONE_* 이어야 함 (mock: 간단 체크)
        // 상태를 DONE_OK 또는 DONE_ISSUE로 갱신
        // 하나라도 이슈가 있으면 DONE_ISSUE
        boolean hasIssue = n.lines().stream().anyMatch(l -> l.status().equals("DONE_ISSUE"));
        var finalStatus = hasIssue ? "DONE_ISSUE" : "DONE_OK";
        var completedAt = OffsetDateTime.now(ZoneOffset.UTC).toString();
        var updated = new ReceivingNoteDetailResponse(n.noteId(), n.supplierName(), n.itemKindsNumber(), n.totalQty(), finalStatus, completedAt, n.lines());
        NOTES.put(noteId, updated);
        return new ReceivingCompleteResponse(completedAt, n.totalQty());
    }

    private static ReceivingNoteSummaryResponse toSummary(ReceivingNoteDetailResponse n) {
        return new ReceivingNoteSummaryResponse(n.noteId(), n.supplierName(), n.itemKindsNumber(), n.totalQty(), n.status(), null);
    }
}
