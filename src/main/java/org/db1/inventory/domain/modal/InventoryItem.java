package org.db1.inventory.domain.modal;

import java.util.UUID;

public class InventoryItem {

    private UUID id;

    private String sku;

    private int quantity;

    private int lowStockThreshold;

    public InventoryItem(String sku, int quantity, int lowStockThreshold) {
        this.sku = sku;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean canReserve(int requested) {
        return quantity >= requested;
    }

    public void reserve(int requested) {
        if (!canReserve(requested)) {
            throw new IllegalStateException(
                    "Estoque insuficiente, solicitado: " + requested + ", disponível: " + quantity);
        }
        this.quantity -= requested;
    }

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

}
