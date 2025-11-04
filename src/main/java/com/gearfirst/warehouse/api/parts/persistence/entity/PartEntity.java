package com.gearfirst.warehouse.api.parts.persistence.entity;

import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "part", indexes = @Index(name = "IDX_part_category", columnList = "categoryId"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PartEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    // Optional supplier attribution for inventory filtering (nullable)
    private String supplierName;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Long categoryId;

    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Integer safetyStockQty = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;
}
