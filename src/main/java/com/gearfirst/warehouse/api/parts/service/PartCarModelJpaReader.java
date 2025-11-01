package com.gearfirst.warehouse.api.parts.service;

import com.gearfirst.warehouse.api.parts.persistence.PartCarModelJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class PartCarModelJpaReader implements PartCarModelReader {

    private final PartCarModelJpaRepository repo;

    @Override
    public long countByPartId(Long partId) {
        if (partId == null) {
            return 0L;
        }
        return repo.countByPartIdAndEnabledTrue(partId);
    }
}
