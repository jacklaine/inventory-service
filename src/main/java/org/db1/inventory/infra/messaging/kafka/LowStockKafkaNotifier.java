package org.db1.inventory.infra.messaging.kafka;

import java.time.Instant;

import org.db1.inventory.application.port.out.LowStockPublisher;
import org.db1.inventory.domain.modal.LowStockAlert;
import org.db1.inventory.infra.messaging.kafka.dto.LockStockAlertKafka;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import io.vertx.core.json.Json;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LowStockKafkaNotifier implements LowStockPublisher {

    @Inject
    Logger logger;

    @Channel("low-stock-out")
    Emitter<String> emitter;

    @Channel("low-stock-dlq-out")
    Emitter<String> dlqEmitter;

    @Override
    public void notify(LowStockAlert alert) {
        logger.info("Alerta de estoque" + " sku=" + alert.getSku()
                + ", currentQuantity=" + alert.getCurrentQuantity()
                + ", threshold=" + alert.getThreshold());

        LockStockAlertKafka dto = new LockStockAlertKafka(
                alert.getSku(),
                alert.getCurrentQuantity(),
                alert.getThreshold(),
                Instant.now().toString());

        String payload = Json.encodePrettily(dto);

        emitter.send(payload)
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        logger.error("Falha ao publicar alerta de estoque. Publicando em DLQ.", throwable);
                        dlqEmitter.send(payload);
                    }
                });
    }
}
