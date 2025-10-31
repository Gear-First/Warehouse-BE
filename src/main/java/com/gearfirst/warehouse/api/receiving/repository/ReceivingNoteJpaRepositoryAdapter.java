package com.gearfirst.warehouse.api.receiving.repository;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.persistence.ReceivingNoteJpaRepository;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
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
                var target = java.time.LocalDate.parse(date);
                list = list.stream()
                        .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().toLocalDate().isEqual(target))
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
                var target = java.time.LocalDate.parse(date);
                list = list.stream()
                        .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().toLocalDate().isEqual(target))
                        .toList();
            } catch (Exception ignored) {
            }
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
