package com.gearfirst.warehouse.api.parts.persistence;

import com.gearfirst.warehouse.api.parts.persistence.entity.PartCarModelEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartCarModelJpaRepository extends JpaRepository<PartCarModelEntity, Long> {
    long countByPartIdAndEnabledTrue(Long partId);
    long countByCarModelIdAndEnabledTrue(Long carModelId);
    List<PartCarModelEntity> findByPartIdAndEnabledTrue(Long partId);
    List<PartCarModelEntity> findByCarModelIdAndEnabledTrue(Long carModelId);
    boolean existsByPartIdAndCarModelId(Long partId, Long carModelId);
    Optional<PartCarModelEntity> findByPartIdAndCarModelId(Long partId, Long carModelId);
}
