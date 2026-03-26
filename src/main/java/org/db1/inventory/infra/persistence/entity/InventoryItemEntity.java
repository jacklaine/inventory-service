package org.db1.inventory.infra.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "inventory_item")
public class InventoryItemEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "sku", nullable = false, unique = true, length = 64)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold;

    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public static InventoryItemEntity findBySku(String sku) {
        return find("sku", sku).firstResult();
    }

}
