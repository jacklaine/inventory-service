package org.db1.inventory.domain.interfaces;

import java.util.List;

import org.db1.inventory.domain.modal.ReservationResult;
import org.db1.inventory.infra.messaging.kafka.dto.OrderItem;

public interface IReserveStock {

    ReservationResult reserve(String orderId, List<OrderItem> items);
    
}
