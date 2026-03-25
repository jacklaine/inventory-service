package org.db1.inventory.infra.messaging.kafka.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LockStockAlertKafka {

    private String sku;

    private int currentQuantity;

    private int threshold;

    private String observedAt;

    public LockStockAlertKafka(String sku, int currentQuantity, int threshold, String observedAt) {
        this.sku = sku;
        this.currentQuantity = currentQuantity;
        this.threshold = threshold;
        this.observedAt = observedAt;
    }
}
