package org.db1.inventory.infra.messaging.kafka.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventPayload {

    private String orderId;

    private String customerId;

    private List<OrderItem> items;

    private String reason;

    private String createdAt;

}