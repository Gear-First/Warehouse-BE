package com.gearfirst.warehouse.api.shipping.dto;

public record ShippingProductResponse(
        Long id,
        String lot,
        String code,
        String name,
        String imgUrl
) {
}
