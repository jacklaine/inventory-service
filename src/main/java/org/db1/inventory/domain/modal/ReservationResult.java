package org.db1.inventory.domain.modal;

public class ReservationResult {

    private String orderId;

    private boolean success;

    private String reason;

    public ReservationResult(String orderId, boolean success, String reason) {
        this.orderId = orderId;
        this.success = success;
        this.reason = reason;
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

    public static ReservationResult confirmed(String orderId) {
        return new ReservationResult(orderId, true, null);
    }

    public static ReservationResult rejected(String orderId, String reason) {
        return new ReservationResult(orderId, false, reason);
    }
}
