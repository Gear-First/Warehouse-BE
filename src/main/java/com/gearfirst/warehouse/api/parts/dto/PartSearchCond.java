package com.gearfirst.warehouse.api.parts.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartSearchCond {
    private final String q; // unified keyword: code | name

    private final Long partId;

    private final Long categoryId;
    private final String categoryName;

    private final Long carModelId;
    private final String carModelName;

    private final Boolean enabled;
}
