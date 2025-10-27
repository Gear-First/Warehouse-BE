package com.gearfirst.warehouse.api.receiving.persistence.entity;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receiving_note")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReceivingNoteEntity extends BaseTimeEntity {

    @Id
    private Long noteId;

    private String supplierName;

    private int itemKindsNumber;

    private int totalQty;

    @Enumerated(EnumType.STRING)
    private ReceivingNoteStatus status; // PENDING | IN_PROGRESS | COMPLETED_OK | COMPLETED_ISSUE

    private OffsetDateTime completedAt;

    private String remark;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReceivingNoteLineEntity> lines = new ArrayList<>();

    public void addLine(ReceivingNoteLineEntity line) {
        lines.add(line);
        line.setNote(this);
    }
}
