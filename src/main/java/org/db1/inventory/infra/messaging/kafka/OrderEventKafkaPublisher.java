package org.db1.inventory.infra.messaging.kafka;

import java.time.Instant;

import org.db1.inventory.application.port.out.OrderEventPublisher;
import org.db1.inventory.domain.modal.ReservationResult;
import org.db1.inventory.infra.messaging.kafka.dto.OrderEventEnvelope;
import org.db1.inventory.infra.messaging.kafka.dto.OrderEventPayload;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import io.vertx.core.json.Json;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderEventKafkaPublisher implements OrderEventPublisher {

    @Inject
    Logger logger;

    @Channel("orders-out")
    Emitter<String> emitter;

    @Override
    public void publishReservationResult(ReservationResult result) {

        OrderEventPayload payload = new OrderEventPayload();
        payload.setOrderId(result.getOrderId());
        payload.setCreatedAt(Instant.now().toString());

        String type;

        if (result.isSuccess()) {
            type = "OrderConfirmed";

            logger.info("Evento OrderConfirmed publicado para orderId= " + result.getOrderId());
        } else {
            type = "OrderRejected";
            payload.setReason(result.getReason());

            logger.info("Evento OrderRejected publicado para orderId= " + result.getOrderId() + ", motivo= "
                    + result.getReason());
        }

        OrderEventEnvelope envelope = new OrderEventEnvelope(type, payload);
        emitter.send(Json.encodePrettily(envelope));
    }
}