package com.gearfirst.warehouse.api.receiving.dto;

public record ReceivingProductDto(
        Long id,
        String lot,
        String serial,
        String name,
        String imgUrl
) {

}
