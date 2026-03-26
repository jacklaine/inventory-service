package org.db1.inventory.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.db1.inventory.application.port.out.LowStockPublisher;
import org.db1.inventory.application.port.out.OrderEventPublisher;
import org.db1.inventory.domain.modal.InventoryItem;
import org.db1.inventory.domain.modal.LowStockAlert;
import org.db1.inventory.domain.modal.ReservationResult;
import org.db1.inventory.infra.messaging.kafka.dto.OrderItem;
import org.db1.inventory.infra.persistence.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReserveStockUseCaseTest {

    @Mock
    InventoryRepository inventoryRepository;

    @Mock
    OrderEventPublisher orderEventPublisher;

    @Mock
    LowStockPublisher lowStockPublisher;

    @InjectMocks
    ReserveStockUseCase reserveStockUseCase;

    @Test
    void shouldRejectReservationWhenStockIsInsufficient() {
        OrderItem item = new OrderItem("SKU-1", 5, BigDecimal.TEN);

        when(inventoryRepository.reserveStock("SKU-1", 5)).thenReturn(false);

        ReservationResult result = reserveStockUseCase.reserve("order-1", "cus-12345", List.of(item));

        assertFalse(result.isSuccess());
        assertEquals("Estoque insuficiente", result.getReason());
        verify(orderEventPublisher).publishReservationResult(result);
        verify(lowStockPublisher, never()).notify(any(LowStockAlert.class));
        verify(inventoryRepository, never()).findBySkuAfterUpdate(any());
    }

    @Test
    void shouldConfirmReservationAndNotifyWhenLowStock() {
        OrderItem item = new OrderItem("SKU-2", 3, BigDecimal.ONE);

        when(inventoryRepository.reserveStock("SKU-2", 3)).thenReturn(true);
        when(inventoryRepository.findBySkuAfterUpdate("SKU-2"))
                .thenReturn(new InventoryItem("SKU-2", 2, 2));

        ReservationResult result = reserveStockUseCase.reserve("order-2", "cus-12345", List.of(item));

        assertTrue(result.isSuccess());
        assertNull(result.getReason());
        verify(orderEventPublisher).publishReservationResult(result);

        ArgumentCaptor<LowStockAlert> alertCaptor = ArgumentCaptor.forClass(LowStockAlert.class);
        verify(lowStockPublisher).notify(alertCaptor.capture());

        LowStockAlert alert = alertCaptor.getValue();
        assertEquals("SKU-2", alert.getSku());
        assertEquals(2, alert.getCurrentQuantity());
        assertEquals(2, alert.getThreshold());
    }
}
