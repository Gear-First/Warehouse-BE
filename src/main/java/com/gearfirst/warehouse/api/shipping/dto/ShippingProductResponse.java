package com.gearfirst.warehouse.api.shipping.dto;

public record ShippingProductResponse(
        Long id,
        String lot,
        String serial,
        String name,
        String imgUrl
) {
}
