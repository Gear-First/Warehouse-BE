package com.gearfirst.warehouse.common.sequence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteNumberSeqRepository extends JpaRepository<NoteNumberSeqEntity, NoteNumberSeqId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from NoteNumberSeqEntity e where e.id = :id")
    Optional<NoteNumberSeqEntity> findByIdForUpdate(@Param("id") NoteNumberSeqId id);
}
