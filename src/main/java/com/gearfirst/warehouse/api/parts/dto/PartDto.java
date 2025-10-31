package com.gearfirst.warehouse.api.parts.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartDto {
    private Long id;
    private String partName;
    private String partCode;
    private String category;
    private String supplierName;
}
