package com.gearfirst.warehouse.common.sequence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "note_number_seq")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NoteNumberSeqEntity {

    @EmbeddedId
    private NoteNumberSeqId id;

    /**
     * Next sequence to be allocated (1-based). The generator returns the current value and then increments by 1.
     */
    private Integer nextSeq;

    @Version
    private Long version;
}
