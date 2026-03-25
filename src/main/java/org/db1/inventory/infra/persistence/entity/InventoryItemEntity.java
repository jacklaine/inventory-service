package org.db1.inventory.infra.persistence.entity;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "inventory_item")
public class InventoryItemEntity extends PanacheEntity {

    @Column(name = "sku", nullable = false, unique = true, length = 64)
    public String sku;

    @Column(name = "quantity", nullable = false)
    public int quantity;

    @Column(name = "low_stock_threshold", nullable = false)
    public int lowStockThreshold;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    public static InventoryItemEntity findBySku(String sku) {
        return find("sku", sku).firstResult();
    }


}
