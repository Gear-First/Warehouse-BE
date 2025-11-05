package com.gearfirst.warehouse.api.receiving.dto;

public record ReceivingProductResponse(
        Long id,
        String lot,
        String code,
        String name,
        String imgUrl
) {

}
