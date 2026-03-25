package org.db1.inventory.application.port.out;

import org.db1.inventory.domain.modal.LowStockAlert;

public interface LowStockPublisher {

    void notify(LowStockAlert alert);
}
