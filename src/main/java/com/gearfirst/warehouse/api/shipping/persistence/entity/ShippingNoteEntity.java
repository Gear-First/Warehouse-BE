package com.gearfirst.warehouse.api.shipping.persistence.entity;

import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipping_note")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ShippingNoteEntity extends BaseTimeEntity {

    @Id
    private Long noteId;

    private String customerName;

    private int itemKindsNumber;

    private int totalQty;

    @Enumerated(EnumType.STRING)
    private NoteStatus status;

    private OffsetDateTime completedAt;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShippingNoteLineEntity> lines = new ArrayList<>();

    public void addLine(ShippingNoteLineEntity line) {
        lines.add(line);
        line.setNote(this);
    }
}
