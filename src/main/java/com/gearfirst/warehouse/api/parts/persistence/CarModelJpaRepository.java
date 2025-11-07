package com.gearfirst.warehouse.api.parts.persistence;

import com.gearfirst.warehouse.api.parts.persistence.entity.CarModelEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarModelJpaRepository extends JpaRepository<CarModelEntity, Long> {
    boolean existsByNameIgnoreCase(String name);

    List<CarModelEntity> findByEnabledTrueAndNameContainingIgnoreCase(String name);

    Page<CarModelEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<CarModelEntity> findByEnabledAndNameContainingIgnoreCase(boolean enabled, String name, Pageable pageable);
}
