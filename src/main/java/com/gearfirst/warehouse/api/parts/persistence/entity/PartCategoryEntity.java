package com.gearfirst.warehouse.api.parts.persistence.entity;

import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "part_category")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PartCategoryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(length = 200)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;
}
