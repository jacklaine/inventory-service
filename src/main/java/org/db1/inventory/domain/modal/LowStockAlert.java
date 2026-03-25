package org.db1.inventory.domain.modal;

public class LowStockAlert {

    private String sku;

    private int currentQuantity;

    private int threshold;

    public LowStockAlert(String sku, int currentQuantity, int threshold) {
        this.sku = sku;
        this.currentQuantity = currentQuantity;
        this.threshold = threshold;
    }

    public String getSku() {
        return sku;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public int getThreshold() {
        return threshold;
    }

}
