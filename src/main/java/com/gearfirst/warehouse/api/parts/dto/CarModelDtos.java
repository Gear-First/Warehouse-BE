package com.gearfirst.warehouse.api.parts.dto;

public class CarModelDtos {
    public record CarModelSummary(Long id, String name) { }
    public record CarModelListItem(Long id, String name, boolean enabled) { }
}
