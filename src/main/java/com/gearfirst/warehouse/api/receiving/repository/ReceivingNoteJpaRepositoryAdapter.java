package com.gearfirst.warehouse.api.receiving.repository;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.persistence.ReceivingNoteJpaRepository;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
import com.gearfirst.warehouse.common.util.DateTimes;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReceivingNoteJpaRepositoryAdapter implements ReceivingNoteRepository {

    private static final List<ReceivingNoteStatus> DONE_STATUSES = List.of(ReceivingNoteStatus.COMPLETED_OK,
            ReceivingNoteStatus.COMPLETED_ISSUE);

    private final ReceivingNoteJpaRepository jpa;

    @Override
    public List<ReceivingNoteEntity> findNotDone(String date) {
        var list = jpa.findAllByStatusNotIn(DONE_STATUSES);
        if (date != null && !date.isBlank()) {
            try {
                var target = LocalDate.parse(date);
                var bounds = DateTimes.kstDayBounds(target);
                var from = bounds.fromInclusive();
                var to = bounds.toInclusive();
                list = list.stream()
                        .filter(e -> e.getRequestedAt() != null
                                && (!e.getRequestedAt().isBefore(from) && !e.getRequestedAt().isAfter(to)))
                        .toList();
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    @Override
    public List<ReceivingNoteEntity> findDone(String date) {
        var list = jpa.findAllByStatusIn(DONE_STATUSES);
        if (date != null && !date.isBlank()) {
            try {
                var target = LocalDate.parse(date);
                var bounds = DateTimes.kstDayBounds(target);
                var from = bounds.fromInclusive();
                var to = bounds.toInclusive();
                list = list.stream()
                        .filter(e -> e.getRequestedAt() != null
                                && (!e.getRequestedAt().isBefore(from) && !e.getRequestedAt().isAfter(to)))
                        .toList();
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    @Override
    public List<ReceivingNoteEntity> findNotDone(String date, String dateFrom, String dateTo, String warehouseCode) {
        var list = jpa.findAllByStatusNotIn(DONE_STATUSES);
        LocalDate from = null;
        LocalDate to = null;
        try {
            from = (dateFrom == null || dateFrom.isBlank()) ? null : LocalDate.parse(dateFrom);
            to = (dateTo == null || dateTo.isBlank()) ? null : LocalDate.parse(dateTo);
            if (from == null && to == null && date != null && !date.isBlank()) {
                var d = LocalDate.parse(date);
                from = d; to = d;
            }
        } catch (Exception ignored) { }
        if (from != null || to != null) {
            var bounds = DateTimes.kstRangeBounds(from, to);
            var fromUtc = bounds.fromInclusive();
            var toUtc = bounds.toInclusive();
            list = list.stream()
                    .filter(e -> e.getRequestedAt() != null
                            && (!e.getRequestedAt().isBefore(fromUtc) && !e.getRequestedAt().isAfter(toUtc)))
                    .toList();
        }
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            final String wc = warehouseCode;
            list = list.stream().filter(e -> java.util.Objects.equals(wc, e.getWarehouseCode())).toList();
        }
        return list;
    }

    @Override
    public List<ReceivingNoteEntity> findDone(String date, String dateFrom, String dateTo, String warehouseCode) {
        var list = jpa.findAllByStatusIn(DONE_STATUSES);
        LocalDate from = null;
        LocalDate to = null;
        try {
            from = (dateFrom == null || dateFrom.isBlank()) ? null : LocalDate.parse(dateFrom);
            to = (dateTo == null || dateTo.isBlank()) ? null : LocalDate.parse(dateTo);
            if (from == null && to == null && date != null && !date.isBlank()) {
                var d = LocalDate.parse(date);
                from = d; to = d;
            }
        } catch (Exception ignored) { }
        if (from != null || to != null) {
            var bounds = DateTimes.kstRangeBounds(from, to);
            var fromUtc = bounds.fromInclusive();
            var toUtc = bounds.toInclusive();
            list = list.stream()
                    .filter(e -> e.getRequestedAt() != null
                            && (!e.getRequestedAt().isBefore(fromUtc) && !e.getRequestedAt().isAfter(toUtc)))
                    .toList();
        }
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            final String wc = warehouseCode;
            list = list.stream().filter(e -> java.util.Objects.equals(wc, e.getWarehouseCode())).toList();
        }
        return list;
    }

    @Override
    public Optional<ReceivingNoteEntity> findById(Long noteId) {
        return jpa.findById(noteId);
    }

    @Override
    public ReceivingNoteEntity save(ReceivingNoteEntity entity) {
        return jpa.save(entity);
    }
}
