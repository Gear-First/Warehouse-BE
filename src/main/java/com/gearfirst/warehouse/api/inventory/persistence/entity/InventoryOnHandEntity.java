package com.gearfirst.warehouse.api.inventory.persistence.entity;

import com.gearfirst.warehouse.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_onhand",
        indexes = {
                @Index(name = "IDX_onhand_wh_part", columnList = "warehouseCode,partId"),
                @Index(name = "IDX_onhand_part", columnList = "partId")
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InventoryOnHandEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String warehouseCode;

    @Column(nullable = false)
    private Long partId;

    @Column(nullable = false)
    private Integer onHandQty;

    private OffsetDateTime lastUpdatedAt;

    public void increase(int qty, OffsetDateTime now) {
        if (qty <= 0) {
            return;
        }
        this.onHandQty = (this.onHandQty == null ? 0 : this.onHandQty) + qty;
        this.lastUpdatedAt = now;
    }

    public void decrease(int qty, OffsetDateTime now) {
        if (qty <= 0) {
            return;
        }
        int current = this.onHandQty == null ? 0 : this.onHandQty;
        this.onHandQty = Math.max(0, current - qty);
        this.lastUpdatedAt = now;
    }
}
