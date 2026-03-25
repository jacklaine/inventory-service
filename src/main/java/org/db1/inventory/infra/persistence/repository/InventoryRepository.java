package org.db1.inventory.infra.persistence.repository;

import java.time.Instant;
import java.util.Optional;

import org.db1.inventory.domain.interfaces.IInventoryItem;
import org.db1.inventory.domain.modal.InventoryItem;
import org.db1.inventory.infra.persistence.entity.InventoryItemEntity;
import org.db1.inventory.infra.persistence.mapper.InventoryItemMapper;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class InventoryRepository implements IInventoryItem, PanacheRepository<InventoryItemEntity> {

    private final EntityManager entityManager;

    public InventoryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public InventoryItem findBySku(String sku) {
        return InventoryItemMapper.toDomain(InventoryItemEntity.findBySku(sku));
    }

    @Override
    public boolean reserveStock(String sku, int quantity) {
        int updated = entityManager.createQuery(
                        "UPDATE InventoryItemEntity i SET i.quantity = i.quantity - :qty, i.updatedAt = :now " +
                                "WHERE i.sku = :sku AND i.quantity >= :qty")
                .setParameter("qty", quantity)
                .setParameter("now", Instant.now())
                .setParameter("sku", sku)
                .executeUpdate();
        return updated > 0;
    }

    @Override
    public InventoryItem findBySkuAfterUpdate(String sku) {
        entityManager.flush();
        entityManager.clear();
        return findBySku(sku);
    }
}
