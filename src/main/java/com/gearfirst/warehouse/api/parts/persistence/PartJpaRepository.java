package com.gearfirst.warehouse.api.parts.persistence;

import com.gearfirst.warehouse.api.parts.persistence.entity.PartEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartJpaRepository extends JpaRepository<PartEntity, Long> {
    boolean existsByCodeIgnoreCase(String code);

    long countByCategoryId(Long categoryId);

    List<PartEntity> findByCategoryId(Long categoryId);

    // Server-side pagination & filtering for list API
    Page<PartEntity> findByEnabledTrueAndCodeContainingIgnoreCaseAndNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

    Page<PartEntity> findByEnabledTrueAndCodeContainingIgnoreCaseAndNameContainingIgnoreCaseAndCategoryId(
            String code,
            String name,
            Long categoryId,
            Pageable pageable
    );
}
