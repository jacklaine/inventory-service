package org.db1.inventory.infra.persistence.repository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.db1.inventory.domain.modal.InventoryItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@ExtendWith(MockitoExtension.class)
class InventoryRepositoryTest {

    @Mock
    EntityManager entityManager;

    @Mock
    Query query;

    @Test
    void shouldReturnTrueWhenReserveStockUpdatesRows() {
        InventoryRepository repository = new InventoryRepository(entityManager);

        when(entityManager.createQuery(org.mockito.ArgumentMatchers.anyString())).thenReturn(query);
        when(query.setParameter(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        boolean reserved = repository.reserveStock("SKU-10", 2);

        assertTrue(reserved);
    }

    @Test
    void shouldReturnFalseWhenReserveStockUpdatesNoRows() {
        InventoryRepository repository = new InventoryRepository(entityManager);

        when(entityManager.createQuery(org.mockito.ArgumentMatchers.anyString())).thenReturn(query);
        when(query.setParameter(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        boolean reserved = repository.reserveStock("SKU-10", 2);

        assertFalse(reserved);
    }

    @Test
    void shouldFlushAndClearBeforeFindingBySkuAfterUpdate() {
        InventoryItem expected = new InventoryItem("SKU-20", 7, 2);

        InventoryRepository repository = new InventoryRepository(entityManager) {
            @Override
            public InventoryItem findBySku(String sku) {
                return expected;
            }
        };

        InventoryItem result = repository.findBySkuAfterUpdate("SKU-20");

        verify(entityManager).flush();
        verify(entityManager).clear();
        assertSame(expected, result);
    }
}
