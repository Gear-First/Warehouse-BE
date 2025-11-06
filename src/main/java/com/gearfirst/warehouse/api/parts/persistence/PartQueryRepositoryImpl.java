package com.gearfirst.warehouse.api.parts.persistence;

import static com.gearfirst.warehouse.api.parts.persistence.entity.QPartEntity.partEntity;

import com.gearfirst.warehouse.api.parts.dto.PartIntegratedItem;
import com.gearfirst.warehouse.api.parts.dto.PartSearchCond;
import com.gearfirst.warehouse.api.parts.persistence.entity.QCarModelEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.QPartCarModelEntity;
import com.gearfirst.warehouse.api.parts.persistence.entity.QPartCategoryEntity;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PartQueryRepositoryImpl implements PartQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PartIntegratedItem> search(PartSearchCond cond, Pageable pageable) {
        QPartCategoryEntity c = QPartCategoryEntity.partCategoryEntity;
        // Main content query: join category for name only (1:1)
        var contentQuery = queryFactory
                .select(
                        partEntity.id,
                        partEntity.code,
                        partEntity.name,
                        partEntity.price,
                        partEntity.imageUrl,
                        partEntity.safetyStockQty,
                        partEntity.enabled,
                        partEntity.categoryId,
                        c.name
                )
                .from(partEntity)
                .leftJoin(c).on(c.id.eq(partEntity.categoryId))
                .where(buildWhereForContent(cond, c))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // Sorting with whitelist
        List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(pageable.getSort());
        if (orderSpecifiers.isEmpty()) {
            // Baseline fallback: code ASC, id DESC
            orderSpecifiers = List.of(
                    new OrderSpecifier<>(Order.ASC, partEntity.code),
                    new OrderSpecifier<>(Order.DESC, partEntity.id)
            );
        }
        for (OrderSpecifier<?> os : orderSpecifiers) {
            contentQuery.orderBy(os);
        }

        List<Tuple> tuples = contentQuery.fetch();
        List<Long> partIds = tuples.stream().map(t -> t.get(partEntity.id)).filter(Objects::nonNull).toList();

        // Secondary query to aggregate car model names for the current page
        Map<Long, List<String>> carModelNamesByPartId = partIds.isEmpty() ? Collections.emptyMap() : loadCarModelNames(partIds);

        List<PartIntegratedItem> content = tuples.stream().map(t -> {
            Long id = t.get(partEntity.id);
            return PartIntegratedItem.builder()
                    .id(id)
                    .code(t.get(partEntity.code))
                    .name(t.get(partEntity.name))
                    .price(t.get(partEntity.price))
                    .imageUrl(t.get(partEntity.imageUrl))
                    .safetyStockQty(t.get(partEntity.safetyStockQty))
                    .enabled(Boolean.TRUE.equals(t.get(partEntity.enabled)))
                    .categoryId(t.get(partEntity.categoryId))
                    .categoryName(t.get(c.name))
                    .carModelNames(carModelNamesByPartId.getOrDefault(id, List.of()))
                    .build();
        }).toList();

        // Count query (light; no joins)
        Long totalL = queryFactory
                .select(partEntity.id.count())
                .from(partEntity)
                .where(buildWhereForCount(cond))
                .fetchOne();
        long total = (totalL == null) ? 0L : totalL;

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression[] buildWhereForContent(PartSearchCond cond, QPartCategoryEntity c) {
        List<BooleanExpression> predicates = new ArrayList<>();
        if (cond == null) return new BooleanExpression[0];

        if (cond.getQ() != null && !cond.getQ().isBlank()) {
            String q = cond.getQ().trim();
            predicates.add(partEntity.code.containsIgnoreCase(q).or(partEntity.name.containsIgnoreCase(q)));
        }
        if (cond.getPartId() != null) {
            predicates.add(partEntity.id.eq(cond.getPartId()));
        }
        if (cond.getCategoryId() != null) {
            predicates.add(partEntity.categoryId.eq(cond.getCategoryId()));
        }
        if (cond.getCategoryName() != null && !cond.getCategoryName().isBlank()) {
            predicates.add(c.name.containsIgnoreCase(cond.getCategoryName().trim()));
        }
        if (cond.getCarModelId() != null) {
            QPartCarModelEntity pcm = QPartCarModelEntity.partCarModelEntity;
            predicates.add(JPAExpressions.selectOne()
                    .from(pcm)
                    .where(pcm.partId.eq(partEntity.id)
                            .and(pcm.carModelId.eq(cond.getCarModelId())))
                    .exists());
        }
        if (cond.getCarModelName() != null && !cond.getCarModelName().isBlank()) {
            QPartCarModelEntity pcm = QPartCarModelEntity.partCarModelEntity;
            QCarModelEntity m = QCarModelEntity.carModelEntity;
            String term = cond.getCarModelName().trim();
            predicates.add(JPAExpressions.selectOne()
                    .from(pcm)
                    .join(m).on(m.id.eq(pcm.carModelId))
                    .where(pcm.partId.eq(partEntity.id)
                            .and(m.name.containsIgnoreCase(term)))
                    .exists());
        }
        if (cond.getEnabled() != null) {
            predicates.add(partEntity.enabled.eq(cond.getEnabled()));
        }
        return predicates.toArray(BooleanExpression[]::new);
    }

    private BooleanExpression[] buildWhereForCount(PartSearchCond cond) {
        List<BooleanExpression> predicates = new ArrayList<>();
        if (cond == null) return new BooleanExpression[0];

        if (cond.getQ() != null && !cond.getQ().isBlank()) {
            String q = cond.getQ().trim();
            predicates.add(partEntity.code.containsIgnoreCase(q).or(partEntity.name.containsIgnoreCase(q)));
        }
        if (cond.getPartId() != null) {
            predicates.add(partEntity.id.eq(cond.getPartId()));
        }
        if (cond.getCategoryId() != null) {
            predicates.add(partEntity.categoryId.eq(cond.getCategoryId()));
        }
        if (cond.getCategoryName() != null && !cond.getCategoryName().isBlank()) {
            QPartCategoryEntity c2 = QPartCategoryEntity.partCategoryEntity;
            String term = cond.getCategoryName().trim();
            predicates.add(JPAExpressions.selectOne()
                    .from(c2)
                    .where(c2.id.eq(partEntity.categoryId)
                            .and(c2.name.containsIgnoreCase(term)))
                    .exists());
        }
        if (cond.getCarModelId() != null) {
            QPartCarModelEntity pcm = QPartCarModelEntity.partCarModelEntity;
            predicates.add(JPAExpressions.selectOne()
                    .from(pcm)
                    .where(pcm.partId.eq(partEntity.id)
                            .and(pcm.carModelId.eq(cond.getCarModelId())))
                    .exists());
        }
        if (cond.getCarModelName() != null && !cond.getCarModelName().isBlank()) {
            QPartCarModelEntity pcm = QPartCarModelEntity.partCarModelEntity;
            QCarModelEntity m = QCarModelEntity.carModelEntity;
            String term = cond.getCarModelName().trim();
            predicates.add(JPAExpressions.selectOne()
                    .from(pcm)
                    .join(m).on(m.id.eq(pcm.carModelId))
                    .where(pcm.partId.eq(partEntity.id)
                            .and(m.name.containsIgnoreCase(term)))
                    .exists());
        }
        if (cond.getEnabled() != null) {
            predicates.add(partEntity.enabled.eq(cond.getEnabled()));
        }
        return predicates.toArray(BooleanExpression[]::new);
    }

    private List<OrderSpecifier<?>> buildOrderSpecifiers(Sort sort) {
        if (sort == null || sort.isEmpty()) return List.of();
        Map<String, Function<Sort.Order, OrderSpecifier<?>>> mapping = new HashMap<>();
        mapping.put("code", o -> new OrderSpecifier<>(toOrder(o), partEntity.code));
        mapping.put("name", o -> new OrderSpecifier<>(toOrder(o), partEntity.name));
        mapping.put("price", o -> new OrderSpecifier<>(toOrder(o), partEntity.price));
        mapping.put("createdAt", o -> new OrderSpecifier<>(toOrder(o), partEntity.createdAt));
        mapping.put("updatedAt", o -> new OrderSpecifier<>(toOrder(o), partEntity.updatedAt));

        List<OrderSpecifier<?>> result = new ArrayList<>();
        for (Sort.Order o : sort) {
            var key = o.getProperty();
            var fn = mapping.get(key);
            if (fn != null) {
                result.add(fn.apply(o));
            }
        }
        return result;
    }

    private Order toOrder(Sort.Order o) {
        return o.isAscending() ? Order.ASC : Order.DESC;
    }

    private Map<Long, List<String>> loadCarModelNames(List<Long> partIds) {
        QPartCarModelEntity pcm = QPartCarModelEntity.partCarModelEntity;
        QCarModelEntity m = QCarModelEntity.carModelEntity;

        List<Tuple> rows = queryFactory
                .select(pcm.partId, m.name)
                .from(pcm)
                .join(m).on(m.id.eq(pcm.carModelId))
                .where(pcm.partId.in(partIds))
                .fetch();

        Map<Long, List<String>> map = new HashMap<>();
        for (Tuple row : rows) {
            Long partId = row.get(pcm.partId);
            String modelName = row.get(m.name);
            if (partId == null || modelName == null) continue;
            map.computeIfAbsent(partId, k -> new ArrayList<>()).add(modelName);
        }
        // Optionally sort/unique the names
        for (Map.Entry<Long, List<String>> e : map.entrySet()) {
            List<String> uniq = e.getValue().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
            e.setValue(uniq);
        }
        return map;
    }
}
