package org.db1.inventory.application.port.out;

import org.db1.inventory.domain.modal.ReservationResult;

public interface OrderEventPublisher {

    void publishReservationResult(ReservationResult result);
}
