package com.gearfirst.warehouse.api.parts.persistence;

import com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem;
import com.gearfirst.warehouse.api.parts.dto.PartSearchCond;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PartQueryRepositoryImpl implements PartQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PartIntegratedItem> search(PartSearchCond cond, Pageable pageable) {
        // PR1 skeleton: return empty page until implementation in PR2
        List<PartIntegratedItem> content = List.of();
        long total = 0L;
        return new PageImpl<>(content, pageable, total);
    }
}
