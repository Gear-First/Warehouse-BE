package com.gearfirst.warehouse.api.parts.persistence;

import com.gearfirst.warehouse.api.parts.persistence.entity.PartEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartJpaRepository extends JpaRepository<PartEntity, Long> {
    boolean existsByCodeIgnoreCase(String code);
    long countByCategoryId(Long categoryId);
    List<PartEntity> findByCategoryId(Long categoryId);
}
