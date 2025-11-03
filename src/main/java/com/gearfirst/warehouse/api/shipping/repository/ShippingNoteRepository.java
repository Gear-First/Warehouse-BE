package com.gearfirst.warehouse.api.shipping.repository;

import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;
import java.util.List;
import java.util.Optional;

public interface ShippingNoteRepository {
    List<ShippingNote> findNotDone(String date);

    List<ShippingNote> findDone(String date);

    // New overloads: centralized filtering (range > single) and optional warehouseCode filter
    List<ShippingNote> findNotDone(String date, String dateFrom, String dateTo, String warehouseCode);

    List<ShippingNote> findDone(String date, String dateFrom, String dateTo, String warehouseCode);

    Optional<ShippingNote> findById(Long noteId);

    ShippingNote save(ShippingNote note);
}
