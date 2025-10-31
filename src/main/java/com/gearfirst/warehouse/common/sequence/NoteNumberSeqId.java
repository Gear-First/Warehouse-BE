package com.gearfirst.warehouse.common.sequence;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NoteNumberSeqId implements Serializable {
    private String type;          // "IN" | "OUT"
    private String warehouseCode; // e.g., "서울"
    private String dateYmd;       // yyyyMMdd (UTC basis)
}
