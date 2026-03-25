package org.db1.inventory.infra.messaging.kafka.dto;

import java.math.BigDecimal;

public class OrderItem {

    private String sku;

    private Integer quantity;

    private BigDecimal unitPrice;

    public OrderItem(String sku, Integer quantity, BigDecimal unitPrice) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getSku() {
        return sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

}
