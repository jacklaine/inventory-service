package org.db1.inventory.domain.modal;

import java.math.BigDecimal;

public class OrderItem {

    private String sku;

    private String quantity;

    private BigDecimal unitPrice;

    public OrderItem(String sku, String quantity, BigDecimal unitPrice) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getSku() {
        return sku;
    }

    public String getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

}
