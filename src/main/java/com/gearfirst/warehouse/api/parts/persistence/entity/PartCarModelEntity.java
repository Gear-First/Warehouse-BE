package com.gearfirst.warehouse.api.parts.persistence.entity;

import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
