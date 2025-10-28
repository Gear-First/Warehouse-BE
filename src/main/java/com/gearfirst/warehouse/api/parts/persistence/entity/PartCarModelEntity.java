package com.gearfirst.warehouse.api.parts.persistence.entity;

import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "part_car_model",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_part_car_model", columnNames = {"partId", "carModelId"})
        },
        indexes = {
                @Index(name = "IDX_pcm_carmodel", columnList = "carModelId")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PartCarModelEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partId;

    @Column(nullable = false)
    private Long carModelId;

    @Column(length = 200)
    private String note;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;
}
