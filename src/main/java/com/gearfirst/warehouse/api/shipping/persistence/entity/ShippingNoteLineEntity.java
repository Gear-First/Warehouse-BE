package com.gearfirst.warehouse.api.shipping.persistence.entity;

import com.gearfirst.warehouse.api.shipping.domain.LineStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shipping_note_line")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ShippingNoteLineEntity {

    @Id
    private Long lineId;

    private Long productId;
    private String productLot;
    private String productSerial;
    private String productName;
    private String productImgUrl;

    private int orderedQty;
    private int allocatedQty;
    private int pickedQty;

    @Enumerated(EnumType.STRING)
    private LineStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private ShippingNoteEntity note;
}
