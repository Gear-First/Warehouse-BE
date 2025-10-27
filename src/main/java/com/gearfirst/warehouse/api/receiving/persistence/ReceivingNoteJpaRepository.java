package com.gearfirst.warehouse.api.receiving.persistence;

import com.gearfirst.warehouse.api.receiving.domain.ReceivingNoteStatus;
import com.gearfirst.warehouse.api.receiving.persistence.entity.ReceivingNoteEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceivingNoteJpaRepository extends JpaRepository<ReceivingNoteEntity, Long> {

    @Query("select n from ReceivingNoteEntity n where n.status in :statuses order by n.noteId")
    List<ReceivingNoteEntity> findAllByStatusIn(@Param("statuses") Collection<ReceivingNoteStatus> statuses);

    @Query("select n from ReceivingNoteEntity n where n.status not in :statuses order by n.noteId")
    List<ReceivingNoteEntity> findAllByStatusNotIn(@Param("statuses") Collection<ReceivingNoteStatus> statuses);
}
