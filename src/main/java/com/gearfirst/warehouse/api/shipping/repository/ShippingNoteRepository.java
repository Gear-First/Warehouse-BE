package com.gearfirst.warehouse.api.shipping.repository;

import com.gearfirst.warehouse.api.shipping.domain.ShippingNote;

import java.util.List;
import java.util.Optional;

public interface ShippingNoteRepository {
    List<ShippingNote> findNotDone(String date);
    List<ShippingNote> findDone(String date);
    Optional<ShippingNote> findById(Long noteId);
    ShippingNote save(ShippingNote note);
}
