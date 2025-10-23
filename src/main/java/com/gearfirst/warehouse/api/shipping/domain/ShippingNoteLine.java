package com.gearfirst.warehouse.api.shipping.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ShippingNoteLine {
    private Long lineId;
    private Long productId;
    private String productLot;
    private String productSerial;
    private String productName;
    private String productImgUrl;

    private int orderedQty;
    private int allocatedQty;
    private int pickedQty;

    private LineStatus status; // PENDING | READY | SHORTAGE
}
