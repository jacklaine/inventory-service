package org.db1.inventory.domain.interfaces;

import java.util.List;

import org.db1.inventory.domain.modal.OrderItem;
import org.db1.inventory.domain.modal.ReservationResult;

public interface IReserveStock {

    ReservationResult reserve(String orderId, List<OrderItem> items);
    
}
