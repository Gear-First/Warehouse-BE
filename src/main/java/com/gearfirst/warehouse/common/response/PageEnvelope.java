package com.gearfirst.warehouse.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public record PageEnvelope<T>(List<T> items, int page, int size, long total) {
    public static <T> PageEnvelope<T> of(List<T> items, int page, int size, long total) {
        return new PageEnvelope<>(items, page, size, total);
    }
}
