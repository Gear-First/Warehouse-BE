package com.gearfirst.warehouse.api.receiving.repository;

import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
import java.util.List;
import java.util.Optional;

/**
 * Receiving repository port for JPA-based persistence. This port is introduced to gradually replace the static
 * ReceivingMockStore.
 */
public interface ReceivingNoteRepository {
    List<ReceivingNoteEntity> findNotDone(String date);

    List<ReceivingNoteEntity> findDone(String date);

    // New overloads: centralized filtering (range > single) and optional warehouseCode filter
    List<ReceivingNoteEntity> findNotDone(String date, String dateFrom, String dateTo, String warehouseCode);

    List<ReceivingNoteEntity> findDone(String date, String dateFrom, String dateTo, String warehouseCode);

    Optional<ReceivingNoteEntity> findById(Long noteId);

    ReceivingNoteEntity save(ReceivingNoteEntity entity);
}
