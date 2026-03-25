package org.db1.inventory.infra.messaging.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventEnvelope {

    private String type;
    
    private OrderEventPayload payload;
}
