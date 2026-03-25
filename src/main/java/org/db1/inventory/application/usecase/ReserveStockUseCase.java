package org.db1.inventory.application.usecase;

import java.util.List;

import org.db1.inventory.application.port.out.LowStockPublisher;
import org.db1.inventory.application.port.out.OrderEventPublisher;
import org.db1.inventory.domain.interfaces.IReserveStock;
import org.db1.inventory.domain.modal.InventoryItem;
import org.db1.inventory.domain.modal.LowStockAlert;
import org.db1.inventory.domain.modal.ReservationResult;
import org.db1.inventory.infra.messaging.kafka.dto.OrderItem;
import org.db1.inventory.infra.persistence.repository.InventoryRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ReserveStockUseCase implements IReserveStock {

    private final InventoryRepository inventoryRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final LowStockPublisher lowStockNotifier;

    public ReserveStockUseCase(InventoryRepository inventoryRepository,
            OrderEventPublisher orderEventPublisher,
            LowStockPublisher lowStockNotifier) {
        this.inventoryRepository = inventoryRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.lowStockNotifier = lowStockNotifier;
    }

    @Override
    @Transactional
    public ReservationResult reserve(String orderId, List<OrderItem> items) {
        for (OrderItem item : items) {
            boolean reserved = inventoryRepository.reserveStock(item.getSku(), item.getQuantity());
            if (!reserved) {
                ReservationResult rejected = ReservationResult.rejected(orderId, "Estoque insuficiente");
                orderEventPublisher.publishReservationResult(rejected);
                return rejected;
            }
        }

        for (OrderItem item : items) {
            InventoryItem inventoryItem = inventoryRepository.findBySkuAfterUpdate(item.getSku());
            if (inventoryItem != null) {
                checkLowStock(inventoryItem);
            }
        }

        ReservationResult confirmed = ReservationResult.confirmed(orderId);
        orderEventPublisher.publishReservationResult(confirmed);
        return confirmed;
    }

    private void checkLowStock(InventoryItem inventoryItem) {
        if (inventoryItem.isLowStock()) {
            lowStockNotifier.notify(new LowStockAlert(
                    inventoryItem.getSku(),
                    inventoryItem.getQuantity(),
                    inventoryItem.getLowStockThreshold()));
        }
    }
}
