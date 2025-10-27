package com.gearfirst.warehouse.api.parts.persistence;

import com.gearfirst.warehouse.api.parts.persistence.entity.PartCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartCategoryJpaRepository extends JpaRepository<PartCategoryEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
}
