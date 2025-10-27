package com.gearfirst.warehouse.api.receiving.persistence.entity;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receiving_note_line")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReceivingNoteLineEntity extends BaseTimeEntity {

    @Id
    private Long lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private ReceivingNoteEntity note;

    // Product snapshot fields (keeping independence from Parts domain for now)
    private Long productId;
    private String productLot;
    private String productCode;
    private String productName;
    private String productImgUrl;

    private int orderedQty;
    private int inspectedQty;
    private int issueQty;

    @Enumerated(EnumType.STRING)
    private ReceivingLineStatus status; // PENDING | ACCEPTED | REJECTED

    private String remark;
}
