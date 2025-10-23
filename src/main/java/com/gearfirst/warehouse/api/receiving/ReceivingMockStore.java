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
                        new ReceivingNoteLineResponse(1L, new ReceivingProductResponse(1L, "LOT-P-1001", "P-1001", "볼트", "/img/p1001"), 50, 48, 2, "DONE_ISSUE"),
                        new ReceivingNoteLineResponse(2L, new ReceivingProductResponse(2L, "LOT-P-1001", "P-1002", "너트", "/img/p1002"), 40, 40, 0, "DONE_OK"),
                        new ReceivingNoteLineResponse(3L, new ReceivingProductResponse(3L, "LOT-P-1001", "P-1003", "와셔", "/img/p1003"), 30, 0, 0, "PENDING")
                )
        ));
        // noteId=102 (PENDING)
        NOTES.put(102L, new ReceivingNoteDetailResponse(
                102L, "BCD Parts", 2, 45, "PENDING", null,
                List.of(
                        new ReceivingNoteLineResponse(10L, new ReceivingProductResponse(4L, "LOT-P-2001", "P-2001", "스페이서", "/img/p2001"), 20, 0, 0, "PENDING"),
                        new ReceivingNoteLineResponse(11L, new ReceivingProductResponse(5L, "LOT-P-2002", "P-2002", "클립", "/img/p2002"), 25, 0, 0, "PENDING")
                )
        ));
        // noteId=201 (Done_OK)  PENDING | IN_PROGRESS | DONE_OK | DONE_ISSUE
        NOTES.put(201L, new ReceivingNoteDetailResponse(
                201L, "ABC Supply", 1, 10, "DONE_OK", "2025-10-16-12:00:00",
                List.of(
                        new ReceivingNoteLineResponse(20L, new ReceivingProductResponse(6L, "LOT-P-3001","P-3001", "가스켓", "/img/p3001"), 10, 10, 0, "DONE_OK")
                )
        ));
        NOTES.put(202L, new ReceivingNoteDetailResponse(
                202L, "ABC Supply", 1, 10, "DONE_ISSUE", "2025-10-16-15:00:00",
                List.of(
                        new ReceivingNoteLineResponse(21L, new ReceivingProductResponse(7L, "LOT-P-3002","P-3002", "와이어A", "/img/p3002"), 10, 8, 2, "DONE_ISSUE")
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
        ArrayList<ReceivingNoteLineResponse> updatedLines = new ArrayList<>();
        for (var l : n.lines()) {
            if (Objects.equals(l.lineId(), lineId)) {
                updatedLines.add(new ReceivingNoteLineResponse(lineId, l.product(), l.orderedQty(), inspected, issue, status));
            } else updatedLines.add(l);
        }
        // note 상태 갱신: PENDING -> IN_PROGRESS
        var newStatus = n.status();
        if (newStatus.equals("PENDING")) {
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
