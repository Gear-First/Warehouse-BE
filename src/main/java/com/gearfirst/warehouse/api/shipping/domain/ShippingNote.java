package com.gearfirst.warehouse.api.shipping.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ShippingNote {
    private Long noteId;
    private String customerName;
    private int itemKindsNumber;
    private int totalQty;
    private Long warehouseId; // nullable for MVP
    private NoteStatus status; // PENDING | IN_PROGRESS | DELAYED | COMPLETED
    private String completedAt; // ISO8601 or null
    private List<ShippingNoteLine> lines;
}
