package org.db1.inventory.entrypoint.consumer;

import java.math.BigDecimal;
import java.util.List;

import org.db1.inventory.domain.interfaces.IReserveStock;
import org.db1.inventory.infra.messaging.kafka.dto.OrderEventEnvelope;
import org.db1.inventory.infra.messaging.kafka.dto.OrderItem;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KafkaConsumer {

    @Inject
    Logger logger;

    private final IReserveStock reserveStockUseCase;

    public KafkaConsumer(IReserveStock reserveStockUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
    }

    @Incoming("orders-in")
    public void consume(String msg) {
        OrderEventEnvelope envelope = mapToDTO(msg, OrderEventEnvelope.class);
        if ("OrderCreated".equals(envelope.getType())) {
            handleOrderCreated(envelope);
        }
    }

    private void handleOrderCreated(OrderEventEnvelope envelope) {
        logger.info("Processando OrderCreated para orderId=" + envelope.getPayload().getOrderId());

        List<OrderItem> items = envelope.getPayload().getItems().stream()
                .map(i -> new OrderItem(
                        i.getSku(),
                        i.getQuantity(),
                        BigDecimal.valueOf(i.getUnitPrice().doubleValue())))
                .toList();

        reserveStockUseCase.reserve(envelope.getPayload().getOrderId(), envelope.getPayload().getCustomerId(), items);
    }

    private <T> T mapToDTO(String msg, Class<T> dtoClass) {
        JsonObject json = new JsonObject(msg);
        return json.mapTo(dtoClass);
    }
}
