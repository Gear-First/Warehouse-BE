package com.gearfirst.warehouse.api.receiving.persistence;

import static com.gearfirst.warehouse.api.receiving.persistence.entity.QReceivingNoteEntity.receivingNoteEntity;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummary;
import com.gearfirst.warehouse.api.receiving.dto.ReceivingSearchCond;
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
public class ReceivingQueryRepositoryImpl implements ReceivingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReceivingNoteSummary> search(ReceivingSearchCond cond, Pageable pageable) {
        var contentQuery = queryFactory
            .select(
                receivingNoteEntity.noteId,
                receivingNoteEntity.receivingNo,
                receivingNoteEntity.warehouseCode,
                receivingNoteEntity.supplierName,
                receivingNoteEntity.requestedAt,
                receivingNoteEntity.expectedReceiveDate,
                receivingNoteEntity.completedAt,
                receivingNoteEntity.status,
                receivingNoteEntity.itemKindsNumber,
                receivingNoteEntity.totalQty
            )
            .from(receivingNoteEntity)
            .where(buildWhere(cond))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        // Sorting with whitelist
        List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(pageable.getSort());
        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers = List.of(
                new OrderSpecifier<>(Order.DESC, receivingNoteEntity.noteId),
                new OrderSpecifier<>(Order.DESC, receivingNoteEntity.requestedAt)
            );
        }
        for (OrderSpecifier<?> os : orderSpecifiers) {
            contentQuery.orderBy(os);
        }

        List<Tuple> tuples = contentQuery.fetch();
        List<ReceivingNoteSummary> content = tuples.stream().map(t -> ReceivingNoteSummary.builder()
            .noteId(t.get(receivingNoteEntity.noteId))
            .receivingNo(t.get(receivingNoteEntity.receivingNo))
            .warehouseCode(t.get(receivingNoteEntity.warehouseCode))
            .supplierName(t.get(receivingNoteEntity.supplierName))
            .requestedAt(DateTimes.toKstString(t.get(receivingNoteEntity.requestedAt)))
            .expectedReceiveDate(DateTimes.toKstString(t.get(receivingNoteEntity.expectedReceiveDate)))
            .completedAt(DateTimes.toKstString(t.get(receivingNoteEntity.completedAt)))
            .status(t.get(receivingNoteEntity.status))
            .itemKindsNumber(safeInt(t.get(receivingNoteEntity.itemKindsNumber)))
            .totalQty(safeInt(t.get(receivingNoteEntity.totalQty)))
            .build()).toList();

        Long totalL = queryFactory
            .select(receivingNoteEntity.noteId.count())
            .from(receivingNoteEntity)
            .where(buildWhere(cond))
            .fetchOne();
        long total = totalL == null ? 0L : totalL;

        return new PageImpl<>(content, pageable, total);
    }

    private int safeInt(Integer v) { return v == null ? 0 : v; }

    private BooleanExpression[] buildWhere(ReceivingSearchCond cond) {
        List<BooleanExpression> list = new ArrayList<>();
        if (cond == null) return new BooleanExpression[0];

        // status handling
        String status = cond.getStatus() == null ? "not-done" : cond.getStatus().toLowerCase(Locale.ROOT).trim();
        switch (status) {
            case "done" -> list.add(receivingNoteEntity.status.in(ReceivingNoteStatus.COMPLETED_OK, ReceivingNoteStatus.COMPLETED_ISSUE));
            case "all" -> { /* no status filter */ }
            default -> list.add(receivingNoteEntity.status.notIn(ReceivingNoteStatus.COMPLETED_OK, ReceivingNoteStatus.COMPLETED_ISSUE));
        }

        // date filters KST local day → UTC bounds
        String date = cond.getDate();
        String dateFrom = cond.getDateFrom();
        String dateTo = cond.getDateTo();
        if (dateFrom != null || dateTo != null) {
            LocalDate from = parseLocalDate(dateFrom);
            LocalDate to = parseLocalDate(dateTo);
            var bounds = DateTimes.kstRangeBounds(from, to);
            if (bounds != null) {
                list.add(receivingNoteEntity.requestedAt.goe(bounds.fromInclusive()));
                list.add(receivingNoteEntity.requestedAt.loe(bounds.toInclusive()));
            }
        } else if (date != null && !date.isBlank()) {
            LocalDate day = parseLocalDate(date);
            var bounds = DateTimes.kstDayBounds(day);
            if (bounds != null) {
                list.add(receivingNoteEntity.requestedAt.goe(bounds.fromInclusive()));
                list.add(receivingNoteEntity.requestedAt.loe(bounds.toInclusive()));
            }
        }

        if (cond.getWarehouseCode() != null && !cond.getWarehouseCode().isBlank()) {
            list.add(receivingNoteEntity.warehouseCode.equalsIgnoreCase(cond.getWarehouseCode().trim()));
        }
        if (cond.getReceivingNo() != null && !cond.getReceivingNo().isBlank()) {
            String term = cond.getReceivingNo().trim();
            list.add(receivingNoteEntity.receivingNo.containsIgnoreCase(term));
        }
        if (cond.getSupplierName() != null && !cond.getSupplierName().isBlank()) {
            String term = cond.getSupplierName().trim();
            list.add(receivingNoteEntity.supplierName.containsIgnoreCase(term));
        }
        return list.toArray(BooleanExpression[]::new);
    }

    private List<OrderSpecifier<?>> buildOrderSpecifiers(Sort sort) {
        if (sort == null || sort.isEmpty()) return List.of();
        Map<String, Function<Sort.Order, OrderSpecifier<?>>> mapping = new HashMap<>();
        mapping.put("requestedAt", o -> new OrderSpecifier<>(toOrder(o), receivingNoteEntity.requestedAt));
        mapping.put("completedAt", o -> new OrderSpecifier<>(toOrder(o), receivingNoteEntity.completedAt));
        mapping.put("receivingNo", o -> new OrderSpecifier<>(toOrder(o), receivingNoteEntity.receivingNo));
        mapping.put("noteId", o -> new OrderSpecifier<>(toOrder(o), receivingNoteEntity.noteId));
        mapping.put("status", o -> new OrderSpecifier<>(toOrder(o), receivingNoteEntity.status));
        mapping.put("supplierName", o -> new OrderSpecifier<>(toOrder(o), receivingNoteEntity.supplierName));
        mapping.put("warehouseCode", o -> new OrderSpecifier<>(toOrder(o), receivingNoteEntity.warehouseCode));

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

    private LocalDate parseLocalDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }
}
