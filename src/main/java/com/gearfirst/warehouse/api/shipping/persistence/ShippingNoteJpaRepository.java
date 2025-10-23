package com.gearfirst.warehouse.api.shipping.persistence;

import com.gearfirst.warehouse.api.shipping.domain.NoteStatus;
import com.gearfirst.warehouse.api.shipping.persistence.entity.ShippingNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShippingNoteJpaRepository extends JpaRepository<ShippingNoteEntity, Long> {

    @Query("select n from ShippingNoteEntity n where n.status in :statuses order by n.noteId")
    List<ShippingNoteEntity> findAllByStatusIn(@Param("statuses") Collection<NoteStatus> statuses);

    @Query("select n from ShippingNoteEntity n where n.status not in :statuses order by n.noteId")
    List<ShippingNoteEntity> findAllByStatusNotIn(@Param("statuses") Collection<NoteStatus> statuses);
}
