package com.gearfirst.warehouse.api.shipping.persistence;

import static com.gearfirst.warehouse.api.shipping.persistence.entity.QShippingNoteEntity.shippingNoteEntity;

import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.dto.ShippingNoteSummary;
import com.gearfirst.warehouse.api.shipping.dto.ShippingSearchCond;
import com.gearfirst.warehouse.common.util.DateTimes;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShippingQueryRepositoryImpl implements ShippingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ShippingNoteSummary> search(ShippingSearchCond cond, Pageable pageable) {
        var contentQuery = queryFactory
            .select(
                shippingNoteEntity.noteId,
                shippingNoteEntity.shippingNo,
                shippingNoteEntity.warehouseCode,
                shippingNoteEntity.branchName,
                shippingNoteEntity.requestedAt,
                shippingNoteEntity.expectedShipDate,
                shippingNoteEntity.completedAt,
                shippingNoteEntity.status,
                shippingNoteEntity.itemKindsNumber,
                shippingNoteEntity.totalQty
            )
            .from(shippingNoteEntity)
            .where(buildWhere(cond))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        // Sorting with whitelist
        List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(pageable.getSort());
        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers = List.of(
                new OrderSpecifier<>(Order.DESC, shippingNoteEntity.noteId),
                new OrderSpecifier<>(Order.DESC, shippingNoteEntity.requestedAt)
            );
        }
        for (OrderSpecifier<?> os : orderSpecifiers) {
            contentQuery.orderBy(os);
        }

        List<Tuple> tuples = contentQuery.fetch();
        List<ShippingNoteSummary> content = tuples.stream().map(t -> ShippingNoteSummary.builder()
            .noteId(t.get(shippingNoteEntity.noteId))
            .shippingNo(t.get(shippingNoteEntity.shippingNo))
            .warehouseCode(t.get(shippingNoteEntity.warehouseCode))
            .branchName(t.get(shippingNoteEntity.branchName))
            .requestedAt(DateTimes.toKstString(t.get(shippingNoteEntity.requestedAt)))
            .expectedShipDate(DateTimes.toKstString(t.get(shippingNoteEntity.expectedShipDate)))
            .completedAt(DateTimes.toKstString(t.get(shippingNoteEntity.completedAt)))
            .status(t.get(shippingNoteEntity.status))
            .itemKindsNumber(safeInt(t.get(shippingNoteEntity.itemKindsNumber)))
            .totalQty(safeInt(t.get(shippingNoteEntity.totalQty)))
            .build()).toList();

        Long totalL = queryFactory
            .select(shippingNoteEntity.noteId.count())
            .from(shippingNoteEntity)
            .where(buildWhere(cond))
            .fetchOne();
        long total = totalL == null ? 0L : totalL;

        return new PageImpl<>(content, pageable, total);
    }

    private int safeInt(Integer v) { return v == null ? 0 : v; }

    private BooleanExpression[] buildWhere(ShippingSearchCond cond) {
        List<BooleanExpression> list = new ArrayList<>();
        if (cond == null) return new BooleanExpression[0];

        // status handling
        String status = cond.getStatus() == null ? "not-done" : cond.getStatus().toLowerCase(Locale.ROOT).trim();
        switch (status) {
            case "done" -> list.add(shippingNoteEntity.status.in(NoteStatus.COMPLETED, NoteStatus.DELAYED));
            case "all" -> { /* no status filter */ }
            default -> list.add(shippingNoteEntity.status.notIn(NoteStatus.COMPLETED, NoteStatus.DELAYED));
        }

        // date filters KST local day → UTC bounds (requestedAt)
        String date = cond.getDate();
        String dateFrom = cond.getDateFrom();
        String dateTo = cond.getDateTo();
        if (dateFrom != null || dateTo != null) {
            LocalDate from = parseLocalDate(dateFrom);
            LocalDate to = parseLocalDate(dateTo);
            var bounds = DateTimes.kstRangeBounds(from, to);
            if (bounds != null) {
                list.add(shippingNoteEntity.requestedAt.goe(bounds.fromInclusive()));
                list.add(shippingNoteEntity.requestedAt.loe(bounds.toInclusive()));
            }
        } else if (date != null && !date.isBlank()) {
            LocalDate day = parseLocalDate(date);
            var bounds = DateTimes.kstDayBounds(day);
            if (bounds != null) {
                list.add(shippingNoteEntity.requestedAt.goe(bounds.fromInclusive()));
                list.add(shippingNoteEntity.requestedAt.loe(bounds.toInclusive()));
            }
        }

        // unified q (shippingNo | branchName | warehouseCode[when explicit param is blank])
        if (cond.getQ() != null && !cond.getQ().isBlank()) {
            String term = cond.getQ().trim();
            var qPredicate =
                shippingNoteEntity.shippingNo.containsIgnoreCase(term)
                    .or(shippingNoteEntity.branchName.containsIgnoreCase(term));
            if (cond.getWarehouseCode() == null || cond.getWarehouseCode().isBlank()) {
                qPredicate = qPredicate.or(shippingNoteEntity.warehouseCode.containsIgnoreCase(term));
            }
            list.add(qPredicate);
        }

        if (cond.getWarehouseCode() != null && !cond.getWarehouseCode().isBlank()) {
            list.add(shippingNoteEntity.warehouseCode.equalsIgnoreCase(cond.getWarehouseCode().trim()));
        }
        if (cond.getShippingNo() != null && !cond.getShippingNo().isBlank()) {
            String term = cond.getShippingNo().trim();
            list.add(shippingNoteEntity.shippingNo.containsIgnoreCase(term));
        }
        if (cond.getBranchName() != null && !cond.getBranchName().isBlank()) {
            String term = cond.getBranchName().trim();
            list.add(shippingNoteEntity.branchName.containsIgnoreCase(term));
        }
        return list.toArray(BooleanExpression[]::new);
    }

    private List<OrderSpecifier<?>> buildOrderSpecifiers(Sort sort) {
        if (sort == null || sort.isEmpty()) return List.of();
        Map<String, Function<Sort.Order, OrderSpecifier<?>>> mapping = new HashMap<>();
        mapping.put("requestedAt", o -> new OrderSpecifier<>(toOrder(o), shippingNoteEntity.requestedAt));
        mapping.put("expectedShipDate", o -> new OrderSpecifier<>(toOrder(o), shippingNoteEntity.expectedShipDate));
        mapping.put("completedAt", o -> new OrderSpecifier<>(toOrder(o), shippingNoteEntity.completedAt));
        mapping.put("shippingNo", o -> new OrderSpecifier<>(toOrder(o), shippingNoteEntity.shippingNo));
        mapping.put("noteId", o -> new OrderSpecifier<>(toOrder(o), shippingNoteEntity.noteId));
        mapping.put("status", o -> new OrderSpecifier<>(toOrder(o), shippingNoteEntity.status));
        mapping.put("branchName", o -> new OrderSpecifier<>(toOrder(o), shippingNoteEntity.branchName));
        mapping.put("warehouseCode", o -> new OrderSpecifier<>(toOrder(o), shippingNoteEntity.warehouseCode));

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

    private Order toOrder(Sort.Order o) { return o.isAscending() ? Order.ASC : Order.DESC; }

    private LocalDate parseLocalDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    @Override
    public java.util.List<ShippingNoteSummary> searchAll(ShippingSearchCond cond) {
        // Unpaged variant used by legacy service overloads. Apply the same whitelist fallback sort.
        var contentQuery = queryFactory
            .select(
                shippingNoteEntity.noteId,
                shippingNoteEntity.shippingNo,
                shippingNoteEntity.warehouseCode,
                shippingNoteEntity.branchName,
                shippingNoteEntity.requestedAt,
                shippingNoteEntity.expectedShipDate,
                shippingNoteEntity.completedAt,
                shippingNoteEntity.status,
                shippingNoteEntity.itemKindsNumber,
                shippingNoteEntity.totalQty
            )
            .from(shippingNoteEntity)
            .where(buildWhere(cond))
            .limit(Integer.MAX_VALUE);

        // Baseline fallback ordering
        contentQuery.orderBy(
            new OrderSpecifier<>(Order.DESC, shippingNoteEntity.noteId),
            new OrderSpecifier<>(Order.DESC, shippingNoteEntity.requestedAt)
        );

        List<Tuple> tuples = contentQuery.fetch();
        return tuples.stream().map(t -> ShippingNoteSummary.builder()
            .noteId(t.get(shippingNoteEntity.noteId))
            .shippingNo(t.get(shippingNoteEntity.shippingNo))
            .warehouseCode(t.get(shippingNoteEntity.warehouseCode))
            .branchName(t.get(shippingNoteEntity.branchName))
            .requestedAt(DateTimes.toKstString(t.get(shippingNoteEntity.requestedAt)))
            .expectedShipDate(DateTimes.toKstString(t.get(shippingNoteEntity.expectedShipDate)))
            .completedAt(DateTimes.toKstString(t.get(shippingNoteEntity.completedAt)))
            .status(t.get(shippingNoteEntity.status))
            .itemKindsNumber(safeInt(t.get(shippingNoteEntity.itemKindsNumber)))
            .totalQty(safeInt(t.get(shippingNoteEntity.totalQty)))
            .build()).toList();
    }
}
