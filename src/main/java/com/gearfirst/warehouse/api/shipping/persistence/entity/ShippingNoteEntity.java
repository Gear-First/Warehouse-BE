package com.gearfirst.warehouse.api.shipping.persistence.entity;

import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String branchName;

    private int itemKindsNumber;

    private int totalQty;

    // Nullable for MVP; multi-warehouse adoption
    private String warehouseCode;

    // Additive metadata
    private String shippingNo;
    private OffsetDateTime requestedAt;
    private OffsetDateTime expectedShipDate;
    private OffsetDateTime shippedAt;
    private String assigneeName;
    private String assigneeDept;
    private String assigneePhone;
    private String remark;

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
