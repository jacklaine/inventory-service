package org.db1.inventory.infra.persistence.mapper;

import org.db1.inventory.domain.modal.InventoryItem;
import org.db1.inventory.infra.persistence.entity.InventoryItemEntity;

public final class InventoryItemMapper {

    public static InventoryItem toDomain(InventoryItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return new InventoryItem(entity.getSku(),
                entity.getQuantity(), entity.getLowStockThreshold());
    }
}
