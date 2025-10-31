package com.gearfirst.warehouse.api.receiving.persistence.entity;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingLineStatus;
import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "receiving_note_line")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReceivingNoteLineEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    private ReceivingLineStatus status; // PENDING | ACCEPTED | REJECTED

    private String remark;
}
