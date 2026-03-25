package org.db1.inventory.domain.interfaces;

import org.db1.inventory.domain.modal.LowStockAlert;

public interface ILowStockNotify {

    void notify(LowStockAlert alert);

}
