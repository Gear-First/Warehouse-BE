package com.gearfirst.warehouse.api.shipping.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ShippingNote {
    private Long noteId;
    private String customerName;
    private int itemKindsNumber;
    private int totalQty;
    private NoteStatus status; // PENDING | IN_PROGRESS | DELAYED | COMPLETED
    private String completedAt; // ISO8601 or null
    private List<ShippingNoteLine> lines;
}
