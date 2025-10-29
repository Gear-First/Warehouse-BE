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
    // Additive metadata (nullable)
    private String shippingNo;
    private String requestedAt;      // ISO8601 or null
    private String expectedShipDate; // ISO8601 or null
    private String shippedAt;        // ISO8601 or null
    private String assigneeName;
    private String assigneeDept;
    private String assigneePhone;
    private String remark;

    private NoteStatus status; // PENDING | IN_PROGRESS | DELAYED | COMPLETED
    private String completedAt; // ISO8601 or null
    private List<ShippingNoteLine> lines;
}
