package com.gearfirst.warehouse.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageEnvelope<T> {
    private final List<T> items;
    private final int page;
    private final int size;
    private final long total;

    public static <T> PageEnvelope<T> of(List<T> items, int page, int size, long total) {
        return new PageEnvelope<>(items, page, size, total);
    }
}
