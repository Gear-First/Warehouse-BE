package com.gearfirst.warehouse.common.sequence;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteNumberGenerator {

    private final NoteNumberSeqRepository repository;

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    @Transactional
    public String generateReceivingNo(String warehouseCode, OffsetDateTime requestedAtUtc) {
        return generate("IN", warehouseCode, requestedAtUtc);
    }

    @Transactional
    public String generateShippingNo(String warehouseCode, OffsetDateTime requestedAtUtc) {
        return generate("OUT", warehouseCode, requestedAtUtc);
    }

    private String generate(String type, String warehouseCode, OffsetDateTime requestedAt) {
        if (warehouseCode == null || warehouseCode.isBlank() || requestedAt == null) {
            throw new IllegalArgumentException("warehouseCode and requestedAt are required for number generation");
        }
        String dateYmd = requestedAt.atZoneSameInstant(ZoneOffset.UTC).toLocalDate().format(YYYYMMDD);
        NoteNumberSeqId id = new NoteNumberSeqId(type, warehouseCode, dateYmd);

        // Pessimistic lock row fetch or create with conflict-retry
        NoteNumberSeqEntity row = repository.findByIdForUpdate(id).orElseGet(() -> {
            try {
                NoteNumberSeqEntity created = NoteNumberSeqEntity.builder()
                        .id(id)
                        .nextSeq(1)
                        .build();
                return repository.saveAndFlush(created);
            } catch (DataIntegrityViolationException e) {
                // Row inserted concurrently. Fetch with lock now.
                return repository.findByIdForUpdate(id).orElseThrow();
            }
        });

        int allocated = row.getNextSeq();
        row.setNextSeq(allocated + 1);
        String seq3 = String.format("%03d", allocated);
        return type + "-" + warehouseCode + "-" + dateYmd + "-" + seq3;
    }
}
