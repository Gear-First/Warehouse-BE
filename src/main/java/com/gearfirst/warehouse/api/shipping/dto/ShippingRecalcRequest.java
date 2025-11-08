package com.gearfirst.warehouse.api.shipping.dto;

import java.util.List;

public record ShippingRecalcRequest(
        List<Long> lineIds
) {}
