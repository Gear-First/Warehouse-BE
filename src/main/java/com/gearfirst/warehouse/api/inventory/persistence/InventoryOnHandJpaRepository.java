package com.gearfirst.warehouse.api.inventory.persistence;

import com.gearfirst.warehouse.api.inventory.persistence.entity.InventoryOnHandEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryOnHandJpaRepository extends JpaRepository<InventoryOnHandEntity, Long> {
    Optional<InventoryOnHandEntity> findByWarehouseCodeAndPartId(String warehouseCode, Long partId);

    List<InventoryOnHandEntity> findAllByWarehouseCode(String warehouseCode);
}
