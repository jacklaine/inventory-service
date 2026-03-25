package org.db1.inventory.domain.interfaces;

import org.db1.inventory.domain.modal.InventoryItem;

public interface IInventoryItem {

    InventoryItem findBySku(String sku);

    boolean reserveStock(String sku, int quantity);

    InventoryItem findBySkuAfterUpdate(String sku);
}
