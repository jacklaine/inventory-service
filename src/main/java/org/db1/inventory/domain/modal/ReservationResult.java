package org.db1.inventory.domain.modal;

import java.util.List;

import org.db1.inventory.infra.messaging.kafka.dto.OrderItem;

public class ReservationResult {

    private String orderId;

    private boolean success;

    private String reason;

    private String customerId;

    private List<OrderItem> items;

    public ReservationResult(String orderId, boolean success, String reason, String customerId, List<OrderItem> items) {
        this.orderId = orderId;
        this.success = success;
        this.reason = reason;
        this.customerId = customerId;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public static ReservationResult confirmed(String orderId, String customerId, List<OrderItem> items) {
        return new ReservationResult(orderId, true, null, customerId, items);
    }

    public static ReservationResult rejected(String orderId, String reason, String customerId, List<OrderItem> items) {
        return new ReservationResult(orderId, false, reason, customerId, items);
    }
}
